package famicom.api.memory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * エリア分割しているメモリ
 * Created by hkoba on 2017/01/09.
 */
public class SplitMemoryAccessor<T extends SplitMemoryAccessor> extends MemoryAccessor<T> {
    protected class AccessMemory {
        protected int startAddr;
        protected int endAddr;

        protected AccessMemory(int start, int end) {
            startAddr = start;
            endAddr = end;
        }
        protected void write(int offset, byte[] data, int start, int size) {
            for (int i = 0; i < size; i++) {
                write(offset + i, data[start + i] & 255);
            }
        }
        protected void write(int offset, int val) {
            SplitMemoryAccessor.super.write(startAddr + offset, val);
        }
        protected void read(int offset, byte[] data, int start, int size) {
            for (int i = 0; i < size; i++) {
                data[start + i] = (byte)read(offset + i);
            }
        }
        protected int read(int offset) {
            return SplitMemoryAccessor.super.read(startAddr + offset);
        }
    }
    protected abstract class DelegateAccessMemory extends AccessMemory {
        protected DelegateAccessMemory(int start, int end) {
            super(start, end);
        }

        @Override
        protected void write(int offset, byte[] data, int start, int size) {
            getMemoryAccessor().write(offset, offset + size, data, start);
        }

        @Override
        protected void write(int offset, int val) {
            getMemoryAccessor().write(offset, val);
        }

        @Override
        protected void read(int offset, byte[] data, int start, int size) {
            getMemoryAccessor().read(offset, offset + size, data, start);
        }

        @Override
        protected int read(int offset) {
            return getMemoryAccessor().read(offset);
        }

        protected abstract MemoryAccessor getMemoryAccessor();
    }
    protected class SlideAccessMemory extends AccessMemory {
        protected int startOffset;

        protected SlideAccessMemory(int start, int end, int offset) {
            super(start, end);
        }

        @Override
        protected void write(int offset, byte[] data, int start, int size) {
            SplitMemoryAccessor.super.write(startOffset + offset, startOffset + offset + size, data, start);
        }

        @Override
        protected void write(int offset, int val) {
            SplitMemoryAccessor.super.write(startOffset + offset, val);
        }

        @Override
        protected void read(int offset, byte[] data, int start, int size) {
            SplitMemoryAccessor.super.read(startOffset + offset, startOffset + offset + size, data, start);
        }

        @Override
        protected int read(int offset) {
            return SplitMemoryAccessor.super.read(startOffset + offset);
        }
    }

    protected List<AccessMemory> accessMemoryList = new ArrayList<>();

    protected SplitMemoryAccessor(int size) {
        super(size);
    }

    @Override
    public T write(int startAddr, int endAddr, byte[] buf, int srcIndex) {
        for (AccessMemory accessMemory: accessMemoryList) {
            if (startAddr < accessMemory.startAddr) {
                // 前で実行する
                int ed = endAddr;
                if (ed > accessMemory.startAddr) {
                    ed = accessMemory.startAddr;
                }
                super.write(startAddr, ed, buf, srcIndex);
                srcIndex += (ed - startAddr);
                startAddr = ed;
                if (startAddr >= endAddr) {
                    break;
                }
            }
            if (startAddr < accessMemory.endAddr) {
                // アクセスする
                int ed = endAddr;
                if (ed > accessMemory.endAddr) {
                    ed = accessMemory.endAddr;
                }
                accessMemory.write(startAddr - accessMemory.startAddr, buf, srcIndex, ed - startAddr);
                updateMemory(startAddr, ed);
                srcIndex += (ed - startAddr);
                startAddr = ed;
                if (startAddr >= endAddr) {
                    break;
                }
            }
        }
        if (startAddr < endAddr) {
            super.write(startAddr, endAddr, buf, srcIndex);
        }
        return (T) this;
    }

    @Override
    public T read(int startAddr, int endAddr, byte[] buf, int destIndex) {
        for (AccessMemory accessMemory: accessMemoryList) {
            if (startAddr < accessMemory.startAddr) {
                // 前で実行する
                int ed = endAddr;
                if (ed > accessMemory.startAddr) {
                    ed = accessMemory.startAddr;
                }
                super.read(startAddr, ed, buf, destIndex);
                destIndex += (ed - startAddr);
                startAddr = ed;
                if (startAddr >= endAddr) {
                    break;
                }
            }
            if (startAddr < accessMemory.endAddr) {
                // アクセスする
                int ed = endAddr;
                if (ed > accessMemory.endAddr) {
                    ed = accessMemory.endAddr;
                }
                accessMemory.read(startAddr - accessMemory.startAddr, buf, destIndex, ed - startAddr);
                destIndex += (ed - startAddr);
                startAddr = ed;
                if (startAddr >= endAddr) {
                    break;
                }
            }
        }
        if (startAddr < endAddr) {
            super.write(startAddr, endAddr, buf, destIndex);
        }
        return (T) this;
    }

    @Override
    public T write(int addr, int val) {
        for (AccessMemory accessMemory: accessMemoryList) {
            if (addr >= accessMemory.startAddr && addr < accessMemory.endAddr) {
                accessMemory.write(addr - accessMemory.startAddr, val);
                updateMemory(addr, addr + 1);
                return (T)this;
            }
        }
        return super.write(addr, val);
    }

    @Override
    public int read(int addr) {
        for (AccessMemory accessMemory: accessMemoryList) {
            if (addr >= accessMemory.startAddr && addr < accessMemory.endAddr) {
                return accessMemory.read(addr - accessMemory.startAddr);
            }
        }
        return super.read(addr);
    }

    protected SplitMemoryAccessor entryAccess(AccessMemory accessMemory) {
        accessMemoryList.add(accessMemory);
        accessMemoryList.sort(new Comparator<AccessMemory>() {
            @Override
            public int compare(AccessMemory o1, AccessMemory o2) {
                if (o1.startAddr < o2.startAddr) {
                    return -1;
                } else if (o1.startAddr > o2.startAddr) {
                    return 1;
                }
                return 0;
            }
        });
        return this;
    }
}
