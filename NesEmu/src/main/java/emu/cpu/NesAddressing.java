package emu.cpu;

import famicom.api.memory.FamicomMemory;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by hkoba on 2017/01/28.
 */
public abstract class NesAddressing implements IOpecode {
    /**
     * 命令を実行中のデータ
     */
    public static class OperationData {
        public int startPc;
        public int nextPc;
        public NesCpu cpu;
        public FamicomMemory memory;
        public boolean pageCross;
        public int address;
        public int data;

        public int value() {
            if (address < 0) {
                return data;
            }
            int ret = memory.read(address);
            cpu.log(l -> l.append(" ($").append(Integer.toString(address, 16)).append(":").append(Integer.toString(ret, 16) + ")"));
            return ret;
        }
        public void store(int value) {
            cpu.log(l -> l.append(" ($").append(Integer.toString(address, 16)).append(")=#").append(Integer.toString(value, 16)));
            memory.write(address, value);
        }

        @Override
        public String toString() {
            StringBuilder ret = new StringBuilder();
            if (address >= 0) {
                ret.append(" addr=$").append(Integer.toString(address, 16));
            }
            if (data >= 0) {
                ret.append(" data=#").append(Integer.toString(data, 16));
            }
            return ret.toString();
        }
    }
    private static OperationData operationData = new OperationData();
    private static OperationData getOperationData(NesCpu cpu, int size) {
        operationData.startPc = cpu.getPC().getValue();
        operationData.nextPc = operationData.startPc + size;
        operationData.cpu = cpu;
        operationData.memory = cpu.famicomMemory;
        operationData.address = -1;
        operationData.data = -1;
        operationData.pageCross = false;
        return operationData;
    }

    private String name;

    private NesAddressing(String str) {
        name = str;
    }

    private void log(StringBuilder log, OperationData data) {
        log.append(name).append(logOperand(data));
        while (log.length() < 30) {
            log.append(' ');
        }
        log.append(String.format(": A:%02X X:%02X Y:%02X S:%02X P:%s",
                data.cpu.getA().getValue(), data.cpu.getX().getValue(), data.cpu.getY().getValue(),
                data.cpu.getSP().getValue(), data.cpu.getPS().toString()));
    }

    protected abstract String logOperand(OperationData data);

    public static NesAddressing accumulator(String str, Function<OperationData, Integer> operation) {
        return new NesAddressing(str) {
            @Override
            protected String logOperand(OperationData data) {
                return "";
            }

            @Override
            public int execute(NesCpu cpu) {
                OperationData data = NesAddressing.getOperationData(cpu, 1);
                cpu.log(l -> super.log(l, data));
                int result = operation.apply(data);
                cpu.getPC().setValue(data.nextPc);
                return result;
            }
        };
    }

    public static NesAddressing immediate(String str, Function<OperationData, Integer> operation) {
        return new NesAddressing(str) {
            @Override
            protected String logOperand(OperationData data) {
                return String.format(" #$%02X", data.data);
            }

            @Override
            public int execute(NesCpu cpu) {
                OperationData data = NesAddressing.getOperationData(cpu, 2);
                data.data = data.memory.read(data.startPc + 1);
                cpu.log(l -> super.log(l, data));
                int result = operation.apply(data);
                cpu.getPC().setValue(data.nextPc);
                return result;
            }
        };
    }

    public static NesAddressing absolute(String str, Function<OperationData, Integer> operation) {
        return new NesAddressing(str) {
            @Override
            protected String logOperand(OperationData data) {
                return String.format(" $%04X", data.address);
            }

            @Override
            public int execute(NesCpu cpu) {
                OperationData data = NesAddressing.getOperationData(cpu, 3);
                data.address = data.memory.read(data.startPc + 1) | (data.memory.read(data.startPc + 2) << 8);
                cpu.log(l -> super.log(l, data));
                int result = operation.apply(data);
                cpu.getPC().setValue(data.nextPc);
                return result;
            }
        };
    }

    public static NesAddressing zeroPage(String str, Function<OperationData, Integer> operation) {
        return new NesAddressing(str) {
            @Override
            protected String logOperand(OperationData data) {
                return String.format(" $%02X", data.address & 0xff);
            }

            @Override
            public int execute(NesCpu cpu) {
                OperationData data = NesAddressing.getOperationData(cpu, 2);
                data.address = data.memory.read(data.startPc + 1);
                cpu.log(l -> super.log(l, data));
                int result = operation.apply(data);
                cpu.getPC().setValue(data.nextPc);
                return result;
            }
        };
    }

    public static NesAddressing indexedZeroPageX(String str, Function<OperationData, Integer> operation) {
        return new NesAddressing(str) {
            @Override
            protected String logOperand(OperationData data) {
                return String.format(" $%02X,X", data.address & 0xff);
            }

            @Override
            public int execute(NesCpu cpu) {
                OperationData data = NesAddressing.getOperationData(cpu, 2);
                data.address = (data.memory.read(data.startPc + 1) + cpu.getX().getValue()) & 0xff;
                cpu.log(l -> super.log(l, data));
                int result = operation.apply(data);
                cpu.getPC().setValue(data.nextPc);
                return result;
            }
        };
    }

