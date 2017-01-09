package famicom.api.memory;

import famicom.api.annotation.Attach;
import famicom.api.annotation.FamicomApi;
import famicom.api.annotation.Initialize;
import famicom.api.annotation.PostReset;
import famicom.api.apu.FamicomAPU;
import famicom.api.apu.NoiseSound;
import famicom.api.apu.SquareSound;
import famicom.api.apu.TriangleSound;
import famicom.api.ppu.IFamicomPPU;
import famicom.api.ppu.SpriteData;

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
@FamicomApi
public class FamicomMemory extends SplitMemoryAccessor<FamicomMemory> {
    @Attach
    protected FamicomAPU famicomAPU;

    @Attach
    protected IFamicomPPU famicomPPU;
    @Attach
    protected PPUMemory ppuMemory;
    @Attach
    protected PrgMapper prgMapper;

    // APU
    protected int[] squareTimer = new int[2];
    protected int triangleTimer;

    // PPU
    protected int ppuAddr;
    protected int ppuPlus = 1;
    protected int spriteAddr;
    protected boolean ppu2006;
    protected boolean ppu2005;
    protected int scrollX;
    protected int scrollY;

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
                return super.read(offset);
            }
        });
        entryAccess(new AccessMemory(0x4000, 0x4020) {
            @Override
            protected void write(int offset, int val) {
                writeAPU(offset, val);
            }

            @Override
            protected int read(int offset) {
                return super.read(offset);
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
    }

    @Override
    public int read(int addr) {
        return super.read(addr);
    }

    private void writePPU(int addr, int val) {
        switch (addr & 7) {
            case 0:
                if ((val & 0x80) > 0) {
                    ppuPlus = 0x20;
                } else {
                    ppuPlus = 1;
                }
                famicomPPU.getControlData().setSpriteSize((val >> 5) & 1).setScreenPatternAddress((val >> 4) & 1)
                        .setSpritePatternAddress((val >> 3) & 1).setNameTableAddress(val & 3);
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
                    scrollX = val;
                } else {
                    scrollY = val;
                }
                ppu2005 = !ppu2005;
                famicomPPU.getControlData().setScroll(scrollX, scrollY);
                break;
            case 6:
                if (ppu2006) {
                    ppuAddr = (ppuAddr & 0xff00) | val;
                } else {
                    ppuAddr = (ppuAddr & 0xff) | (val << 8);
                }
                ppu2006 = !ppu2006;
                break;
            case 7:
                ppuMemory.write(ppuAddr, val);
                ppuAddr += ppuPlus;
                break;
            default:
                break;
        }
    }
    private void writeSprite(int addr, int val) {
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

    private void writeAPU(int addr, int val) {
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
        }
    }
}
