package lrunner;

import famicom.api.annotation.*;
import famicom.api.ppu.IFamicomPPU;
import famicom.api.ppu.PPUMemory;
import famicom.api.ppu.rom.NesFileRom;
import famicom.api.state.ScanState;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by hkoba on 2017/01/02.
 */
@FamicomRom(name = "ロードランナー")
@ChrRom(type = NesFileRom.class, args = "/Users/hkoba/Documents/ROM/DQ3.nes")
@ChrRom(type = NesFileRom.class, args = "/Users/hkoba/Documents/ROM/LodeRunner.nes")
public class LodeRunnerRom {
    @Attach
    private IFamicomPPU famicomPPU;

    @Attach
    private PPUMemory ppuMemory;

    @Initialize
    private void init() {
        for (int i = 0; i < 256 * 16; i++) {
            famicomPPU.getPatternTable(0).write(i, (int)(Math.random() * 256));
        }
        for (int i = 0; i < 32 * 32; i++) {
            famicomPPU.getNameTable(0).write(i, i & 255);
        }
        for (int i = 1; i < 16; i++) {
            famicomPPU.getPaletteTable().write(i, (int)(Math.random() * 256));
        }
        ppuMemory.changeBank(0);
        famicomPPU.getPaletteTable().write(0, 13);
        famicomPPU.getControlData().setSpriteEnabled(true).setSpriteSize(1).setScreenMask(true).setScreenPatternAddress(1);
        famicomPPU.getPatternTable(0)
                .write(0, 0x7e)
                .write(1, 0xc3)
                .write(2, 0xff)
                .write(3, 0x3c);
        famicomPPU.getPaletteTable()
                .write(17, 5)
                .write(18, 6)
                .write(19, 24);
        famicomPPU.getSpriteData(0).setY(30).setX(80).setColor(0);
    }

    @HBlank
    private void test(ScanState state) {
        if (state.getLineCount() == 0) {
            famicomPPU.getControlData().setScreenPatternAddress(0);
        } else if (state.getLineCount() == 120) {
            famicomPPU.getControlData().setScreenPatternAddress(1);
        }
    }
}
