package famicom.impl.ppu;

import famicom.api.annotation.*;
import famicom.api.core.ExecuteManager;
import famicom.api.ppu.*;
import famicom.api.state.ScanState;
import famicom.impl.game.FamicomMainState;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Image;
import org.newdawn.slick.ImageBuffer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import java.util.*;

/**
 * Created by hkoba on 2017/01/01.
 */
@FamicomLibrary(priority = 100)
public class FamicomPPUImpl implements IFamicomPPU {
    public static final int[][] paletteColor = new int[][]{
            {0x75, 0x75, 0x75},
            {0x27, 0x1B, 0x8F}, {0x00, 0x00, 0xAB},
            {0x47, 0x00, 0x9F}, {0x8F, 0x00, 0x77},
            {0xAB, 0x00, 0x13}, {0xA7, 0x00, 0x00},
            {0x7F, 0x0B, 0x00}, {0x43, 0x2F, 0x00},
            {0x00, 0x47, 0x00}, {0x00, 0x51, 0x00},
            {0x00, 0x3F, 0x17}, {0x1B, 0x3F, 0x5F},
            {0x00, 0x00, 0x00}, {0x05, 0x05, 0x05},
            {0x05, 0x05, 0x05},

            {0xBC, 0xBC, 0xBC}, {0x00, 0x73, 0xEF},
            {0x23, 0x3B, 0xEF}, {0x83, 0x00, 0xF3},
            {0xBF, 0x00, 0xBF}, {0xE7, 0x00, 0x5B},
            {0xDB, 0x2B, 0x00}, {0xCB, 0x4F, 0x0F},
            {0x8B, 0x73, 0x00}, {0x00, 0x97, 0x00},
            {0x00, 0xAB, 0x00}, {0x00, 0x93, 0x3B},
            {0x00, 0x83, 0x8B}, {0x11, 0x11, 0x11},
            {0x09, 0x09, 0x09}, {0x09, 0x09, 0x09},

            {0xFF, 0xFF, 0xFF}, {0x3F, 0xBF, 0xFF},
            {0x5F, 0x97, 0xFF}, {0xA7, 0x8B, 0xFD},
            {0xF7, 0x7B, 0xFF}, {0xFF, 0x77, 0xB7},
            {0xFF, 0x77, 0x63}, {0xFF, 0x9B, 0x3B},
            {0xF3, 0xBF, 0x3F}, {0x83, 0xD3, 0x13},
            {0x4F, 0xDF, 0x4B}, {0x58, 0xF8, 0x98},
            {0x00, 0xEB, 0xDB}, {0x66, 0x66, 0x66},
            {0x0D, 0x0D, 0x0D}, {0x0D, 0x0D, 0x0D},

            {0xFF, 0xFF, 0xFF}, {0xAB, 0xE7, 0xFF},
            {0xC7, 0xD7, 0xFF}, {0xD7, 0xCB, 0xFF},
            {0xFF, 0xC7, 0xFF}, {0xFF, 0xC7, 0xDB},
            {0xFF, 0xBF, 0xB3}, {0xFF, 0xDB, 0xAB},
            {0xFF, 0xE7, 0xA3}, {0xE3, 0xFF, 0xA3},
            {0xAB, 0xF3, 0xBF}, {0xB3, 0xFF, 0xCF},
            {0x9F, 0xFF, 0xF3}, {0xDD, 0xDD, 0xDD},
            {0x11, 0x11, 0x11}, {0x11, 0x11, 0x11}};

    private ImageBuffer imageBuffer;

    private byte[] scanLine = new byte[256];

    public FamicomPPUImpl() {
        imageBuffer = new ImageBuffer(512, 448);
        FamicomMainState.getInstance().setImageBuffer(imageBuffer);
        for (int y = 0; y < 448; y += 2) {
            for (int x = 8; x < 512; x++) {
                imageBuffer.setRGBA(x, y, 128, 128, 255, 255);
                imageBuffer.setRGBA(x, y + 1, 128, 128, 255, 128);
            }
        }
    }

    private class ControlDataImpl extends ControlData {
        public int getScrollX() {
            return scrollX;
        }
        public int getScrollY() {
            return scrollY;
        }

