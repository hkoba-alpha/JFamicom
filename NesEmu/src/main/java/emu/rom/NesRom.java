package emu.rom;

import famicom.api.annotation.Attach;
import famicom.api.annotation.FamicomRom;
import famicom.api.annotation.Initialize;
import famicom.api.memory.ChrMapper;
import famicom.api.memory.PrgMapper;
import famicom.api.memory.file.NesRomFile;
import famicom.api.ppu.IFamicomPPU;

import java.io.FileNotFoundException;

/**
 * Created by hkoba on 2017/01/14.
 */
@FamicomRom(name = "エミュレータ", packages = "emu", mirror = FamicomRom.MirrorMode.VERTICAL)
public class NesRom {
    @Attach
    protected ChrMapper chrMapper;

    @Attach
    protected NesMapperMemory prgMapper;

    @Attach
    protected IFamicomPPU famicomPPU;

    @Initialize
    public void init() {
        NesRomFile romFile = new NesRomFile();
        try {
            //romFile.loadData("/Users/hkoba/Documents/ROM/LodeRunner.nes");
            //romFile.loadData("/Users/hkoba/Documents/ROM/Champ.nes");
            //romFile.loadData("/Users/hkoba/Documents/ROM/DQ1.nes");
            //romFile.loadData("/Users/hkoba/Documents/ROM/DQ2.nes");
            //romFile.loadData("/Users/hkoba/Documents/ROM/DQ3.nes");
            romFile.loadData("/Users/hkoba/Documents/ROM/DQ4.nes");
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
