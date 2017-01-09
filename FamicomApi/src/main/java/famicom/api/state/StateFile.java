package famicom.api.state;

import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hkoba on 2017/01/04.
 */
public class StateFile {
    protected Map<String, byte[]> saveData = new HashMap<>();
    protected boolean updateFlag;

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

        };
        return null;
    }
    public ObjectInputStream getInput(String name) {
        return null;
    }
}
