package famicom.api.memory;

import famicom.api.annotation.*;
import famicom.api.apu.FamicomAPU;
import famicom.api.apu.NoiseSound;
import famicom.api.apu.SquareSound;
import famicom.api.apu.TriangleSound;
import famicom.api.pad.IFamicomPad;
import famicom.api.pad.PadData;
import famicom.api.ppu.IFamicomPPU;
import famicom.api.ppu.SpriteData;
import famicom.api.state.ScanState;

/**
 * 0x0000-0x07ff RAM
 * 0x0800-0x1fff RAM mirror
 * 0x2000-0x2007 PPU
 * 0x2008-0x3fff PPU mirror
 * 0x4000-0x401f APU/etc
 * 0x4020-0x5fff ext RAM
 * 0x6000-0x7fff Battery Backup RAM
 * 0x8000-0xffff PRG ROM
 * Created by hkoba on 2017/01/04.
 */
@FamicomLibrary(priority = 0)
public class FamicomMemory extends SplitMemoryAccessor<FamicomMemory> {
    @Attach
    protected FamicomAPU famicomAPU;

    @Attach
    protected IFamicomPPU famicomPPU;
    @Attach
    protected PPUMemory ppuMemory;
    @Attach
    protected PrgMapper prgMapper;
    @Attach
    protected IFamicomPad famicomPad;

    // APU
    protected int[] squareTimer = new int[2];
    protected int triangleTimer;

    // PPU
    protected int ppuAddr;
    protected int ppuAddrNext;
    protected int ppuPlus = 1;
    protected int spriteAddr;
    protected boolean ppu2006;
    protected boolean ppu2005;
    protected int scrollX;
    protected int scrollY;
    protected int scanLine;
    protected int ppuReadBuf;
    // PAD
    protected boolean padFlag;
    protected int[] padIndex = new int[2];

    public FamicomMemory() {
        // 0x0000-0x07ff
        super(0x4000);
    }

    @Initialize
    protected void init() {
        for (int addr = 0x800; addr < 0x1000; addr += 0x800) {
            entryAccess(new DelegateAccessMemory(addr, addr + 0x800) {
                @Override
                protected MemoryAccessor getMemoryAccessor() {
                    return FamicomMemory.this;
                }
            });
        }
        entryAccess(new AccessMemory(0x2000, 0x4000) {
            @Override
            protected void write(int offset, int val) {
                writePPU(offset, val);
            }

            @Override
            protected int read(int offset) {
                return readPPU(offset);
            }
        });
        entryAccess(new AccessMemory(0x4000, 0x4020) {
            @Override
            protected void write(int offset, int val) {
                writeAPU(offset, val);
            }

            @Override
            protected int read(int offset) {
                return readAPU(offset);
            }
        });
        entryAccess(new SlideAccessMemory(0x4020, 0x8000, 0x800));
        entryAccess(new DelegateAccessMemory(0x8000, 0x10000) {
            @Override
            protected MemoryAccessor getMemoryAccessor() {
                return prgMapper;
            }
        });
    }

    @PostReset
    protected void reset() {
        squareTimer = new int[2];
        triangleTimer = 0;
        ppuAddr = 0;
        ppuPlus = 1;
        spriteAddr = 0;
        ppu2006 = ppu2005 = false;
        scrollX = scrollY = 0;
        scanLine = 0;
        ppuAddrNext = 0;
        ppuReadBuf = 0;
        padIndex = new int[2];
    }

    @HBlank
    protected void hBlank(ScanState state) {
        scanLine = state.getLineCount();
    }


    @Override
    public int read(int addr) {
        return super.read(addr);
    }

    protected int readPPU(int addr) {
        if (addr == 2) {
            ppu2005 = false;
            int ret = scanLine >= 240 ? 0x80 : 0;
            if (famicomPPU.getSpriteData(0).getY() < scanLine) {
                ret |= 0x40;
            }
            return ret;
        } else if (addr == 7) {
            // PPUは１つ遅れて読み込まれる
            int ret = ppuReadBuf;
            if (ppuAddr < 0x4000) {
                ppuReadBuf = ppuMemory.read(ppuAddr);
            } else {
                ppuReadBuf = 0;
            }
            ppuAddr += ppuPlus;
            return ret;
        }
        return 0;
    }

