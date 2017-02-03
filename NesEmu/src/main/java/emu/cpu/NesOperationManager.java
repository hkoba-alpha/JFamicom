package emu.cpu;

import famicom.api.annotation.Attach;
import famicom.api.annotation.FamicomApplication;
import famicom.api.annotation.HBlank;
import famicom.api.annotation.Initialize;
import famicom.api.state.ScanState;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Created by hkoba on 2017/01/28.
 */
@FamicomApplication
public class NesOperationManager implements IOpecodeManager {
    private Map<Integer, NesAddressing> operationMap = new HashMap<>();

    private int adc(int value, NesAddressing.OperationData data, int clk) {
        return clk + (data.pageCross ? 1 : 0);
    }

    private int sbc(int value, NesAddressing.OperationData data, int clk) {
        return clk + (data.pageCross ? 1 : 0);
    }

    private int and(int value, NesAddressing.OperationData data, int clk) {
        return clk + (data.pageCross ? 1 : 0);
    }

    private int ora(int value, NesAddressing.OperationData data, int clk) {
        return clk + (data.pageCross ? 1 : 0);
    }

    private int eor(int value, NesAddressing.OperationData data, int clk) {
        return clk + (data.pageCross ? 1 : 0);
    }

    private int asl(int value, NesAddressing.OperationData data, int clk) {
        return clk + (data.pageCross ? 1 : 0);
    }

    private int lsr(int value, NesAddressing.OperationData data, int clk) {
        return clk + (data.pageCross ? 1 : 0);
    }

    private int rol(int value, NesAddressing.OperationData data, int clk) {
        return clk + (data.pageCross ? 1 : 0);
    }

    private int ror(int value, NesAddressing.OperationData data, int clk) {
        return clk + (data.pageCross ? 1 : 0);
    }

    private int cmp(NesRegister reg, int value, NesAddressing.OperationData data, int clk) {
        if (reg.getValue() == value) {

        }
        return clk + (data.pageCross ? 1 : 0);
    }

    private int bit(int value, NesAddressing.OperationData data, int clk) {
        return clk + (data.pageCross ? 1 : 0);
    }

    private int jmp(int addr, NesAddressing.OperationData data, int clk) {
        return clk + (data.pageCross ? 1 : 0);
    }

    private int relativeJump(int addr, NesAddressing.OperationData data, boolean condition) {
        int clk = data.pageCross ? 3 : 2;
        if (condition) {
            clk++;
            data.nextPc = addr;
        }
        return clk;
    }

    private int flagNZ(int value, NesAddressing.OperationData data) {
        data.cpu.getPS().n((value & 0x80) > 0).z((value & 0xff) == 0);
        return value;
    }
    private int load(NesRegister reg, int value, NesAddressing.OperationData data, int clk) {
        reg.setValue(flagNZ(value, data));
        return clk + (data.pageCross ? 1 : 0);
    }

