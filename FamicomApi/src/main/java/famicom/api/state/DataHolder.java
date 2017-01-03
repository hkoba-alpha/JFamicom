package famicom.api.state;

import java.io.Serializable;

/**
 * Created by hkoba on 2017/01/03.
 */
public class DataHolder {
    public <T extends Serializable> DataHolder set(T data) {
        return set(data.getClass(), data);
    }

    public <T extends Serializable> DataHolder set(Class<?> type, T data) {
        return this;
    }

    public <T extends Serializable> T get(Class<T> type) {
        return get(type, false);
    }
    public <T extends Serializable> T get(Class<T> type, boolean removeFlag) {
        return null;
    }
}
