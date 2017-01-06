package lrunner;

import famicom.api.annotation.*;
import famicom.api.apu.FamicomAPU;
import famicom.api.pad.IFamicomPad;
import famicom.api.pad.PadData;
import famicom.api.ppu.IFamicomPPU;
import famicom.api.ppu.PPUMemory;
import famicom.api.ppu.rom.NesFileRom;
import famicom.api.state.ScanState;
import lrunner.apu.PsgSoundData;
import lrunner.apu.SoundManager;
import lrunner.data.StageConfig;
import lrunner.data.StageData;
import lrunner.play.NormalTitlePlay;
import lrunner.play.PlayBase;
import lrunner.play.StartPlay;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by hkoba on 2017/01/02.
 */
@FamicomRom(name = "ロードランナー", mirror = FamicomRom.MirrorMode.VERTICAL)
@ChrRom(type = NesFileRom.class, args = "/Users/hkoba/Documents/ROM/LodeRunner.nes")
@ChrRom(type = NesFileRom.class, args = "/Users/hkoba/Documents/ROM/DQ1.nes")
public class LodeRunnerRom {

    private int y = 0;

    private byte[] prog;

    private int addr;
    private int[] color1 = new int[] { 0x6, 0x16, 0x26, 0x16 };
    private int[] color2 = new int[] { 0x20, 0x10, 0x00, 0x10 };

    private static byte[][] blockData;

    //public static SoundManager soundManager;
    @Attach
    private SoundManager soundManager;

    static class CharaData {
        int spriteNum;
        int charaNum;
        int colorNum;
        boolean revFlag;
        int spX;
        int spY;

        CharaData(int num, int cl) {
            spriteNum = num;
            colorNum = cl;
        }

        CharaData setChara(int num, boolean rev) {
            charaNum = num;
            revFlag = rev;
            return this;
        }

        CharaData setPosition(int x, int y) {
            spX = x;
            spY = y;
            return this;
        }

        void setSprite(IFamicomPPU ppu) {
            int spix = spriteNum * 4;
            int dx = 0;
            int chnum = charaNum * 4;
            if (revFlag) {
                dx = 2;
            }
            ppu.getSpriteData(spix).setAttribute(false, revFlag, false, colorNum)
                    .setPattern(chnum + dx).setX(spX).setY(spY);
            ppu.getSpriteData(spix + 1)
                    .setAttribute(false, revFlag, false, colorNum)
                    .setPattern(chnum + dx + 1).setX(spX).setY(spY + 8);
            ppu.getSpriteData(spix + 2)
                    .setAttribute(false, revFlag, false, colorNum)
                    .setPattern(chnum + (dx ^ 2)).setX(spX + 8).setY(spY);
            ppu.getSpriteData(spix + 3)
                    .setAttribute(false, revFlag, false, colorNum)
                    .setPattern(chnum + (dx ^ 2) + 1).setX(spX + 8)
                    .setY(spY + 8);
        }
    }

    private static int[] blockColor = {
            0, 1, 1, 2, 2, 1, 2, 3,
            0, 0, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1,};

    private PlayBase playData;

    @Attach
    private IFamicomPPU famicomPPU;

    @Attach
    private PPUMemory ppuMemory;

    @Attach
    private IFamicomPad famicomPad;

    @Attach
    private FamicomAPU famicomAPU;

    @Initialize
    private void init() {
        blockData = new byte[blockColor.length][5];
        for (int i = 0; i < blockColor.length; i++) {
            for (int j = 0; j < 4; j++) {
                blockData[i][j] = (byte) (0x60 + i * 4 + ((j << 1) & 2) + (j >> 1));
            }
            blockData[i][4] = (byte) blockColor[i];
            int x = (i & 0xf) * 2;
            int y = (i >> 4) * 2 + 6;
            // famicom.getPPU().getNameTable(0x0000).print(x, y, blockData[i],
            // 2, 2, 0, 2).setColor(x, y, 1);
        }
        famicomPPU.getControlData().setSpriteSize(0).setScreenPatternAddress(1).setSpritePatternAddress(0).setNameTableAddress(0);
        famicomPPU.getControlData().setSpriteEnabled(true).setScreenEnabled(true).setSpriteMask(false).setScreenMask(false);
        famicomPPU.getPaletteTable().write(0, 32, new byte[] {
                 0x0f, 0x16, 0x30, 0x38, 0x0f, 0x17,
                        0x26, 0x07, 0x0f, 0x36, 0x00, 0x30, 0x0f,
                        0x38, 0x28, 0x30, 0x0f, 0x16, 0x27, 0x12,
                        0x0f, 0x30, 0x2b, 0x16, 0x0f, 0x29, 0x16,
                        0x30, 0x0f, 0x30, 0x30, 0x30 });

        StageConfig config = new StageConfig(LodeRunnerRom.class.getResource("/sample/data/"));
        StageData stage = config.loadStage(2);
        //stage.initStage();
        //stage.drawStage(famicom.getPPU(), true);
        //playData = new StageStart(stage, new PlayData(stage));
        //playData = new StartPlay(stage);
        playData = new NormalTitlePlay(famicomPPU);
    }

    @PostReset
    private void test() {
        famicomAPU.getSquare(0).setEnabled(true);
        famicomAPU.getSquare(1).setEnabled(true);
        famicomAPU.getTriangle().setEnabled(true);
        famicomAPU.getNoise().setEnabled(true);
        //famicomAPU.getSquare(0).setEnabled(true).setVolume(0, true, 15).setTimer(1, 1200);
        PsgSoundData startSound = new PsgSoundData(
                StartPlay.class.getResourceAsStream("/sound6.txt"));
        soundManager.addSequencer(0, startSound, true);
    }

    @VBlank
    private void stepFrame() {
        addr++;
        famicomPPU.getPaletteTable().write(9, color1[(addr >> 5) & 3])
                .write(15, color2[(addr >> 4) & 3]);
        playData = playData.stepFrame(famicomPad, famicomPPU);
        for (int i = 0; i < 4; i++) {
            soundManager.soundStep(famicomAPU, false);
        }
    }
}