    @Initialize
    public void init() {
        // ADC
        operationMap.put(0x69, NesAddressing.immediate((v, d) -> adc(v, d, 2)));
        operationMap.put(0x65, NesAddressing.zeroPage((v, d) -> adc(v, d, 3)));
        operationMap.put(0x75, NesAddressing.indexedZeroPageX((v, d) -> adc(v, d, 4)));
        operationMap.put(0x6d, NesAddressing.absolute((v, d) -> adc(v, d, 4)));
        operationMap.put(0x7d, NesAddressing.indexedAbsoluteX((v, d) -> adc(v, d, 4)));
        operationMap.put(0x79, NesAddressing.indexedAbsoluteY((v, d) -> adc(v, d, 4)));
        operationMap.put(0x61, NesAddressing.indexedIndirectX((v, d) -> adc(v, d, 6)));
        operationMap.put(0x71, NesAddressing.indexedIndirectY((v, d) -> adc(v, d, 5)));
        // SBC
        operationMap.put(0xe9, NesAddressing.immediate((v, d) -> sbc(v, d, 2)));
        operationMap.put(0xe5, NesAddressing.zeroPage((v, d) -> sbc(v, d, 3)));
        operationMap.put(0xf5, NesAddressing.indexedZeroPageX((v, d) -> sbc(v, d, 4)));
        operationMap.put(0xed, NesAddressing.absolute((v, d) -> sbc(v, d, 4)));
        operationMap.put(0xfd, NesAddressing.indexedAbsoluteX((v, d) -> sbc(v, d, 4)));
        operationMap.put(0xf9, NesAddressing.indexedAbsoluteY((v, d) -> sbc(v, d, 4)));
        operationMap.put(0xe1, NesAddressing.indexedIndirectX((v, d) -> sbc(v, d, 6)));
        operationMap.put(0xf1, NesAddressing.indexedIndirectY((v, d) -> sbc(v, d, 5)));
        // AND
        operationMap.put(0x29, NesAddressing.immediate((v, d) -> and(v, d, 2)));
        operationMap.put(0x25, NesAddressing.zeroPage((v, d) -> and(v, d, 3)));
        operationMap.put(0x35, NesAddressing.indexedZeroPageX((v, d) -> and(v, d, 4)));
        operationMap.put(0x2d, NesAddressing.absolute((v, d) -> and(v, d, 4)));
        operationMap.put(0x3d, NesAddressing.indexedAbsoluteX((v, d) -> and(v, d, 4)));
        operationMap.put(0x39, NesAddressing.indexedAbsoluteY((v, d) -> and(v, d, 4)));
        operationMap.put(0x21, NesAddressing.indexedIndirectX((v, d) -> and(v, d, 6)));
        operationMap.put(0x31, NesAddressing.indexedIndirectY((v, d) -> and(v, d, 5)));
        // ORA
        operationMap.put(0x09, NesAddressing.immediate((v, d) -> ora(v, d, 2)));
        operationMap.put(0x05, NesAddressing.zeroPage((v, d) -> ora(v, d, 3)));
        operationMap.put(0x15, NesAddressing.indexedZeroPageX((v, d) -> ora(v, d, 4)));
        operationMap.put(0x0d, NesAddressing.absolute((v, d) -> ora(v, d, 4)));
        operationMap.put(0x1d, NesAddressing.indexedAbsoluteX((v, d) -> ora(v, d, 4)));
        operationMap.put(0x19, NesAddressing.indexedAbsoluteY((v, d) -> ora(v, d, 4)));
        operationMap.put(0x01, NesAddressing.indexedIndirectX((v, d) -> ora(v, d, 6)));
        operationMap.put(0x11, NesAddressing.indexedIndirectY((v, d) -> ora(v, d, 5)));
        // EOR
        operationMap.put(0x49, NesAddressing.immediate((v, d) -> eor(v, d, 2)));
        operationMap.put(0x45, NesAddressing.zeroPage((v, d) -> eor(v, d, 3)));
        operationMap.put(0x55, NesAddressing.indexedZeroPageX((v, d) -> eor(v, d, 4)));
        operationMap.put(0x4d, NesAddressing.absolute((v, d) -> eor(v, d, 4)));
        operationMap.put(0x5d, NesAddressing.indexedAbsoluteX((v, d) -> eor(v, d, 4)));
        operationMap.put(0x59, NesAddressing.indexedAbsoluteY((v, d) -> eor(v, d, 4)));
        operationMap.put(0x41, NesAddressing.indexedIndirectX((v, d) -> eor(v, d, 6)));
        operationMap.put(0x51, NesAddressing.indexedIndirectY((v, d) -> eor(v, d, 5)));
        // ASL
        operationMap.put(0x0a, NesAddressing.accumulator((d) -> asl(d.cpu.getA().getValue(), d, 2)));
        operationMap.put(0x06, NesAddressing.zeroPage((v, d) -> asl(v, d, 5)));
        operationMap.put(0x16, NesAddressing.indexedZeroPageX((v, d) -> asl(v, d, 6)));
        operationMap.put(0x0e, NesAddressing.absolute((v, d) -> asl(v, d, 6)));
        operationMap.put(0x1e, NesAddressing.indexedAbsoluteX((v, d) -> asl(v, d, 6)));
        // LSR
        operationMap.put(0x0a, NesAddressing.accumulator((d) -> lsr(d.cpu.getA().getValue(), d, 2)));
        operationMap.put(0x06, NesAddressing.zeroPage((v, d) -> lsr(v, d, 5)));
        operationMap.put(0x16, NesAddressing.indexedZeroPageX((v, d) -> lsr(v, d, 6)));
        operationMap.put(0x0e, NesAddressing.absolute((v, d) -> lsr(v, d, 6)));
        operationMap.put(0x1e, NesAddressing.indexedAbsoluteX((v, d) -> lsr(v, d, 6)));
        // ROL
        operationMap.put(0x0a, NesAddressing.accumulator((d) -> rol(d.cpu.getA().getValue(), d, 2)));
        operationMap.put(0x06, NesAddressing.zeroPage((v, d) -> rol(v, d, 5)));
        operationMap.put(0x16, NesAddressing.indexedZeroPageX((v, d) -> rol(v, d, 6)));
        operationMap.put(0x0e, NesAddressing.absolute((v, d) -> rol(v, d, 6)));
        operationMap.put(0x1e, NesAddressing.indexedAbsoluteX((v, d) -> rol(v, d, 6)));
        // ROR
        operationMap.put(0x0a, NesAddressing.accumulator((d) -> ror(d.cpu.getA().getValue(), d, 2)));
        operationMap.put(0x06, NesAddressing.zeroPage((v, d) -> ror(v, d, 5)));
        operationMap.put(0x16, NesAddressing.indexedZeroPageX((v, d) -> ror(v, d, 6)));
        operationMap.put(0x0e, NesAddressing.absolute((v, d) -> ror(v, d, 6)));
        operationMap.put(0x1e, NesAddressing.indexedAbsoluteX((v, d) -> ror(v, d, 6)));
        // RelativeJump
        operationMap.put(0x90, NesAddressing.relative((v, d) -> relativeJump(v, d, !d.cpu.getPS().c())));
        operationMap.put(0xb0, NesAddressing.relative((v, d) -> relativeJump(v, d, d.cpu.getPS().c())));
        operationMap.put(0xf0, NesAddressing.relative((v, d) -> relativeJump(v, d, d.cpu.getPS().z())));
        operationMap.put(0xd0, NesAddressing.relative((v, d) -> relativeJump(v, d, !d.cpu.getPS().z())));
        operationMap.put(0x50, NesAddressing.relative((v, d) -> relativeJump(v, d, !d.cpu.getPS().v())));
        operationMap.put(0x70, NesAddressing.relative((v, d) -> relativeJump(v, d, d.cpu.getPS().v())));
        operationMap.put(0x10, NesAddressing.relative((v, d) -> relativeJump(v, d, !d.cpu.getPS().n())));
        operationMap.put(0x30, NesAddressing.relative((v, d) -> relativeJump(v, d, d.cpu.getPS().n())));
        // BIT
        operationMap.put(0x24, NesAddressing.zeroPage((v, d) -> bit(v, d, 3)));
        operationMap.put(0x2c, NesAddressing.absolute((v, d) -> bit(v, d, 4)));
        // JMP
        operationMap.put(0x4c, NesAddressing.absolute((v, d) -> jmp(v, d, 3)));
        operationMap.put(0x6c, NesAddressing.indirect((v, d) -> jmp(v, d, 5)));
        // JSR
        operationMap.put(0x20, NesAddressing.absolute((v, d) -> {
            int pushAddr = d.startPc + 2;
            d.cpu.push(pushAddr >> 8);
            d.cpu.push(pushAddr & 0xff);
            d.nextPc = v;
            return 6;
        }));
        // RTS
        operationMap.put(0x60, NesAddressing.immediate((v, d) -> {
            int addr = d.cpu.pop();
            addr |= (d.cpu.pop() << 8);
            d.nextPc = addr + 1;
            return 6;
        }));
        // BRK
        operationMap.put(0x00, NesAddressing.accumulator(d -> {
            d.cpu.push(d.nextPc >> 8);
            d.cpu.push(d.nextPc & 0xff);
            d.cpu.push(d.cpu.getPS().getValue());
            d.nextPc = d.memory.read(0xfffe) | (d.memory.read(0xffff) << 8);
            return 7;
        }));
        // RTI
        operationMap.put(0x40, NesAddressing.accumulator(d -> {
            d.cpu.getPS().setValue(d.cpu.pop());
            int addr = d.cpu.pop();
            addr |= (d.cpu.pop() << 8);
            d.nextPc = addr;
            return 6;
        }));
        // CMP
        operationMap.put(0xc9, NesAddressing.immediate((v, d) -> cmp(d.cpu.getA(), v, d, 2)));
        operationMap.put(0xc5, NesAddressing.zeroPage((v, d) -> cmp(d.cpu.getA(), v, d, 3)));
        operationMap.put(0xd5, NesAddressing.indexedZeroPageX((v, d) -> cmp(d.cpu.getA(), v, d, 4)));
        operationMap.put(0xcd, NesAddressing.absolute((v, d) -> cmp(d.cpu.getA(), v, d, 4)));
        operationMap.put(0xdd, NesAddressing.indexedAbsoluteX((v, d) -> cmp(d.cpu.getA(), v, d, 4)));
        operationMap.put(0xd9, NesAddressing.indexedAbsoluteY((v, d) -> cmp(d.cpu.getA(), v, d, 4)));
        operationMap.put(0xc1, NesAddressing.indexedAbsoluteX((v, d) -> cmp(d.cpu.getA(), v, d, 6)));
        operationMap.put(0xd1, NesAddressing.indexedIndirectY((v, d) -> cmp(d.cpu.getA(), v, d, 5)));
        // CPX
        operationMap.put(0xe0, NesAddressing.immediate((v, d) -> cmp(d.cpu.getX(), v, d, 2)));
        operationMap.put(0xe4, NesAddressing.zeroPage((v, d) -> cmp(d.cpu.getX(), v, d, 3)));
        operationMap.put(0xec, NesAddressing.absolute((v, d) -> cmp(d.cpu.getX(), v, d, 4)));
        // CPY
        operationMap.put(0xc0, NesAddressing.immediate((v, d) -> cmp(d.cpu.getY(), v, d, 2)));
        operationMap.put(0xc4, NesAddressing.zeroPage((v, d) -> cmp(d.cpu.getY(), v, d, 3)));
        operationMap.put(0xcc, NesAddressing.absolute((v, d) -> cmp(d.cpu.getY(), v, d, 4)));
        // INC
        operationMap.put(0xe6, NesAddressing.zeroPage((v, d) -> {
            d.memory.write(d.address, flagNZ(v + 1, d));
            return 5;
        }));
        BiFunction<Integer, NesAddressing.OperationData, Integer> inc6 = (v, d) -> {
            d.memory.write(d.address, flagNZ(v + 1, d));
            return 5;
        };
        operationMap.put(0xf6, NesAddressing.indexedZeroPageX(inc6));
        operationMap.put(0xee, NesAddressing.absolute(inc6));
        operationMap.put(0xfe, NesAddressing.indexedAbsoluteX(inc6));
        // DEC
        operationMap.put(0xc6, NesAddressing.zeroPage((v, d) -> {
            d.memory.write(d.address, flagNZ(v - 1, d));
            return 5;
        }));
        BiFunction<Integer, NesAddressing.OperationData, Integer> dec6 = (v, d) -> {
            d.memory.write(d.address, flagNZ(v - 1, d));
            return 5;
        };
        operationMap.put(0xd6, NesAddressing.indexedZeroPageX(dec6));
        operationMap.put(0xce, NesAddressing.absolute(dec6));
        operationMap.put(0xde, NesAddressing.indexedAbsoluteX(dec6));
        // INX/DEC/INY/DEY
        operationMap.put(0xe8, NesAddressing.accumulator(d -> {
            d.cpu.getX().setValue(flagNZ(d.cpu.getX().getValue() + 1, d));
            return 2;
        }));
        operationMap.put(0xca, NesAddressing.accumulator(d -> {
            d.cpu.getX().setValue(flagNZ(d.cpu.getX().getValue() - 1, d));
            return 2;
        }));
        operationMap.put(0xc8, NesAddressing.accumulator(d -> {
            d.cpu.getY().setValue(flagNZ(d.cpu.getY().getValue() + 1, d));
            return 2;
        }));
        operationMap.put(0x88, NesAddressing.accumulator(d -> {
            d.cpu.getY().setValue(flagNZ(d.cpu.getY().getValue() - 1, d));
            return 2;
        }));
        // FLAG
        operationMap.put(0x18, NesAddressing.accumulator(d -> {
            d.cpu.getPS().c(false);
            return 2;
        }));
        operationMap.put(0x38, NesAddressing.accumulator(d -> {
            d.cpu.getPS().c(true);
            return 2;
        }));
        operationMap.put(0x58, NesAddressing.accumulator(d -> {
            d.cpu.getPS().i(false);
            return 2;
        }));
        operationMap.put(0x78, NesAddressing.accumulator(d -> {
            d.cpu.getPS().i(true);
            return 2;
        }));
        operationMap.put(0xd8, NesAddressing.accumulator(d -> {
            d.cpu.getPS().d(false);
            return 2;
        }));
        operationMap.put(0xf8, NesAddressing.accumulator(d -> {
            d.cpu.getPS().d(true);
            return 2;
        }));
        operationMap.put(0xb8, NesAddressing.accumulator(d -> {
            d.cpu.getPS().v(false);
            return 2;
        }));
        // LDA
        operationMap.put(0xa9, NesAddressing.immediate((v, d) -> load(d.cpu.getA(), v, d, 2)));
        operationMap.put(0xa5, NesAddressing.zeroPage((v, d) -> load(d.cpu.getA(), v, d, 3)));
        operationMap.put(0xb5, NesAddressing.indexedZeroPageX((v, d) -> load(d.cpu.getA(), v, d, 4)));
        operationMap.put(0xad, NesAddressing.absolute((v, d) -> load(d.cpu.getA(), v, d, 4)));
        operationMap.put(0xbd, NesAddressing.indexedAbsoluteX((v, d) -> load(d.cpu.getA(), v, d, 4)));
        operationMap.put(0xb9, NesAddressing.indexedAbsoluteY((v, d) -> load(d.cpu.getA(), v, d, 4)));
        operationMap.put(0xa1, NesAddressing.indexedIndirectX((v, d) -> load(d.cpu.getA(), v, d, 6)));
        operationMap.put(0xb1, NesAddressing.indexedIndirectY((v, d) -> load(d.cpu.getA(), v, d, 5)));
        // LDX
        operationMap.put(0xa2, NesAddressing.immediate((v, d) -> load(d.cpu.getX(), v, d, 2)));
        operationMap.put(0xa6, NesAddressing.zeroPage((v, d) -> load(d.cpu.getX(), v, d, 3)));
        operationMap.put(0xb6, NesAddressing.indexedZeroPageY((v, d) -> load(d.cpu.getX(), v, d, 3)));
        operationMap.put(0xae, NesAddressing.absolute((v, d) -> load(d.cpu.getX(), v, d, 4)));
        operationMap.put(0xbe, NesAddressing.indexedAbsoluteY((v, d) -> load(d.cpu.getX(), v, d, 4)));
        // LDY
        operationMap.put(0xa0, NesAddressing.immediate((v, d) -> load(d.cpu.getY(), v, d, 2)));
        operationMap.put(0xa4, NesAddressing.zeroPage((v, d) -> load(d.cpu.getY(), v, d, 3)));
        operationMap.put(0xb4, NesAddressing.indexedZeroPageX((v, d) -> load(d.cpu.getY(), v, d, 3)));
        operationMap.put(0xac, NesAddressing.absolute((v, d) -> load(d.cpu.getY(), v, d, 4)));
        operationMap.put(0xbc, NesAddressing.indexedAbsoluteX((v, d) -> load(d.cpu.getY(), v, d, 4)));
    }

    @Override
    public IOpecode getOpecode(int code) {
        return operationMap.get(code);
    }
}
