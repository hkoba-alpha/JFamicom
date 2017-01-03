package famicom.api.ppu;

import java.util.List;

/**
 * Created by hkoba on 2017/01/03.
 */
public interface IChrRom {
    List<ChrBankData> loadData(String[] args, int ppuAddr, int size);
}
