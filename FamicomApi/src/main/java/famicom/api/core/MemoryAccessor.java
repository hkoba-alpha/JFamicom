package famicom.api.core;

import famicom.api.ppu.NameTable;

/**
 * Created by hkoba on 2017/01/01.
 */
public class MemoryAccessor<T extends MemoryAccessor> {
    protected byte[] memoryData;
    private int maskFlag;

    /**
     * 2の階乗で作成すること
     *
     * @param size
     */
    protected MemoryAccessor(int size) {
        memoryData = new byte[size];
        maskFlag = size - 1;
    }

    public T write(int startAddr, int endAddr, byte[] buf, int srcIndex) {
        if (startAddr < 0 || startAddr >= endAddr) {
            return (T) this;
        }
        while (startAddr >= memoryData.length) {
            startAddr -= memoryData.length;
            endAddr -= memoryData.length;
        }
        if (endAddr < memoryData.length) {
            // 範囲内
            System.arraycopy(buf, srcIndex, memoryData, startAddr, endAddr - startAddr);
            updateMemory(startAddr, endAddr);
        } else {
            System.arraycopy(buf, srcIndex, memoryData, startAddr, memoryData.length - startAddr);
            updateMemory(startAddr, memoryData.length);
            // TODO 先頭へ戻る
        }
        return (T) this;
    }
    public T write(int startAddr, int endAddr, byte[] buf) {
        return write(startAddr, endAddr, buf, 0);
    }

    public T read(int startAddr, int endAddr, byte[] buf, int destIndex) {
        if (startAddr < 0 || startAddr >= endAddr) {
            return (T) this;
        }
        while (startAddr >= memoryData.length) {
            startAddr -= memoryData.length;
            endAddr -= memoryData.length;
        }
        if (endAddr < memoryData.length) {
            // 範囲内
            System.arraycopy(memoryData, startAddr, buf, destIndex, endAddr - startAddr);
        } else {
            System.arraycopy(memoryData, startAddr, buf, destIndex, memoryData.length - startAddr);
            // TODO 先頭へ戻る
        }
        return (T) this;
    }
    public T read(int startAddr, int endAddr, byte[] buf) {
        return read(startAddr, endAddr, buf, 0);
    }

    public T write(int addr, int val) {
        addr &= maskFlag;
        memoryData[addr] = (byte) (val & 255);
        updateMemory(addr, addr  + 1);
        return (T) this;
    }
    public int read(int addr) {
        return memoryData[addr & maskFlag] & 255;
    }

    protected void updateMemory(int startAddr, int endAddr) {
    }
}
