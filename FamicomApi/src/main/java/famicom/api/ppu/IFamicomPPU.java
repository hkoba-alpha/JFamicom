package famicom.api.ppu;

import famicom.api.annotation.FamicomApi;

/**
 * Created by hkoba on 2017/01/01.
 */
@FamicomApi
public interface IFamicomPPU {
    ControlData getControlData();
    SpriteData getSpriteData(int index);
    NameTable getNameTable(int index);
    PatternTable getPatternTable(int index);
    PaletteTable getPaletteTable();
}
