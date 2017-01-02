package famicom.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

/**
 * Created by hkoba on 2016/08/16.
 */
public class LibraryLoader {
    public static final String[] libraries = {
        "OpenAL32.dll",
            "OpenAL64.dll",
            "lwjgl.dll",
            "lwjgl64.dll",
            "liblwjgl.so",
            "liblwjgl64.so",
            "liblwjgl.jnilib",
            "openal.dylib",
            "libjinput-linux.so",
            "libjinput-linux64.so",
            "jinput-dx8.dll",
            "jinput-dx8_64.dll",
            "jinput-raw.dll",
            "jinput-raw_64.dll",
            "jinput-wintab.dll",
            "libjinput-osx.jnilib"
    };

    public static void init() {
        init("temp_lib");
    }

    public static void init(String libDir) {
        File dir = new File(libDir);
        dir.mkdirs();
        for (String name: libraries) {
            File libFile = new File(dir, name);
            if (libFile.isFile()) {
                continue;
            }
            try (InputStream is = LibraryLoader.class.getResourceAsStream("/" + name)) {
                byte[] buf = new byte[2048];
                int len;
                try (FileOutputStream os = new FileOutputStream(libFile)) {
                    while ((len = is.read(buf)) > 0) {
                        os.write(buf, 0, len);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            // ライブラリパスを強引に追加する
            Field field = ClassLoader.class.getDeclaredField("usr_paths");
            field.setAccessible(true);
            String[] libs = (String[])field.get(null);
            String[] newLibs = new String[libs.length + 1];
            System.arraycopy(libs, 0, newLibs, 0, libs.length);
            newLibs[libs.length] = libDir;
            field.set(null, newLibs);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
