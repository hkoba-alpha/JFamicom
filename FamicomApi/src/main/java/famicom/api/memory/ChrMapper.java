package famicom.api.memory;

import famicom.api.annotation.*;
import famicom.api.core.ExecuteManager;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * プログラムROMのマッパー管理
 * Created by hkoba on 2017/01/09.
 */
@FamicomLibrary(priority = 0)
public class ChrMapper {
    @Attach
    protected PPUMemory ppuMemory;

    protected List<byte[]> bankDataList = new ArrayList<>();

    public ChrMapper selectBank(int bank) {
        if (bank >= 0 && bank < bankDataList.size()) {
            byte[] rom = bankDataList.get(bank);
            ppuMemory.write(0, rom.length > 0x2000 ? 0x2000 : rom.length, rom);
        }
        return this;
    }

    /**
     * バンク切り替え
     *
     * @param page    0-7: 0x400のページ
     * @param romPage
     * @return
     */
    public ChrMapper selectPage(int page, int romPage) {
        if (bankDataList.size() > 0) {
            int offset = (romPage & 7) * 0x400;
            int bankIx = (romPage >> 3) % bankDataList.size();
            byte[] rom = bankDataList.get(bankIx);
            ppuMemory.write(page * 0x400, (page + 1) * 0x400, rom, offset);
        }
        return this;
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
        for (ChrRom rom : ExecuteManager.getInstance().getRomClass().getAnnotationsByType(ChrRom.class)) {
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
                    if (bankDataList.size() == 0) {
                        // 名前がなかったので全てを登録する
                        entryBank(memoryFile.getData());
                    }
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

    @PostReset
    protected void reset() {
        selectBank(0);
    }
}