        @Override
        public ControlData setNameTableAddress(int nameTableAddress) {
            nameTableIndex = nameTableAddress;
            switch (nameTableAddress) {
                case 1:
                    screenTable[0] = nameTables[1];
                    screenTable[1] = nameTables[0];
                    screenTable[2] = nameTables[3];
                    screenTable[3] = nameTables[2];
                    break;
                case 2:
                    screenTable[0] = nameTables[2];
                    screenTable[1] = nameTables[3];
                    screenTable[2] = nameTables[0];
                    screenTable[3] = nameTables[1];
                    break;
                case 3:
                    screenTable[0] = nameTables[3];
                    screenTable[1] = nameTables[2];
                    screenTable[2] = nameTables[1];
                    screenTable[3] = nameTables[0];
                    break;
                default:
                    screenTable[0] = nameTables[0];
                    screenTable[1] = nameTables[1];
                    screenTable[2] = nameTables[2];
                    screenTable[3] = nameTables[3];
                    break;
            }
            /*
            switch (mirrorMode) {
                case FOUR_HORIZONTAL:
                case HORIZONTAL:
                    screenTable[1] = nameTables[2];
                    screenTable[2] = nameTables[1];
                    break;
                default:
                    if (nameTableAddress == 1) {
                        screenTable[1] = nameTables[2];
                        screenTable[2] = nameTables[1];
                    } else {
                        screenTable[1] = nameTables[1];
                        screenTable[2] = nameTables[2];
                    }
                    break;
            }
            */
            return super.setNameTableAddress(nameTableAddress);
        }
    }

    private class SpriteDataImpl extends SpriteData {
        private boolean scanSprite(int y, PatternTableImpl pattern) {
            int dy = y - super.y;
            if (dy < 0) {
                dy += 256;
            }
            int ht = 8 + controlData.getSpriteSize() * 8;
            if (dy >= ht) {
                return false;
            }
            if (super.flipY) {
                dy = ht - dy - 1;
            }
            int bit;
            if (dy < 8) {
                bit = pattern.getData(super.pattern, dy);
            } else {
                bit = pattern.getData(super.pattern ^ 1, dy - 8);
            }
            int spX = super.x;
            int sft = 14;
            int ax = -2;
            if (super.flipX) {
                sft = 0;
                ax = 2;
            }
            byte[] palette = paletteTable.getData(super.color + 4, super.behindBg ? 0 : 64);
            for (int i = 0; i < 8; i++) {
                if (scanLine[spX] < 0 && (spX >= 8 || controlData.isSpriteMask())) {
                    // 対象
                    int b = (bit >> sft) & 3;
                    if (b > 0) {
                        scanLine[spX] = palette[b - 1];
                    }
                }
                spX = (spX + 1) & 255;
                sft += ax;
            }
            return true;
        }
    }

    private class NameTableImpl extends NameTable {
        private int scanBg(int x, int y, int scanX, PatternTableImpl pattern) {
            int lx = x >> 3;
            int ly = y >> 3;
            int addr = lx | (ly << 5);
            int sft = 14 - ((x & 7) * 2);
            int bit = pattern.getData(memoryData[addr] & 255, y & 7);
            byte[] color = paletteTable.getData(getPalette(lx, ly), 0);
            while (scanX < 256) {
                if (scanLine[scanX] < 64) {
                    int b = (bit >> sft) & 3;
                    if (b > 0) {
                        scanLine[scanX] = color[b - 1];
                    }
                }
                scanX++;
                if (sft == 0) {
                    // キャラが変わった
                    addr++;
                    lx++;
                    if ((addr & 31) == 0) {
                        break;
                    }
                    if ((addr & 1) == 0) {
                        // パレット
                        color = paletteTable.getData(getPalette(lx, ly), 0);
                    }
                    bit = pattern.getData(memoryData[addr] & 255, y & 7);
                    sft = 14;
                } else {
                    sft -= 2;
                }
            }
            return scanX;
        }
    }

    private class PatternTableImpl extends PatternTable {
        private Map<Integer, short[]> charMap = new HashMap<>();

        public int getData(int ch, int y) {
            short[] data = charMap.get(ch);
            if (data == null) {
                // データ作成
                data = new short[8];
                int addr = ch << 4;
                for (int i = 0; i < 8; i++) {
                    byte dt1 = memoryData[addr];
                    byte dt2 = memoryData[addr | 8];
                    short v = 0;
                    for (int x = 0; x < 8; x++) {
                        if ((dt1 & (1 << x)) > 0) {
                            v |= (1 << (x * 2));
                        }
                        if ((dt2 & (1 << x)) > 0) {
                            v |= (1 << (x * 2 + 1));
                        }
                    }
                    data[i] = v;
                    addr++;
                }
                charMap.put(ch, data);
            }
            return data[y];
        }

        @Override
        protected void updateMemory(int startAddr, int endAddr) {
            int ch1 = startAddr >> 4;
            int ch2 = (endAddr + 15) >> 4;
            for (int ch = ch1; ch < ch2; ch++) {
                charMap.remove(ch);
            }
        }
    }

    private class PaletteTableImpl extends PaletteTable {
        /**
         * index: 0-3:bg 4-7:sprite
         *
         * @param index
         * @return
         */
        public byte[] getData(int index, int offset) {
            byte[] ret = new byte[3];
            for (int i = 0; i < 3; i++) {
                ret[i] = (byte) (memoryData[index * 4 + i + 1] + offset);
            }
            return ret;
        }
    }

