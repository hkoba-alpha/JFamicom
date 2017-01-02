package famicom.api.ppu;

import famicom.api.core.MemoryAccessor;

import java.util.Arrays;

/**
 * Created by hkoba on 2017/01/01.
 */
public class PatternTable extends MemoryAccessor<PatternTable> {
    protected PatternTable() {
        super(256 * 16);
    }
}
