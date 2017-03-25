package famicom.api.core;

import famicom.api.annotation.Attach;
import famicom.api.annotation.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Created by hkoba on 2016/12/31.
 */
public class ComponentManager {
    private class ComponentData implements Comparable<ComponentData> {
        private int priority;
        private Component.ComponentType type;
        private Class<?> classDef;
        private Object instance;

        private ComponentData(Map<String, Object> attr, Class<?> def) {
            priority = (Integer) attr.get("priority");
            type = (Component.ComponentType) attr.get("value");
            classDef = def;
        }

        @Override
        public int compareTo(ComponentData o) {
            if (type.getPriority() > o.type.getPriority()) {
                return -1;
            } else if (type.getPriority() < o.type.getPriority()) {
                return 1;
            }
            if (priority > o.priority) {
                return -1;
            } else if (priority < o.priority) {
                return 1;
            }
            return 0;
        }
    }

    private Map<String, ComponentData> componentMap = new HashMap<>();

    private static final ComponentManager thisInstance = new ComponentManager();

    public static ComponentManager getInstance() {
        return thisInstance;
    }

    private ComponentManager() {
    }

    public List<Class<?>> getComponentClasses() {
        return componentMap.entrySet().stream().filter(v -> v.getKey().equals(v.getValue().classDef.getName())).map(v -> v.getValue()).sorted().map(v -> v.classDef).collect(Collectors.toList());
    }

    private void attachField(Object obj, Class<?> classDef) {
        if (Object.class.equals(classDef)) {
            return;
        }
        for (Field field : classDef.getDeclaredFields()) {
            AnnotationUtil.getAnnotation(field.getAnnotations(), Attach.class).stream().findFirst().ifPresent(v -> {
                field.setAccessible(true);
                try {
                    field.set(obj, getObject(field.getType()));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public <T> T getObject(Class<T> type) {
        ComponentData data = componentMap.get(type.getName());
        if (data != null) {
            if (data.instance == null) {
                try {
                    data.instance = data.classDef.newInstance();
                    attachField(data.instance, data.classDef);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return (T) data.instance;
        }
        return null;
    }

    private void entryClass(Class<?> classDef, ComponentData componentData) {
        if (classDef.equals(Object.class)) {
            return;
        }
        if (componentMap.containsKey(classDef.getName())) {
            // 登録済み
            //return;
        }
        AnnotationUtil.getAnnotation(classDef.getAnnotations(), Component.class).forEach(attr -> {
            ComponentData data = componentMap.get(classDef.getName());
            ComponentData newData = (componentData != null ? componentData : new ComponentData(attr, classDef));
            if (data == null || newData.compareTo(data) < 0) {
                // 登録する
                System.out.println("EntryComponent:" + classDef.getName());
                componentMap.put(classDef.getName(), newData);
                if (componentData == null) {
                    entryClass(classDef.getSuperclass(), newData);
                    if (!classDef.isInterface()) {
                        for (Class<?> cls : classDef.getInterfaces()) {
                            entryClass(cls, newData);
                        }
                    }
                }
            }
        });
        // 親クラスへも遡る
        if (componentData != null && !classDef.isInterface()) {
            entryClass(classDef.getSuperclass(), componentData);
        }
    }

    private void checkClass(Class<?> classDef) {
        if (classDef.isAnnotation() || classDef.isInterface()) {
            return;
        }
        entryClass(classDef, null);
    }

    public ComponentManager scanComponent(URLClassLoader classLoader, URL root) {
        if (root.getFile().endsWith(".jar")) {
            try (JarFile jarFile = new JarFile(root.getFile())) {
                Collections.list(jarFile.entries()).forEach(f -> {
                    if (!f.getName().endsWith(".class")) {
                        return;
                    }
                    String name = f.getName().replaceAll(".class$", "").replace('/', '.');
                    try {
                        checkClass(classLoader.loadClass(name));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            File topFile = new File(root.getFile());
            try {
                Path topPath = topFile.toPath();
                Files.walkFileTree(topPath, new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (file.toString().endsWith(".class")) {
                            String name = topPath.relativize(file).toString().replaceAll(".class$", "").replace('/', '.');
                            try {
                                checkClass(classLoader.loadClass(name));
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    public ComponentManager scanComponent(Class<?> cls) {
        final ClassLoader classLoader = cls.getClassLoader();
        final URL root = classLoader.getResource(cls.getPackage().getName().replace('.', '/'));

        if ("file".equals(root.getProtocol())) {
            File topFile = new File(root.getFile());
            String pkg = cls.getPackage().getName();
            int pkgLen = pkg.length() - pkg.replaceAll("\\.", "").length();
            for (int i = 0; i <= pkgLen; i++) {
                topFile = topFile.getParentFile();
            }
            try {
                Path topPath = topFile.toPath();
                Files.walkFileTree(topPath, new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (file.toString().endsWith(".class")) {
                            String name = topPath.relativize(file).toString().replaceAll(".class$", "").replace('/', '.');
                            try {
                                checkClass(classLoader.loadClass(name));
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if ("jar".equals(root.getProtocol())) {
            try (JarFile jarFile = ((JarURLConnection) root.openConnection()).getJarFile()) {
                Collections.list(jarFile.entries()).forEach(f -> {
                    if (!f.getName().endsWith(".class")) {
                        return;
                    }
                    String name = f.getName().replaceAll(".class$", "").replace('/', '.');
                    try {
                        checkClass(classLoader.loadClass(name));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return this;
    }
}