    private SpriteDataImpl[] spriteDataList = new SpriteDataImpl[64];
    private ControlDataImpl controlData;
    private PatternTableImpl[] patternTables = new PatternTableImpl[2];
    private NameTableImpl[] nameTables = new NameTableImpl[4];
    private NameTableImpl[] screenTable = new NameTableImpl[4];
    private PaletteTableImpl paletteTable;
    private FamicomRom.MirrorMode mirrorMode;
    private int nameTableIndex;

    @Initialize
    @PreReset
    private void init() {
        patternTables[0] = new PatternTableImpl();
        patternTables[1] = new PatternTableImpl();
        for (int i = 0; i < spriteDataList.length; i++) {
            spriteDataList[i] = new SpriteDataImpl();
        }
        nameTableIndex = 0;
        nameTables[0] = new NameTableImpl();
        nameTables[3] = new NameTableImpl();
        controlData = new ControlDataImpl();
        paletteTable = new PaletteTableImpl();
        setMirrorMode(ExecuteManager.getInstance().getFamicomRom().mirror());
        controlData.setNameTableAddress(0);
    }

    private void renderLine(int y) {
        int py = y * 2;
        for (int x = 0; x < 256; x++) {
            byte val = scanLine[x];
            int px = x * 2;
            int r = 0;
            int g = 0;
            int b = 0;
            int a = 0;
            if (val >= -64) {
                // 透明以外
                int[] rgb = paletteColor[val & 63];
                r = rgb[0];
                g = rgb[1];
                b = rgb[2];
                a = 255;
            }
            imageBuffer.setRGBA(px, py, r, g, b, a);
            imageBuffer.setRGBA(px + 1, py, r, g, b, a);
            imageBuffer.setRGBA(px, py + 1, r, g, b, a * 3 / 4);
            imageBuffer.setRGBA(px + 1, py + 1, r, g, b, a * 3 / 4);
        }
    }

    @HBlank
    private void hBlank(ScanState state) {
        if (state.getLineCount() == 0) {
            int[] color = paletteColor[paletteTable.read(0) & 63];
            FamicomMainState.getInstance().setBgColor(color[0], color[1], color[2]);
        }
        if (state.getLineCount() >= 8 && state.getLineCount() < 232) {
            // Bg
            if (controlData.isScreenEnabled()) {
                int nmix = 0;
                int py = state.getLineCount() + controlData.getScrollY();
                int px = controlData.getScrollX();
                int scx = 0;
                if (py >= 240) {
                    nmix ^= 2;
                    py -= 240;
                }
                if (!controlData.isScreenMask()) {
                    scx += 8;
                    px += 8;
                    if (px >= 256) {
                        nmix ^= 1;
                        px -= 256;
                    }
                }
                scx = screenTable[nmix].scanBg(px, py, scx, patternTables[controlData.getScreenPatternAddress()]);
                if (scx < 256) {
                    screenTable[nmix ^ 1].scanBg(0, py, scx, patternTables[controlData.getScreenPatternAddress()]);
                }
            }
            renderLine(state.getLineCount() - 8);
        }
        if (state.getLineCount() >= 7 && state.getLineCount() < 231) {
            // Sprite
            Arrays.fill(scanLine, (byte) -128);
            if (controlData.isSpriteEnabled()) {
                int num = 0;
                int y = state.getLineCount();
                PatternTableImpl pattern = patternTables[controlData.getSpritePatternAddress()];
                for (int i = 0; i < spriteDataList.length; i++) {
                    if (spriteDataList[i].scanSprite(y, pattern)) {
                        num++;
                        if (num >= 8) {
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public ControlData getControlData() {
        return controlData;
    }

    @Override
    public SpriteData getSpriteData(int index) {
        return spriteDataList[index & 63];
    }

    @Override
    public NameTable getNameTable(int index) {
        return nameTables[index & 3];
    }

    @Override
    public PatternTable getPatternTable(int index) {
        return patternTables[index & 1];
    }

    @Override
    public PaletteTable getPaletteTable() {
        return paletteTable;
    }

    @Override
    public IFamicomPPU setMirrorMode(FamicomRom.MirrorMode mode) {
        mirrorMode = mode;
        switch (mode) {
            case FOUR_HORIZONTAL:
            case FOUR_VERTICAL:
                if (nameTables[1] == null) {
                    nameTables[1] = new NameTableImpl();
                    nameTables[2] = new NameTableImpl();
                }
                break;
            case HORIZONTAL:
                nameTables[1] = nameTables[0];
                nameTables[2] = nameTables[3];
                break;
            default:
                nameTables[1] = nameTables[3];
                nameTables[2] = nameTables[0];
                break;
        }
        screenTable[0] = nameTables[0];
        screenTable[1] = nameTables[1];
        screenTable[2] = nameTables[2];
        screenTable[3] = nameTables[3];
        if (mode == FamicomRom.MirrorMode.ONE_SCREEN) {
            screenTable[1] = screenTable[2] = screenTable[3] = nameTables[0];
        }
        this.controlData.setNameTableAddress(nameTableIndex);
        return this;
    }
}
