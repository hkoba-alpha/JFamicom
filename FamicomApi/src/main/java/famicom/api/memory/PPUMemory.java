package famicom.api.memory;

import famicom.api.annotation.*;
import famicom.api.ppu.IFamicomPPU;

/**
 * Created by hkoba on 2017/01/03.
 */
@FamicomLibrary(priority = 0)
public class PPUMemory extends SplitMemoryAccessor<PPUMemory> {
    @Attach
    protected IFamicomPPU famicomPPU;

    public PPUMemory() {
        super(0);
    }

    @Initialize
    protected void init() {
        entryAccess(new DelegateAccessMemory(0, 0x1000) {
            @Override
            protected MemoryAccessor getMemoryAccessor() {
                return famicomPPU.getPatternTable(0);
            }
        });
        entryAccess(new DelegateAccessMemory(0x1000, 0x2000) {
            @Override
            protected MemoryAccessor getMemoryAccessor() {
                return famicomPPU.getPatternTable(1);
            }
        });
        for (int addr = 0x2000; addr < 0x3f00; addr += 0x400) {
            int ed = addr + 0x400;
            if (addr == 0x3c00) {
                ed = 0x3f00;
            }
            final int ix = (addr >> 18) & 3;
            entryAccess(new DelegateAccessMemory(addr, ed) {
                @Override
                protected MemoryAccessor getMemoryAccessor() {
                    return famicomPPU.getNameTable(ix);
                }
            });
        }
        entryAccess(new DelegateAccessMemory(0x3f00, 0x4000) {
            @Override
            protected MemoryAccessor getMemoryAccessor() {
                return famicomPPU.getPaletteTable();
            }
        });
    }
}
