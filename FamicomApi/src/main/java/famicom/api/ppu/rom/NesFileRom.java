package famicom.api.ppu.rom;

import famicom.api.core.ExecuteManager;
import famicom.api.ppu.ChrBankData;
import famicom.api.ppu.IChrRom;

import java.io.*;
import java.util.List;

/**
 * Created by hkoba on 2017/01/03.
 */
public class NesFileRom implements IChrRom {
    @Override
    public List<ChrBankData> loadData(String[] args, int ppuAddr, int size) {
        InputStream fis = null;
        try {
            File f = new File(args[0]);
            if (f.isFile()) {
                fis = new FileInputStream(f);
            } else {
                fis = ExecuteManager.getInstance().getRomClass().getClassLoader().getResourceAsStream(args[0]);
                if (fis == null) {
                    throw new FileNotFoundException(args[0]);
                }
            }
            byte[] data = new byte[16];
            fis.read(data);
            int mapper = ((data[6] >> 4) & 15) | (data[7] & 0xf0);
            System.out.println("mapper=" + mapper);
            int dx = data[4] * 0x4000;
            if (data[5] > 0) {
                fis.skip(dx);
                int sz = 0x4000 * data[5];
                byte[] pattern = new byte[sz];
                fis.read(pattern);
                return ChrBankData.makeBankData(pattern, ppuAddr, size);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // 無視
                }
            }
        }

        return null;
    }
}
