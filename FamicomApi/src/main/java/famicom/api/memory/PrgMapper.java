package famicom.api.memory;

import famicom.api.annotation.FamicomLibrary;
import famicom.api.annotation.Initialize;
import famicom.api.annotation.PostReset;
import famicom.api.annotation.PrgRom;
import famicom.api.core.ExecuteManager;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * プログラムROMのマッパー管理
 * Created by hkoba on 2017/01/09.
 */
@FamicomLibrary(priority = 0)
public class PrgMapper extends SplitMemoryAccessor<PrgMapper> {
    protected class BankMemory extends MemoryAccessor<BankMemory> {
        protected int offset;

        protected BankMemory() {
            super(0x4000);
        }

        protected void selectPage(int page) {
            offset = (page & 1) > 0 ? 0x2000 : 0;
            super.memoryData = bankDataList.get((page >> 1) % bankDataList.size());
        }

        @Override
        public BankMemory read(int startAddr, int endAddr, byte[] buf, int destIndex) {
            return super.read(startAddr + offset, endAddr + offset, buf, destIndex);
        }

        @Override
        public BankMemory read(int startAddr, int endAddr, byte[] buf) {
            return super.read(startAddr + offset, endAddr + offset, buf);
        }

        @Override
        public int read(int addr) {
            return super.read(addr + offset);
        }
    }

    protected List<byte[]> bankDataList = new ArrayList<>();
    protected BankMemory[] bankMemoryList = new BankMemory[4];

    public PrgMapper() {
        super(0);
        for (int i = 0; i < bankMemoryList.length; i++) {
            BankMemory memory = new BankMemory();
            bankMemoryList[i] = memory;
            entryAccess(new DelegateAccessMemory(i * 0x2000, (i + 1) * 0x2000) {
                @Override
                protected MemoryAccessor getMemoryAccessor() {
                    return memory;
                }
            });
        }
    }

    /**
     * ページを切り替える
     *
     * @param page    0-3: 0x2000区切りのページ
     * @param romPage
     * @return
     */
    public PrgMapper selectPage(int page, int romPage) {
        bankMemoryList[page & 3].selectPage(romPage);
        /*
        if (bankDataList.size() > 0) {
            int offset = (romPage & 1) * 0x2000;
            int bankIx = (romPage >> 1) % bankDataList.size();
            byte[] rom = bankDataList.get(bankIx);
            System.arraycopy(rom, offset, super.memoryData, (page & 3) * 0x2000, 0x2000);
        }
        */
        return this;
    }

    public int getBankSize() {
        return bankDataList.size();
    }

    /**
     * 初期化時に設定されている
     *
     * @param data
     * @return
     */
    public int entryBank(byte[] data) {
        int ret = bankDataList.size();
        if (data != null) {
            bankDataList.add(data);
        }
        return ret;
    }


    @Initialize
    protected void init() {
        for (PrgRom rom : ExecuteManager.getInstance().getRomClass().getAnnotationsByType(PrgRom.class)) {
            System.out.println(rom);
            try {
                AbstractMemoryFile memoryFile = rom.type().newInstance();
                memoryFile.loadData(rom.fileName());
                if (rom.names().length > 0) {
                    for (String name : rom.names()) {
                        entryBank(memoryFile.getData(name));
                    }
                } else {
                    // CHRを全て登録する
                    memoryFile.nameMap.keySet().stream().sorted().forEach(v -> {
                        entryBank(memoryFile.getData((String) v));
                    });
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public PrgMapper write(int startAddr, int endAddr, byte[] buf, int srcIndex) {
        return this;
    }

    @Override
    public PrgMapper write(int addr, int val) {
        return this;
    }

    @PostReset
    protected void reset() {
        selectPage(0, 0).selectPage(1, 1).selectPage(2, 2).selectPage(3, 3);
    }
}
