package famicom.api.core;

import famicom.api.annotation.Attach;
import famicom.api.annotation.Component;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URL;
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
            priority = (Integer)attr.get("priority");
            type = (Component.ComponentType)attr.get("value");
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
        scanComponent(ComponentManager.class);
    }

    public List<Class<?>> getComponentClasses() {
        return componentMap.values().stream().sorted().map(v -> v.classDef).collect(Collectors.toList());
    }

    public Object getObject(Class<?> type) {
        ComponentData data = componentMap.get(type.getName());
        if (data != null) {
            if (data.instance == null) {
                try {
                    data.instance = data.classDef.newInstance();
                    for (Field field: data.classDef.getDeclaredFields()) {
                        AnnotationUtil.getAnnotation(field.getAnnotations(), Attach.class).stream().findFirst().ifPresent(v -> {
                            field.setAccessible(true);
                            try {
                                field.set(data.instance, getObject(field.getType()));
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return data.instance;
        }
        return null;
    }

    private void entryClass(Class<?> classDef, Map<String, Object> attrMap) {
        if (classDef.equals(Object.class)) {
            return;
        }
        if (componentMap.containsKey(classDef.getName())) {
            // 登録済み
            return;
        }
        AnnotationUtil.getAnnotation(classDef.getAnnotations(), Component.class).forEach(attr -> {
            ComponentData data = componentMap.get(classDef.getName());
            ComponentData newData = new ComponentData((attrMap != null ? attrMap: attr), classDef);
            if (data == null || newData.compareTo(data) < 0) {
                // 登録する
                componentMap.put(classDef.getName(), newData);
                if (attrMap == null) {
                    entryClass(classDef.getSuperclass(), attr);
                }
            }
        });
        // 親クラスへも遡る
        if (attrMap != null) {
            entryClass(classDef.getSuperclass(), attrMap);
        }
    }

    private void checkClass(Class<?> classDef) {
        if (classDef.isAnnotation() || classDef.isInterface()) {
            System.out.println("Out:" + classDef);
            return;
        }
        entryClass(classDef, null);
    }

    public ComponentManager scanComponent(Class<?> cls) {
        final ClassLoader classLoader = cls.getClassLoader();
        final URL root = classLoader.getResource(cls.getPackage().getName().replace('.', '/'));

        if ("file".equals(root.getProtocol())) {
            File topFile = new File(root.getFile());
            String pkg = cls.getPackage().getName();
            String[] lst = cls.getPackage().getName().split(".");
            for (int i = cls.getPackage().getName().split("¥¥.").length; i >= 0; i--) {
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
                            System.out.println(name);
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