package famicom.api.ppu;

import famicom.api.annotation.*;
import famicom.api.core.ExecuteManager;
import famicom.api.core.MemoryAccessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hkoba on 2017/01/03.
 */
@FamicomLibrary(priority = 0)
public class PPUMemory extends MemoryAccessor<PPUMemory> {
    @Attach
    protected IFamicomPPU famicomPPU;

    protected List<ChrBankData> chrBankDataList = new ArrayList<>();

    public PPUMemory() {
        super(0);
    }

    @Override
    public PPUMemory write(int startAddr, int endAddr, byte[] buf, int srcIndex) {
        int size = endAddr - startAddr;
        if (startAddr < 0 || size <= 0) {
            return this;
        }
        if (startAddr < 0x1000) {
            int ed = 0x1000;
            if (endAddr < ed) {
                ed = endAddr;
            }
            famicomPPU.getPatternTable(0).write(startAddr & 0xfff, ed, buf, srcIndex);
            srcIndex += (ed - startAddr);
            startAddr = ed;
            if (startAddr >= endAddr) {
                return this;
            }
        }
        if (startAddr < 0x2000) {
            int ed = 0x2000;
            if (endAddr < ed) {
                ed = endAddr;
            }
            famicomPPU.getPatternTable(1).write(startAddr & 0xfff, ed, buf, srcIndex);
            srcIndex += (ed - startAddr);
            startAddr = ed;
            if (startAddr >= endAddr) {
                return this;
            }
        }
        if (startAddr < 0x3f00) {
            for (int addr = 0x2000; addr < 0x3f00; addr += 0x400) {
                int ed = addr + 0x400;
                if (addr == 0x3c00) {
                    ed = 0x3f00;
                }
                if (startAddr < ed) {
                    if (endAddr < ed) {
                        ed = endAddr;
                    }
                    famicomPPU.getNameTable((addr >> 18) & 3).write(startAddr & 0x3ff, ed, buf, srcIndex);
                    srcIndex += (ed - startAddr);
                    startAddr = ed;
                    if (startAddr >= endAddr) {
                        return this;
                    }
                }
            }
        }
        if (startAddr < 0x4000) {
            for (int addr = 0x3f00; addr < 0x4000; addr += 0x20) {
                int ed = addr + 0x20;
                if (startAddr < ed) {
                    if (endAddr < ed) {
                        ed = endAddr;
                    }
                    famicomPPU.getPaletteTable().write(startAddr & 0x1f, ed, buf, srcIndex);
                    srcIndex += (ed - startAddr);
                    startAddr = ed;
                    if (startAddr >= endAddr) {
                        return this;
                    }
                }
            }
        }
        return this;
    }

    @Override
    public PPUMemory write(int startAddr, int endAddr, byte[] buf) {
        return write(startAddr, endAddr, buf, 0);
    }

    @Override
    public PPUMemory read(int startAddr, int endAddr, byte[] buf, int destIndex) {
        int size = endAddr - startAddr;
        if (startAddr < 0 || size <= 0) {
            return this;
        }
        if (startAddr < 0x1000) {
            int ed = 0x1000;
            if (endAddr < ed) {
                ed = endAddr;
            }
            famicomPPU.getPatternTable(0).read(startAddr & 0xfff, ed, buf, destIndex);
            destIndex += (ed - startAddr);
            startAddr = ed;
            if (startAddr >= endAddr) {
                return this;
            }
        }
        if (startAddr < 0x2000) {
            int ed = 0x2000;
            if (endAddr < ed) {
                ed = endAddr;
            }
            famicomPPU.getPatternTable(1).write(startAddr & 0xfff, ed, buf, destIndex);
            destIndex += (ed - startAddr);
            startAddr = ed;
            if (startAddr >= endAddr) {
                return this;
            }
        }
        if (startAddr < 0x3f00) {
            for (int addr = 0x2000; addr < 0x3f00; addr += 0x400) {
                int ed = addr + 0x400;
                if (addr == 0x3c00) {
                    ed = 0x3f00;
                }
                if (startAddr < ed) {
                    if (endAddr < ed) {
                        ed = endAddr;
                    }
                    famicomPPU.getNameTable((addr >> 18) & 3).write(startAddr & 0x3ff, ed, buf, destIndex);
                    destIndex += (ed - startAddr);
                    startAddr = ed;
                    if (startAddr >= endAddr) {
                        return this;
                    }
                }
            }
        }
        if (startAddr < 0x4000) {
            for (int addr = 0x3f00; addr < 0x4000; addr += 0x20) {
                int ed = addr + 0x20;
                if (startAddr < ed) {
                    if (endAddr < ed) {
                        ed = endAddr;
                    }
                    famicomPPU.getPaletteTable().write(startAddr & 0x1f, ed, buf, destIndex);
                    destIndex += (ed - startAddr);
                    startAddr = ed;
                    if (startAddr >= endAddr) {
                        return this;
                    }
                }
            }
        }
        return this;
    }

    @Override
    public PPUMemory read(int startAddr, int endAddr, byte[] buf) {
        return read(startAddr, endAddr, buf, 0);
    }

    @Override
    public PPUMemory write(int addr, int val) {
        if (addr < 0 || addr >= 0x4000) {
            return this;
        }
        if (addr < 0x1000) {
            famicomPPU.getPatternTable(0).write(addr, val);
        } else if (addr < 0x2000) {
            famicomPPU.getPatternTable(1).write(addr & 0xfff, val);
        } else if (addr < 0x3f00) {
            famicomPPU.getNameTable((addr >> 18) & 3).write(addr & 0x3ff, val);
        } else {
            famicomPPU.getPaletteTable().write(addr & 0x3f, val);
        }
        return this;
    }

    @Override
    public int read(int addr) {
        if (addr < 0 || addr >= 0x4000) {
            return 0;
        }
        if (addr < 0x1000) {
            return famicomPPU.getPatternTable(0).read(addr);
        } else if (addr < 0x2000) {
            return famicomPPU.getPatternTable(1).read(addr & 0xfff);
        } else if (addr < 0x3f00) {
            return famicomPPU.getNameTable((addr >> 18) & 3).read(addr & 0x3ff);
        } else {
            return famicomPPU.getPaletteTable().read(addr & 0x3f);
        }
    }

    @Initialize
    private void init() {
        for (ChrRom rom: ExecuteManager.getInstance().getRomClass().getAnnotationsByType(ChrRom.class)) {
            System.out.println(rom);
            try {
                List<ChrBankData> list = rom.type().newInstance().loadData(rom.args(), rom.ppuAddr(), rom.size());
                if (list != null) {
                    chrBankDataList.addAll(list);
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        changeBank(0);
    }

    @PreReset
    private void preReset() {
        changeBank(0);
    }

    public List<ChrBankData> getChrBankDataList() {
        return chrBankDataList;
    }

    public PPUMemory changeBank(int index) {
        if (chrBankDataList.size() == 0) {
            return this;
        }
        if (index < 0 || index >= chrBankDataList.size()) {
            index = 0;
        }
        ChrBankData bankData = chrBankDataList.get(index);
        write(bankData.getPpuAddr(), bankData.getPpuAddr() + bankData.getSize(), bankData.getData());
        return this;
    }
}