    public static NesAddressing indexedZeroPageY(String str, Function<OperationData, Integer> operation) {
        return new NesAddressing(str) {
            @Override
            protected String logOperand(OperationData data) {
                return String.format(" $%02X,Y", data.address & 0xff);
            }

            @Override
            public int execute(NesCpu cpu) {
                OperationData data = NesAddressing.getOperationData(cpu, 2);
                data.address = (data.memory.read(data.startPc + 1) + cpu.getY().getValue()) & 0xff;
                cpu.log(l -> super.log(l, data));
                int result = operation.apply(data);
                cpu.getPC().setValue(data.nextPc);
                return result;
            }
        };
    }

    public static NesAddressing indexedAbsoluteX(String str, Function<OperationData, Integer> operation) {
        return new NesAddressing(str) {
            @Override
            protected String logOperand(OperationData data) {
                return String.format(" $%04X,X", data.address);
            }

            @Override
            public int execute(NesCpu cpu) {
                OperationData data = NesAddressing.getOperationData(cpu, 3);
                data.address = ((data.memory.read(data.startPc + 1) | (data.memory.read(data.startPc + 2) << 8)) + cpu.getX().getValue()) & 0xffff;
                data.pageCross = (data.startPc & 0xff) == 0xfe;
                cpu.log(l -> super.log(l, data));
                int result = operation.apply(data);
                cpu.getPC().setValue(data.nextPc);
                return result;
            }
        };
    }

    public static NesAddressing indexedAbsoluteY(String str, Function<OperationData, Integer> operation) {
        return new NesAddressing(str) {
            @Override
            protected String logOperand(OperationData data) {
                return String.format(" $%04X,Y", data.address);
            }

            @Override
            public int execute(NesCpu cpu) {
                OperationData data = NesAddressing.getOperationData(cpu, 3);
                data.address = ((data.memory.read(data.startPc + 1) | (data.memory.read(data.startPc + 2) << 8)) + cpu.getY().getValue()) & 0xffff;
                data.pageCross = (data.startPc & 0xff) == 0xfe;
                cpu.log(l -> super.log(l, data));
                int result = operation.apply(data);
                cpu.getPC().setValue(data.nextPc);
                return result;
            }
        };
    }

    public static NesAddressing relative(String str, Function<OperationData, Integer> operation) {
        return new NesAddressing(str) {
            @Override
            protected String logOperand(OperationData data) {
                return String.format(" $%04X", data.address);
            }

            @Override
            public int execute(NesCpu cpu) {
                OperationData data = NesAddressing.getOperationData(cpu, 2);
                data.address = (data.nextPc + ((byte) data.memory.read(data.startPc + 1))) & 0xffff;
                data.pageCross = (data.startPc & 0xff) == 0xfe;
                cpu.log(l -> super.log(l, data));
                int result = operation.apply(data);
                cpu.getPC().setValue(data.nextPc);
                return result;
            }
        };
    }
    public static NesAddressing indexedIndirectX(String str, Function<OperationData, Integer> operation) {
        return new NesAddressing(str) {
            @Override
            protected String logOperand(OperationData data) {
                return String.format(" ($%02X,X)", data.data);
            }

            @Override
            public int execute(NesCpu cpu) {
                OperationData data = NesAddressing.getOperationData(cpu, 2);
                data.data = data.memory.read(data.startPc + 1);
                int ptr = (data.data + cpu.getX().getValue()) & 0xff;
                data.address = data.memory.read(ptr) | (data.memory.read((ptr + 1)) << 8);
                cpu.log(l -> super.log(l, data));
                int result = operation.apply(data);
                cpu.getPC().setValue(data.nextPc);
                return result;
            }
        };
    }
    public static NesAddressing indexedIndirectY(String str, Function<OperationData, Integer> operation) {
        return new NesAddressing(str) {
            @Override
            protected String logOperand(OperationData data) {
                return String.format(" ($%02X),Y", data.data);
            }

            @Override
            public int execute(NesCpu cpu) {
                OperationData data = NesAddressing.getOperationData(cpu, 2);
                int ptr = data.memory.read(data.startPc + 1);
                data.data = ptr;
                data.address = (data.memory.read(ptr) | (data.memory.read((ptr + 1)) << 8)) + cpu.getY().getValue();
                data.pageCross = ptr == 0xff;
                cpu.log(l -> super.log(l, data));
                int result = operation.apply(data);
                cpu.getPC().setValue(data.nextPc);
                return result;
            }
        };
    }

    public static NesAddressing indirect(String str, Function<OperationData, Integer> operation) {
        return new NesAddressing(str) {
            @Override
            protected String logOperand(OperationData data) {
                return String.format(" ($%04X)", data.data);
            }

            @Override
            public int execute(NesCpu cpu) {
                OperationData data = NesAddressing.getOperationData(cpu, 3);
                int ptr = (data.memory.read(data.startPc + 1) | (data.memory.read(data.startPc + 2) << 8));
                data.data = ptr;
                data.address = data.memory.read(ptr) | (data.memory.read((ptr & 0xff00) | ((ptr + 1) & 0xff)) << 8);
                cpu.log(l -> super.log(l, data));
                int result = operation.apply(data);
                cpu.getPC().setValue(data.nextPc);
                return result;
            }
        };
    }
}
