package emu.rom;

import famicom.api.annotation.Attach;
import famicom.api.annotation.FamicomRom;
import famicom.api.annotation.Initialize;
import famicom.api.memory.BatteryBackupMemory;
import famicom.api.memory.ChrMapper;
import famicom.api.memory.PrgMapper;
import famicom.api.memory.file.NesRomFile;
import famicom.api.ppu.IFamicomPPU;
import famicom.api.state.StateFile;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by hkoba on 2017/01/14.
 */
@FamicomRom(name = "エミュレータ", packages = "emu", mirror = FamicomRom.MirrorMode.VERTICAL, backup = true)
public class NesRom {
    @Attach
    protected ChrMapper chrMapper;

    @Attach
    protected NesMapperMemory prgMapper;

    @Attach
    protected IFamicomPPU famicomPPU;

    @Attach
    protected BatteryBackupMemory batteryBackupMemory;

    @Initialize
    public void init(StateFile stateFile) {
        NesRomFile romFile = new NesRomFile();
        try {
            //File file = new File("/Users/hkoba/Documents/ROM/LodeRunner.nes");
            //File file = new File("/Users/hkoba/Documents/ROM/Champ.nes");
            //File file = new File("/Users/hkoba/Documents/ROM/DQ1.nes");
            //File file = new File("/Users/hkoba/Documents/ROM/DQ2.nes");
            //File file = new File("/Users/hkoba/Documents/ROM/DQ3.nes");
            File file = new File("/Users/hkoba/Documents/ROM/DQ4.nes");
            //File file = new File("/Users/hkoba/Documents/EmuROM/NES/ROMs/console/NES/キャプテン翼 (J).nes");
            stateFile.setSubKey(file.getName());
            romFile.loadData(file.getAbsolutePath());
            famicomPPU.setMirrorMode(romFile.getMirrorMode());
            for (int i = 0; i < 64; i++) {
                byte[] mem = romFile.getData("PRG" + i);
                if (mem == null) {
                    break;
                }
                prgMapper.entryBank(mem);
            }
            for (int i = 0; i < 16; i++) {
                byte[] mem = romFile.getData("CHR" + i);
                if (mem == null) {
                    break;
                }
                chrMapper.entryBank(mem);
            }
            prgMapper.setMapperType(romFile.getMapperType());
            batteryBackupMemory.setEnabled(romFile.isBackupEnabled());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
