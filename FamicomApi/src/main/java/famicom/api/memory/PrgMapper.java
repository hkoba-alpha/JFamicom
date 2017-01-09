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
public class PrgMapper extends MemoryAccessor<PrgMapper> {

    protected List<byte[]> bankDataList = new ArrayList<>();

    public PrgMapper() {
        super(0x8000);
    }

    public PrgMapper selectBank(int bank) {
        if (bank >= 0 && bank < bankDataList.size()) {
            byte[] rom = bankDataList.get(bank);
            System.arraycopy(rom, 0, super.memoryData, 0, rom.length > 0x4000 ? 0x4000: rom.length);
        }
        return this;
    }

    /**
     * 初期化時に設定されている
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
        for (PrgRom rom: ExecuteManager.getInstance().getRomClass().getAnnotationsByType(PrgRom.class)) {
            System.out.println(rom);
            try {
                AbstractMemoryFile memoryFile = rom.type().newInstance();
                memoryFile.loadData(rom.fileName());
                if (rom.names().length > 0) {
                    for (String name: rom.names()) {
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
        if (bankDataList.size() > 0) {
            byte[] rom = bankDataList.get(bankDataList.size() - 1);
            System.arraycopy(rom, 0, super.memoryData, 0x4000, rom.length > 0x4000 ? 0x4000: rom.length);
        }
        selectBank(0);
    }
}