    protected void writePPU(int addr, int val) {
        switch (addr & 7) {
            case 0:
                if ((val & 4) > 0) {
                    ppuPlus = 0x20;
                } else {
                    ppuPlus = 1;
                }
                famicomPPU.getControlData().setSpriteSize((val >> 5) & 1).setScreenPatternAddress((val >> 4) & 1)
                        .setSpritePatternAddress((val >> 3) & 1).setNameTableAddress(val & 3).setNmiEnabled((val & 0x80) > 0);
                //ppuAddrNext = (ppuAddrNext & 0xf3ff) | ((val & 3) << 10);
                break;
            case 1:
                famicomPPU.getControlData().setSpriteEnabled((val & 0x10) > 0)
                        .setScreenEnabled((val & 0x08) > 0)
                        .setSpriteMask((val & 0x04) > 0)
                        .setScreenMask((val & 0x02) > 0);
                break;
            case 3:
                spriteAddr = val;
                break;
            case 4:
                // sprite write
                writeSprite(spriteAddr, val);
                spriteAddr++;
                break;
            case 5:
                if (ppu2005) {
                    scrollY = val;
                    //ppuAddrNext = (ppuAddrNext & 0xfc1f) | (val >> 3);
                    //ppuAddrNext = (ppuAddrNext & 0x8fff) | ((val & 7) << 12);
                } else {
                    scrollX = val;
                    //ppuAddrNext = (ppuAddrNext & 0xffe0) | (val >> 3);
                }
                ppu2005 = !ppu2005;
                //ppu2006 = false;
                famicomPPU.getControlData().setScroll(scrollX, scrollY);
                break;
            case 6:
                if (ppu2005) {
                    ppuAddrNext = (ppuAddrNext & 0xff00) | val;
                    ppuAddr = ppuAddrNext;
                } else {
                    ppuAddrNext = (ppuAddrNext & 0xff) | (val << 8);
                }
                ppu2005 = !ppu2005;
                //ppu2005 = false;
                break;
            case 7:
                if (ppuAddr < 0x4000) {
                    //System.out.printf("PPU[%04X]=%02X\n", ppuAddr, val);
                    ppuMemory.write(ppuAddr, val);
                }
                ppuAddr += ppuPlus;
                break;
            default:
                break;
        }
    }

    protected void writeSprite(int addr, int val) {
        //System.out.println("Sprite[" + Integer.toString(addr, 16) + "]=" + Integer.toString(val, 16));
        SpriteData spriteData = famicomPPU.getSpriteData((addr >> 2) & 63);
        switch (addr & 3) {
            case 0:
                spriteData.setY(val);
                break;
            case 1:
                spriteData.setPattern(val);
                break;
            case 2:
                spriteData.setFlipY((val & 0x80) > 0)
                        .setFlipX((val & 0x40) > 0)
                        .setBehindBg((val & 0x20) > 0)
                        .setColor(val & 3);
                break;
            case 3:
                spriteData.setX(val);
                break;
        }
    }

    protected int readAPU(int addr) {
        if (addr == 0x15) {
            // サウンド状態
            int flag = famicomAPU.isIrqEnabled() ? 0x40: 0;
            int ret = (famicomAPU.getSquare(0).isPlaying() ? 1: 0)
                    | (famicomAPU.getSquare(1).isPlaying() ? 2:0)
                    | (famicomAPU.getTriangle().isPlaying() ? 4: 0)
                    | (famicomAPU.getNoise().isPlaying() ? 8: 0) | flag;
            famicomAPU.clearFrameIrq();
            return ret;
        } else if (addr == 0x16 || addr == 0x17) {
            // Pad
            int ix = addr - 0x16;
            PadData data = famicomPad.getPad(ix);
            int ret = data.isDown(padIndex[ix]) ? 1 : 0;
            padIndex[ix]++;
            if (padIndex[ix] == 24) {
                padIndex[ix] = 0;
            }
            return ret;
        }
        return 0;
    }

    protected void writeAPU(int addr, int val) {
        if (addr == 0x14) {
            // sprite dma
            int mem = val << 8;
            for (int i = 0; i < 256; i++) {
                writeSprite(i, read(mem + i));
            }
            return;
        }
        if (addr < 0x08) {
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
        } else if (addr < 0x0c) {
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
        } else if (addr < 0x10) {
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
        } else if (addr == 0x15) {
            // 音声チャネル制御
            famicomAPU.getSquare(0).setEnabled((val & 1) > 0);
            famicomAPU.getSquare(1).setEnabled((val & 2) > 0);
            famicomAPU.getTriangle().setEnabled((val & 4) > 0);
            famicomAPU.getNoise().setEnabled((val & 8) > 0);
            famicomAPU.getDelta().setEnabled((val & 16) > 0);
        } else if (addr == 0x16) {
            // Pad
            boolean flag = (val & 1) > 0;
            if (padFlag && !flag) {
                // Reset
                padIndex[0] = padIndex[1] = 0;
                famicomPad.getPad(0).reset();
                famicomPad.getPad(1).reset();
            }
            padFlag = flag;
        } else if (addr == 0x17) {
            // APU
            famicomAPU.setStepMode((val & 0x80) == 0 ? FamicomAPU.StepMode.MODE_4STEP: FamicomAPU.StepMode.MODE_5STEP, (val & 0x40) == 0);
        }
    }
}
