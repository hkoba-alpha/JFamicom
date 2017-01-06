package famicom.api.memory;

import famicom.api.annotation.Attach;
import famicom.api.annotation.FamicomApi;
import famicom.api.annotation.PostReset;
import famicom.api.apu.FamicomAPU;
import famicom.api.apu.NoiseSound;
import famicom.api.apu.SquareSound;
import famicom.api.apu.TriangleSound;
import famicom.api.core.MemoryAccessor;

/**
 * 0x0000-0x07ff RAM
 * 0x0800-0x1fff RAM mirror
 * 0x2000-0x2007 PPU
 * 0x2008-0x3fff PPU mirror
 * 0x4000-0x401f APU/etc
 * 0x4020-0x5fff ext RAM
 * 0x6000-0x7fff Battery Backup RAM
 * Created by hkoba on 2017/01/04.
 */
@FamicomApi
public class FamicomRAM extends MemoryAccessor<FamicomRAM> {
    @Attach
    protected FamicomAPU famicomAPU;

    protected int[] squareTimer = new int[2];
    protected int triangleTimer;

    public FamicomRAM() {
        // 0x0000-0x07ff
        super(0x4000);
    }

    @PostReset
    protected void reset() {
        squareTimer = new int[2];
        triangleTimer = 0;
    }

    @Override
    public FamicomRAM write(int startAddr, int endAddr, byte[] buf, int srcIndex) {
        return super.write(startAddr, endAddr, buf, srcIndex);
    }

    @Override
    public FamicomRAM read(int startAddr, int endAddr, byte[] buf, int destIndex) {
        return super.read(startAddr, endAddr, buf, destIndex);
    }

    @Override
    public FamicomRAM write(int addr, int val) {
        if (addr >= 0x4000 && addr < 0x4020) {
            writeAPU(addr, val);
            return this;
        }
        return super.write(addr, val);
    }

    @Override
    public int read(int addr) {
        return super.read(addr);
    }

    public void writeAPU(int addr, int val) {
        if (addr < 0x4008) {
            // 矩形波
            int ix = (addr >> 2) & 1;
            SquareSound sq = famicomAPU.getSquare(ix);
            switch (addr & 3) {
                case 0:
                    if ((val & 0x10) > 0) {
                        // エンベロープ無効
                        sq.setVolume(val >> 6, (val & 0x20) > 0, val & 15);
                    } else {
                        sq.setEnvelope(val >> 6, (val & 0x20) > 0, val & 15);
                    }
                    break;
                case 1:
                    sq.setSweep((val & 0x80) > 0, (val >> 4) & 7, (val & 8) > 0,
                            val & 7);
                    break;
                case 2:
                    squareTimer[ix] = ((squareTimer[ix] & 0x700) | val);
                    break;
                case 3:
                    squareTimer[ix] = ((squareTimer[ix] & 0xff) | ((val & 7) << 8));
                    sq.setTimer((val >> 3) & 0x1f, squareTimer[ix]);
                    break;
            }
        } else if (addr < 0x400c) {
            // 三角波
            TriangleSound triangleData = famicomAPU.getTriangle();
            switch (addr & 3) {
                case 0:
                    triangleData.setLinear((val & 0x80) > 0, val & 0x7f);
                    break;
                case 2:
                    triangleTimer = ((triangleTimer & 0x700) | val);
                    break;
                case 3:
                    triangleTimer = ((triangleTimer & 0xff) | ((val & 7) << 8));
                    triangleData.setTimer((val >> 3) & 0x1f, triangleTimer);
                    break;
            }
        } else if (addr < 0x4010) {
            // ノイズ
            NoiseSound noiseData = famicomAPU.getNoise();
            switch (addr & 3) {
                case 0:
                    if ((val & 0x10) > 0) {
                        // ボリューム
                        noiseData.setVolume((val & 0x20) > 0, val & 15);
                    } else {
                        noiseData.setEnvelope((val & 0x20) > 0, val & 15);
                    }
                    break;
                case 2:
                    noiseData.setRandomMode((val & 0x80) > 0, val & 15);
                    break;
                case 3:
                    noiseData.setLength((val >> 3) & 0x1f);
                    break;
            }
        }
    }
}
