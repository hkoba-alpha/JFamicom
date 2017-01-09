package famicom.api.ppu;

import famicom.api.memory.MemoryAccessor;

/**
 * Created by hkoba on 2017/01/01.
 */
public class NameTable extends MemoryAccessor<NameTable> {
    protected NameTable() {
        super(256 * 4);
    }

    public int getChar(int lx, int ly) {
        return read((lx & 31) | (ly & 31) << 5);
    }
    public NameTable putChar(int lx, int ly, int ch) {
        ly &= 31;
        if (ly < 30) {
            write((lx & 31) | (ly << 5), ch);
        }
        return this;
    }
    public int getPalette(int lx, int ly) {
        lx &= 31;
        ly &= 31;
        int addr = ((ly >> 2) << 3) | (lx >> 2);
        int sft = ((ly & 2) << 1) | (lx & 2);
        return (read(0x3c0 + addr) >> sft) & 3;
    }

    public NameTable setColor(int lx, int ly, int pal) {
        lx &= 31;
        ly &= 31;
        int addr = ((ly >> 2) << 3) | (lx >> 2);
        int sft = ((ly & 2) << 1) | (lx & 2);
        write(0x3c0 + addr, (read(0x3c0 + addr) & (~(3 << sft))) | ((pal & 3) << sft));
        return this;
    }

    public NameTable print(int lx, int ly, byte[] data, int width, int height, int offset, int lineSize) {
        for (int dy = 0; dy < height; dy++) {
            int yy = (ly + dy) % 30;
            for (int dx = 0; dx < width; dx++) {
                int xx = (lx + dx) & 31;
                write((yy << 5) + xx, data[offset + dy * lineSize + dx]);
            }
        }
        return this;
    }
}
