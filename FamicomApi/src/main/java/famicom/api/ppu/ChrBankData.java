package famicom.api.ppu;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hkoba on 2017/01/03.
 */
public class ChrBankData {
    private int ppuAddr;
    private int size;
    private byte[] data;

    public ChrBankData(int addr, byte[] data) {
        ppuAddr = addr;
        this.data = data;
        size = data.length;
    }

    public int getPpuAddr() {
        return ppuAddr;
    }

    public int getSize() {
        return size;
    }

    public byte[] getData() {
        return data;
    }

    public static List<ChrBankData> makeBankData(byte[] data, int addr, int size) {
        List<ChrBankData> ret = new ArrayList<>();
        if (data != null) {
            int ix = 0;
            while (ix < data.length) {
                byte[] dt = new byte[size];
                System.arraycopy(data, ix, dt, 0, size > data.length - ix ? data.length - ix: size);
                ret.add(new ChrBankData(addr, dt));
                ix += size;
            }
        }
        return ret;
    }

}
