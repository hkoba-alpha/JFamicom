package emu.rom;

import famicom.api.annotation.Attach;
import famicom.api.annotation.FamicomRom;
import famicom.api.annotation.Initialize;
import famicom.api.memory.ChrMapper;
import famicom.api.memory.PrgMapper;
import famicom.api.memory.file.NesRomFile;

import java.io.FileNotFoundException;

/**
 * Created by hkoba on 2017/01/14.
 */
@FamicomRom(name = "エミュレータ", packages = "emu", mirror = FamicomRom.MirrorMode.VERTICAL)
public class NesRom {
    @Attach
    protected ChrMapper chrMapper;

    @Attach
    protected PrgMapper prgMapper;

    @Initialize
    public void init() {
        NesRomFile romFile = new NesRomFile();
        try {
            romFile.loadData("/Users/hkoba/Documents/ROM/LodeRunner.nes");
            //romFile.loadData("/Users/hkoba/Documents/ROM/DQ1.nes");
            prgMapper.entryBank(romFile.getData("PRG0"));
            chrMapper.entryBank(romFile.getData("CHR0"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
