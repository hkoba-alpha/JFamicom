package famicom.api;

import famicom.api.core.ComponentManager;
import famicom.api.core.RomManager;
import famicom.api.main.IFamicomOS;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by hkoba on 2017/01/02.
 */
public class FamicomMain {

    public static void main(String[] args) {
        // クラスパスのコンポーネントを登録する
        ClassLoader loader = FamicomMain.class.getClassLoader();
        if (loader instanceof URLClassLoader) {
            URLClassLoader urlClassLoader = (URLClassLoader) loader;
            for (URL url : urlClassLoader.getURLs()) {
                System.out.println("URL=" + url);
                ComponentManager.getInstance().scanComponent(urlClassLoader, url);
            }
        }

        // 定義ファイルの読み込み
        Properties properties = new Properties();
        try (InputStream inputStream = ComponentManager.class.getResourceAsStream("/jfamicom.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        File prop = new File("jfamicom.properties");
        if (prop.isFile()) {
            try (InputStream inputStream = new FileInputStream(prop)) {
                properties.load(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // ライブラリの読み込み
        File libDir = new File(properties.getProperty("jfamicom.library.dir"));
        List<URL> jarList = new ArrayList<>();
        if (libDir.isDirectory()) {
            for (File jar : libDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".jar");
                }
            })) {
                try {
                    jarList.add(jar.toURI().toURL());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
        if (jarList.size() > 0) {
            loader = new URLClassLoader(jarList.toArray(new URL[0]), loader);
            for (URL url : jarList) {
                ComponentManager.getInstance().scanComponent((URLClassLoader) loader, url);
            }
        }
        // ROMの検索
        if (RomManager.getInstance().scanRom(new File(properties.getProperty("jfamicom.rom.dir")), loader) == 0) {
            System.err.println("ROMが見つかりません");
            return;
        }
        // OSの取得
        IFamicomOS os = ComponentManager.getInstance().getObject(IFamicomOS.class);
        if (os == null) {
            // Error
            System.err.println("実装が見つかりません");
            return;
        }
        os.execute(args, RomManager.getInstance().getRomDataList());
    }
}
