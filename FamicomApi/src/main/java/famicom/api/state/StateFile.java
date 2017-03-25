package famicom.api.state;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hkoba on 2017/01/04.
 */
public class StateFile {
    protected Map<String, byte[]> saveData = new HashMap<>();
    protected boolean updateFlag;
    protected String subFolder;

    public void setSubKey(String key) {
        if (key == null) {
            subFolder = null;
            return;
        }
        subFolder = DatatypeConverter.printHexBinary(key.getBytes());
    }

    public StateFile clearData(String name) {
        if (saveData.containsKey(name)) {
            saveData.remove(name);
            updateFlag = true;
        }
        return this;
    }
    public StateFile clearAll() {
        if (saveData.size() > 0) {
            saveData.clear();
            updateFlag = true;
        }
        return this;
    }
    public ObjectOutputStream getOutput(String name) {
        updateFlag = true;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                saveData.put(name, super.toByteArray());
                updateFlag = true;
            }
        };
        try {
            return new ObjectOutputStream(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public ObjectInputStream getInput(String name) {
        if (saveData.containsKey(name)) {
            try {
                return new ObjectInputStream(new ByteArrayInputStream(saveData.get(name)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
