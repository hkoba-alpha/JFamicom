package famicom.api.core;

import famicom.api.annotation.FamicomRom;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarFile;

/**
 * Created by hkoba on 2017/01/02.
 */
public class RomManager {
    public static class RomData implements Serializable {
        private static final long serialVersionUID = 1L;

        private String romName;
        private String className;
        private File filePath;
        private long lastModified;
        private long fileSize;
        private transient ClassLoader classLoader;

        public RomData() {
        }

        /**
         * クラスパス内にあるロムの登録
         * @param romClass
         */
        public RomData(Class<?> romClass) {
            romName = romClass.getAnnotation(FamicomRom.class).name();
            className = romClass.getName();
            filePath = new File("dummy");
            classLoader = romClass.getClassLoader();
        }

        @Override
        public int hashCode() {
            int result = className.hashCode();
            result = 31 * result + filePath.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            RomData src = (RomData) obj;
            return className.equals(src.className) && filePath.equals(src.filePath);
        }

        public boolean isModified() {
            if (!filePath.isFile()) {
                return true;
            }
            return filePath.lastModified() != lastModified || filePath.length() != fileSize;
        }

        private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
            stream.defaultReadObject();
        }

        public Class<?> getRomClass() throws ClassNotFoundException {
            return classLoader.loadClass(className);
        }
    }

    private static final RomManager thisInstance = new RomManager();

    private List<RomData> romDataList = new ArrayList<>();

    public static RomManager getInstance() {
        return thisInstance;
    }

    private RomManager() {

    }

    public int scanRom(File romDir, ClassLoader parentLoader) {
        romDataList.clear();
        if (romDir.isDirectory()) {
            for (File jar: romDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".jar");
                }
            })) {
                try {
                    URLClassLoader appLoader = new URLClassLoader(new URL[]{jar.toURI().toURL()}, parentLoader);
                    // スキャンする
                    try (JarFile jarFile = new JarFile(jar)) {
                        Collections.list(jarFile.entries()).forEach(f -> {
                            if (!f.getName().endsWith(".class")) {
                                return;
                            }
                            String name = f.getName().replaceAll(".class$", "").replace('/', '.');
                            try {
                                Class<?> cls = appLoader.loadClass(name);
                                FamicomRom romInfo = cls.getAnnotation(FamicomRom.class);
                                if (romInfo != null) {
                                    romDataList.add(new RomData(cls));
                                }
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
        // クラスパス内部のROMを探す
        for (Class<?> cls : ComponentManager.getInstance().getComponentClasses()) {
            if (cls.isAnnotationPresent(FamicomRom.class)) {
                romDataList.add(new RomData(cls));
            }
        }
        return romDataList.size();
    }

    public List<RomData> getRomDataList() {
        return romDataList;
    }
}
