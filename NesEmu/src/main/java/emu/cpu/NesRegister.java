package emu.cpu;

/**
 * Created by hkoba on 2017/01/14.
 */
public class NesRegister {
    private int value;
    private final int valueMask;

    public NesRegister(int bit) {
        valueMask = (1 << bit) - 1;
    }
    public int getValue() {
        return value;
    }
    public int setValue(int v) {
        int ret = value;
        value = v & valueMask;
        return ret;
    }
}
