package famicom.api.ppu;

import famicom.api.memory.MemoryAccessor;

/**
 * Created by hkoba on 2017/01/01.
 */
public class PatternTable extends MemoryAccessor<PatternTable> {
    protected PatternTable() {
        super(256 * 16);
    }
}
