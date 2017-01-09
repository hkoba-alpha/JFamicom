package famicom.api.memory;

import famicom.api.core.ExecuteManager;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hkoba on 2017/01/08.
 */
public abstract class AbstractMemoryFile<T extends AbstractMemoryFile> {
    protected Map<String, int[]> nameMap = new HashMap<>();

    public T loadData(String fileName) throws FileNotFoundException {
        File file = new File(fileName);
        InputStream inputStream;
        if (file.isFile()) {
            inputStream = new FileInputStream(file);
        } else {
            inputStream = ExecuteManager.getInstance().getRomClass().getResourceAsStream(fileName);
            if (inputStream == null) {
                throw new FileNotFoundException(fileName);
            }
        }
        readFile(inputStream);
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (T) this;
    }

    protected void entryName(String name, int addr, int size) {
        nameMap.put(name, new int[]{addr, size});
    }

    protected abstract void readFile(InputStream inputStream);

    /**
     * メモリファイルの内容を取得する
     *
     * @return
     */
    public abstract byte[] getData();

    /**
     * 指定した範囲のデータを取得する
     *
     * @param addr
     * @param size
     * @return
     */
    public byte[] getData(int addr, int size) {
        byte[] ret = new byte[size];
        System.arraycopy(getData(), addr, ret, 0, size);
        return ret;
    }

    /**
     * 名前登録されたデータを取得する
     *
     * @param name
     * @return
     */
    public byte[] getData(String name) {
        int[] param = nameMap.get(name);
        if (param != null) {
            return getData(param[0], param[1]);
        }
        return null;
    }

    /**
     * 名前登録されたデータのある範囲を取得する
     *
     * @param name
     * @param addr
     * @param size
     * @return
     */
    public byte[] getData(String name, int addr, int size) {
        byte[] ret = new byte[size];
        System.arraycopy(getData(name), addr, ret, 0, size);
        return ret;
    }
}
