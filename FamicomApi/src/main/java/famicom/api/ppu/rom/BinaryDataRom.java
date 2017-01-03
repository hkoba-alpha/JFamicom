package famicom.api.ppu.rom;

import famicom.api.ppu.ChrBankData;
import famicom.api.ppu.IChrRom;

import java.util.List;

/**
 * Created by hkoba on 2017/01/03.
 */
public class BinaryDataRom implements IChrRom {
    @Override
    public List<ChrBankData> loadData(String[] args, int ppuAddr, int size) {
        return null;
    }
}
