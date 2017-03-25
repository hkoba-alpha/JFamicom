package emu.rom;

import famicom.api.annotation.Attach;
import famicom.api.annotation.FamicomApplication;
import famicom.api.annotation.FamicomLibrary;
import famicom.api.annotation.FamicomRom;
import famicom.api.memory.ChrMapper;
import famicom.api.memory.PrgMapper;
import famicom.api.ppu.IFamicomPPU;
import org.lwjgl.Sys;

/**
 * Created by hkoba on 2017/02/18.
 */
@FamicomLibrary(priority = 0)
public class NesMapperMemory extends PrgMapper {
    @Attach
    private IFamicomPPU famicomPPU;

    @Attach
    private ChrMapper chrMapper;

    private interface IMapperFunction {
        void reset();

        void write(int addr, int val);

        boolean isIrqEnabled();
    }

    private class NullRom implements IMapperFunction {
        @Override
        public void reset() {
            int prgMax = getBankSize() * 2;
            for (int i = 0; i < 4; i++) {
                selectPage(i, i % prgMax);
            }
            for (int i = 0; i < 8; i++) {
                chrMapper.selectPage(i, i);
            }
        }

        @Override
        public void write(int addr, int val) {
        }

        @Override
        public boolean isIrqEnabled() {
            return false;
        }
    }

    private class CNRom implements IMapperFunction {

        @Override
        public void reset() {
            selectPage(0, 0).selectPage(1, 1).selectPage(2, 2).selectPage(3, 3);
            for (int i = 0; i < 8; i++) {
                chrMapper.selectPage(i, i);
            }
        }

        @Override
        public void write(int addr, int val) {
            for (int i = 0; i < 8; i++) {
                chrMapper.selectPage(i, val * 8 + i);
            }
        }

        @Override
        public boolean isIrqEnabled() {
            return true;
        }
    }

    private class UNRom implements IMapperFunction {

        @Override
        public void reset() {
            selectPage(0, 0).selectPage(1, 1).selectPage(2, getBankSize() * 2 - 2).selectPage(3, getBankSize() * 2 - 1);
        }

        @Override
        public void write(int addr, int val) {
            selectPage(0, val * 2);
            selectPage(1, val * 2 + 1);
        }

        @Override
        public boolean isIrqEnabled() {
            return true;
        }
    }

    private class Mmc1 implements IMapperFunction {
        private int count;
        private int data;
        private boolean chr4k;
        private boolean prgLow;
        private boolean prg16k;
        private int prgLowPage;
        private int prgHighPage;
        private int swapBase;
        private boolean size512;

        @Override
        public void reset() {
            count = 0;
            data = 0;
            prgLowPage = prgHighPage = -1;
            prgLow = prg16k = true;
            chr4k = false;
            setPrg(0, (getBankSize() - 1) & 0x0f, 0);
            size512 = getBankSize() > 16;
        }

        @Override
        public void write(int addr, int val) {
            //System.out.printf("MAP:%04X=%02X\n", addr, val);
            if ((val & 0x80) > 0) {
                count = data = 0;
                return;
            }
            data |= ((val & 1) << count);
            count++;
            if (count < 5) {
                return;
            }
            int reg = data;
            data = 0;
            count = 0;
            if (addr < 0xa000) {
                // 設定
                chr4k = (reg & 0x10) > 0;
                prgLow = (reg & 4) > 0;
                prg16k = (reg & 8) > 0;
                // TODO
                if ((reg & 2) == 0) {
                    // one screen
                    famicomPPU.setMirrorMode(FamicomRom.MirrorMode.FOUR_HORIZONTAL);
                } else if ((reg & 1) > 0) {
                    // hor
                    famicomPPU.setMirrorMode(FamicomRom.MirrorMode.HORIZONTAL);
                } else {
                    famicomPPU.setMirrorMode(FamicomRom.MirrorMode.VERTICAL);
                }
            } else if (addr < 0xc000) {
                if (size512) {
                    setPrg(prgLowPage, prgHighPage, reg & 0x10);
                }
                // chr low
                int page = reg & 0xf;
                if ((reg & 0x10) > 0) {
                    page += chrMapper.getBankSize();
                }
                if (chr4k) {
                    // 4k
                    for (int i = 0; i < 4; i++) {
                        chrMapper.selectPage(i, page * 4 + i);
                    }
                } else {
                    // 8k
                    for (int i = 0; i < 8; i++) {
                        chrMapper.selectPage(i, page * 4 + i);
                    }
                }
            } else if (addr < 0xe000) {
                // chr high
                int page = reg & 0xf;
                if ((reg & 0x10) > 0) {
                    page += chrMapper.getBankSize();
                }
                if (chr4k) {
                    // 4k
                    for (int i = 0; i < 4; i++) {
                        chrMapper.selectPage(i + 4, page * 4 + i);
                    }
                }
            } else {
                // prg
                if (prg16k) {
                    // 16k
                    if (prgLow) {
                        setPrg(reg & 0xf, (getBankSize() - 1) & 0xf, swapBase);
                    } else {
                        setPrg(0, reg & 0xf, swapBase);
                    }
                } else {
                    // 32k
                    setPrg(reg & 0xe, (reg & 0xe) | 1, swapBase);
                }
            }
        }

        private void setPrg(int low, int high, int swap) {
            //System.out.println("16k=" + prg16k + ", area=" + prgLow);
            //System.out.println("MAP:" + low + ", " + high + ", swap=" + swap);
            if (low != prgLowPage || swap != swapBase) {
                prgLowPage = low;
                selectPage(0, (swap | low) * 2);
                selectPage(1, (swap | low) * 2 + 1);
            }
            if (high != prgHighPage || swap != swapBase) {
                prgHighPage = high;
                selectPage(2, (swap | high) * 2);
                selectPage(3, (swap | high) * 2 + 1);
            }
            swapBase = swap;
        }

        @Override
        public boolean isIrqEnabled() {
            return false;
        }
    }

    private class Mmc3 implements IMapperFunction {

        @Override
        public void reset() {
            selectPage(0, getBankSize() * 2 - 2);
            selectPage(1, 1);
            selectPage(2, 0);
            selectPage(3, getBankSize() * 2 - 1);
        }

        @Override
        public void write(int addr, int val) {
            System.out.println("MAP:" + Integer.toString(addr, 16) + ", val=" + Integer.toString(val, 16));
        }

        @Override
        public boolean isIrqEnabled() {
            return true;
        }
    }

    private IMapperFunction mapperFunction;

    public void setMapperType(int type) {
        System.out.println("MapperType=" + type);
        switch (type) {
            case 1:
                mapperFunction = new Mmc1();
                break;
            case 2:
                mapperFunction = new UNRom();
                break;
            case 3:
                mapperFunction = new CNRom();
                break;
            default:
                mapperFunction = new NullRom();
                break;
        }
    }

    @Override
    public PrgMapper write(int startAddr, int endAddr, byte[] buf, int srcIndex) {
        for (int addr = startAddr; addr < endAddr; addr++) {
            write(addr, buf[srcIndex + addr - startAddr] & 255);
        }
        return this;
    }

    @Override
    public PrgMapper write(int addr, int val) {
        if (mapperFunction != null) {
            mapperFunction.write(addr + 0x8000, val);
        }
        return this;
    }

    @Override
    protected void reset() {
        super.reset();
        if (mapperFunction != null) {
            mapperFunction.reset();
        }
    }

    public boolean isIrqEnabled() {
        if (mapperFunction != null) {
            return mapperFunction.isIrqEnabled();
        }
        return false;
    }
}
