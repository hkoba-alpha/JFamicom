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
        public final int startPc;
        public int nextPc;
        public final NesCpu cpu;
        public FamicomMemory memory;
        public boolean pageCross;
        public int address;

        OperationData(NesCpu cpu, int size) {
            startPc = cpu.getPC().getValue();
            nextPc = startPc + size;
            this.cpu = cpu;
            memory = cpu.famicomMemory;
        }
    }

    public static NesAddressing accumulator(Function<OperationData, Integer> operation) {
        return new NesAddressing() {
            @Override
            public int execute(NesCpu cpu) {
                OperationData data = new OperationData(cpu, 1);
                int result = operation.apply(data);
                cpu.getPC().setValue(data.nextPc);
                return result;
            }
        };
    }

    public static NesAddressing immediate(BiFunction<Integer, OperationData, Integer> operation) {
        return new NesAddressing() {
            @Override
            public int execute(NesCpu cpu) {
                OperationData data = new OperationData(cpu, 2);
                int value = data.memory.read(data.startPc + 1);
                int result = operation.apply(value, data);
                cpu.getPC().setValue(data.nextPc);
                return result;
            }
        };
    }

    public static NesAddressing absolute(BiFunction<Integer, OperationData, Integer> operation) {
        return new NesAddressing() {
            @Override
            public int execute(NesCpu cpu) {
                OperationData data = new OperationData(cpu, 3);
                data.address = data.memory.read(data.startPc + 1) | (data.memory.read(data.startPc + 2) << 8);
                int value = data.memory.read(data.address);
                int result = operation.apply(value, data);
                cpu.getPC().setValue(data.nextPc);
                return result;
            }
        };
    }

    public static NesAddressing zeroPage(BiFunction<Integer, OperationData, Integer> operation) {
        return new NesAddressing() {
            @Override
            public int execute(NesCpu cpu) {
                OperationData data = new OperationData(cpu, 2);
                data.address = data.memory.read(data.startPc + 1);
                int value = data.memory.read(data.address);
                int result = operation.apply(value, data);
                cpu.getPC().setValue(data.nextPc);
                return result;
            }
        };
    }

    public static NesAddressing indexedZeroPageX(BiFunction<Integer, OperationData, Integer> operation) {
        return new NesAddressing() {
            @Override
            public int execute(NesCpu cpu) {
                OperationData data = new OperationData(cpu, 2);
                data.address = (data.memory.read(data.startPc + 1) + cpu.getX().getValue()) & 0xff;
                int value = data.memory.read(data.address);
                int result = operation.apply(value, data);
                cpu.getPC().setValue(data.nextPc);
                return result;
            }
        };
    }

    public static NesAddressing indexedZeroPageY(BiFunction<Integer, OperationData, Integer> operation) {
        return new NesAddressing() {
            @Override
            public int execute(NesCpu cpu) {
                OperationData data = new OperationData(cpu, 2);
                data.address = (data.memory.read(data.startPc + 1) + cpu.getY().getValue()) & 0xff;
                int value = data.memory.read(data.address);
                int result = operation.apply(value, data);
                cpu.getPC().setValue(data.nextPc);
                return result;
            }
        };
    }

    public static NesAddressing indexedAbsoluteX(BiFunction<Integer, OperationData, Integer> operation) {
        return new NesAddressing() {
            @Override
            public int execute(NesCpu cpu) {
                OperationData data = new OperationData(cpu, 3);
                data.address = ((data.memory.read(data.startPc + 1) | (data.memory.read(data.startPc + 2) << 8)) + cpu.getX().getValue()) & 0xffff;
                int value = data.memory.read(data.address);
                int result = operation.apply(value, data);
                data.pageCross = (data.startPc & 0xff) == 0xfe;
                cpu.getPC().setValue(data.nextPc);
                return result;
            }
        };
    }

    public static NesAddressing indexedAbsoluteY(BiFunction<Integer, OperationData, Integer> operation) {
        return new NesAddressing() {
            @Override
            public int execute(NesCpu cpu) {
                OperationData data = new OperationData(cpu, 3);
                data.address = ((data.memory.read(data.startPc + 1) | (data.memory.read(data.startPc + 2) << 8)) + cpu.getY().getValue()) & 0xffff;
                int value = data.memory.read(data.address);
                int result = operation.apply(value, data);
                data.pageCross = (data.startPc & 0xff) == 0xfe;
                cpu.getPC().setValue(data.nextPc);
                return result;
            }
        };
    }

    public static NesAddressing relative(BiFunction<Integer, OperationData, Integer> operation) {
        return new NesAddressing() {
            @Override
            public int execute(NesCpu cpu) {
                OperationData data = new OperationData(cpu, 2);
                int value = data.nextPc + ((byte) data.memory.read(data.startPc + 1));
                int result = operation.apply(value & 0xffff, data);
                data.pageCross = (data.startPc & 0xff) == 0xfe;
                cpu.getPC().setValue(data.nextPc);
                return result;
            }
        };
    }
    public static NesAddressing indexedIndirectX(BiFunction<Integer, OperationData, Integer> operation) {
        return new NesAddressing() {
            @Override
            public int execute(NesCpu cpu) {
                OperationData data = new OperationData(cpu, 2);
                int ptr = (data.memory.read(data.startPc + 1) + cpu.getX().getValue()) & 0xff;
                data.address = data.memory.read(ptr) | (data.memory.read((ptr + 1)) << 8);
                int value = data.memory.read(data.address);
                int result = operation.apply(value, data);
                cpu.getPC().setValue(data.nextPc);
                return result;
            }
        };
    }
    public static NesAddressing indexedIndirectY(BiFunction<Integer, OperationData, Integer> operation) {
        return new NesAddressing() {
            @Override
            public int execute(NesCpu cpu) {
                OperationData data = new OperationData(cpu, 2);
                int ptr = data.memory.read(data.startPc + 1);
                data.address = (data.memory.read(ptr) | (data.memory.read((ptr + 1)) << 8)) + cpu.getY().getValue();
                int value = data.memory.read(data.address);
                int result = operation.apply(value, data);
                data.pageCross = ptr == 0xff;
                cpu.getPC().setValue(data.nextPc);
                return result;
            }
        };
    }

    public static NesAddressing indirect(BiFunction<Integer, OperationData, Integer> operation) {
        return new NesAddressing() {
            @Override
            public int execute(NesCpu cpu) {
                OperationData data = new OperationData(cpu, 3);
                int ptr = (data.memory.read(data.startPc + 1) | (data.memory.read(data.startPc + 2) << 8)) + cpu.getY().getValue();
                int value = data.memory.read(ptr) | (data.memory.read((ptr & 0xff00) | ((ptr + 1) & 0xff)) << 8);
                int result = operation.apply(value, data);
                cpu.getPC().setValue(data.nextPc);
                return result;
            }
        };
    }
}
