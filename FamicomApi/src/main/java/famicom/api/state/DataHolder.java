package famicom.api.state;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hkoba on 2017/01/03.
 */
public class DataHolder {
    protected Map<String, Serializable> dataMap = new HashMap<>();

    public <T extends Serializable> DataHolder set(T data) {
        return set(data.getClass(), data);
    }

    public <T extends Serializable> DataHolder set(Class<?> type, T data) {
        dataMap.put(type.getName(), data);
        return this;
    }

    public <T extends Serializable> T get(Class<T> type) {
        return get(type, false);
    }

    public <T extends Serializable> T get(Class<T> type, boolean removeFlag) {
        Serializable ret;
        if (removeFlag) {
            ret = dataMap.remove(type.getName());
        } else {
            ret = dataMap.get(type.getName());
        }
        return (T) ret;
    }
}
