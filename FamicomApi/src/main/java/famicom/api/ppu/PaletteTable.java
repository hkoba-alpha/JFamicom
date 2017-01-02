package famicom.api.ppu;

import famicom.api.core.MemoryAccessor;

/**
 * Created by hkoba on 2017/01/01.
 */
public class PaletteTable extends MemoryAccessor<PaletteTable> {
    protected PaletteTable() {
        super(32);
    }
}
