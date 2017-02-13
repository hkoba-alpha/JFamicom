package emu.cpu;

import famicom.api.annotation.FamicomApplication;
import famicom.api.annotation.Initialize;
import org.lwjgl.Sys;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by hkoba on 2017/01/28.
 */
@FamicomApplication
public class NesOperationManager implements IOpecodeManager {
    private Map<Integer, NesAddressing> operationMap = new HashMap<>();

    private int adc(NesAddressing.OperationData data, int clk) {
        int oldVal = data.cpu.getA().getValue();
        int newVal = oldVal + data.value() + (data.cpu.getPS().c() ? 1 : 0);
        data.cpu.getA().setValue(flagNZ(newVal & 255, data));
        data.cpu.getPS().c(newVal >= 0x100).v((oldVal & 0x80) != (newVal & 0x80));
        return clk + (data.pageCross ? 1 : 0);
    }

    private int sbc(NesAddressing.OperationData data, int clk) {
        int oldVal = data.cpu.getA().getValue();
        int newVal = oldVal - data.value() - (data.cpu.getPS().c() ? 0 : 1);
        data.cpu.getA().setValue(flagNZ(newVal & 255, data));
        data.cpu.getPS().c(newVal >= 0).v((oldVal & 0x80) != (newVal & 0x80));
        return clk + (data.pageCross ? 1 : 0);
    }

    private int and(NesAddressing.OperationData data, int clk) {
        data.cpu.getA().setValue(flagNZ(data.cpu.getA().getValue() & data.value(), data));
        return clk + (data.pageCross ? 1 : 0);
    }

    private int ora(NesAddressing.OperationData data, int clk) {
        data.cpu.getA().setValue(flagNZ(data.cpu.getA().getValue() | data.value(), data));
        return clk + (data.pageCross ? 1 : 0);
    }

    private int eor(NesAddressing.OperationData data, int clk) {
        data.cpu.getA().setValue(flagNZ(data.cpu.getA().getValue() ^ data.value(), data));
        return clk + (data.pageCross ? 1 : 0);
    }

    private int asl(NesAddressing.OperationData data, int clk) {
        int value = (data.address < 0 ? data.cpu.getA().getValue() : data.value());
        int newVal = value << 1;
        data.cpu.getPS().c((newVal & 0x100) > 0);
        if (data.address < 0) {
            data.cpu.getA().setValue(newVal & 255);
        } else {
            data.store(newVal & 255);
        }
        return clk + (data.pageCross ? 1 : 0);
    }

    private int lsr(NesAddressing.OperationData data, int clk) {
        int value = (data.address < 0 ? data.cpu.getA().getValue() : data.value());
        data.cpu.getPS().c((value & 1) > 0);
        int newVal = value >> 1;
        if (data.address < 0) {
            data.cpu.getA().setValue(newVal & 255);
        } else {
            data.store(newVal & 255);
        }
        return clk + (data.pageCross ? 1 : 0);
    }

    private int rol(NesAddressing.OperationData data, int clk) {
        int value = (data.address < 0 ? data.cpu.getA().getValue() : data.value());
        int newVal = (value << 1) | (data.cpu.getPS().c() ? 1 : 0);
        data.cpu.getPS().c((newVal & 0x100) > 0);
        if (data.address < 0) {
            data.cpu.getA().setValue(newVal & 255);
        } else {
            data.store(newVal & 255);
        }
        return clk + (data.pageCross ? 1 : 0);
    }

    private int ror(NesAddressing.OperationData data, int clk) {
        int value = (data.address < 0 ? data.cpu.getA().getValue() : data.value());
        boolean c = (value & 1) > 0;
        int newVal = (value >> 1) | (data.cpu.getPS().c() ? 0x80 : 0);
        data.cpu.getPS().c(c);
        if (data.address < 0) {
            data.cpu.getA().setValue(newVal & 255);
        } else {
            data.store(newVal & 255);
        }
        return clk + (data.pageCross ? 1 : 0);
    }

    private int cmp(NesRegister reg, NesAddressing.OperationData data, int clk) {
        int value = data.value();
        data.cpu.getPS().c(reg.getValue() >= value);
        flagNZ(reg.getValue() - value, data);
        return clk + (data.pageCross ? 1 : 0);
    }

    private int bit(NesAddressing.OperationData data, int clk) {
        int result = data.cpu.getA().getValue() & data.value();
        flagNZ(result, data);
        data.cpu.getPS().v((result & 0x40) > 0);
        return clk + (data.pageCross ? 1 : 0);
    }

    private int jmp(NesAddressing.OperationData data, int clk) {
        data.nextPc = data.address;
        return clk + (data.pageCross ? 1 : 0);
    }

    private int relativeJump(NesAddressing.OperationData data, boolean condition) {
        int clk = data.pageCross ? 3 : 2;
        if (condition) {
            clk++;
            data.nextPc = data.address;
        }
        return clk;
    }

    private int flagNZ(int value, NesAddressing.OperationData data) {
        data.cpu.getPS().n((value & 0x80) > 0).z((value & 0xff) == 0);
        return value & 0xff;
    }

    private int load(NesRegister reg, NesAddressing.OperationData data, int clk) {
        reg.setValue(flagNZ(data.value(), data));
        return clk + (data.pageCross ? 1 : 0);
    }

    private int store(NesAddressing.OperationData data, NesRegister reg, int clk) {
        data.store(reg.getValue());
        return clk + (data.pageCross ? 1 : 0);
    }

    private int transfer(NesRegister src, NesRegister dest, NesAddressing.OperationData data) {
        dest.setValue(flagNZ(src.getValue(), data));
        return 2;
    }

    @Initialize
    public void init() {
        // ADC
        operationMap.put(0x69, NesAddressing.immediate("ADC", d -> adc(d, 2)));
        operationMap.put(0x65, NesAddressing.zeroPage("ADC", d -> adc(d, 3)));
        operationMap.put(0x75, NesAddressing.indexedZeroPageX("ADC", d -> adc(d, 4)));
        operationMap.put(0x6d, NesAddressing.absolute("ADC", d -> adc(d, 4)));
        operationMap.put(0x7d, NesAddressing.indexedAbsoluteX("ADC", d -> adc(d, 4)));
        operationMap.put(0x79, NesAddressing.indexedAbsoluteY("ADC", d -> adc(d, 4)));
        operationMap.put(0x61, NesAddressing.indexedIndirectX("ADC", d -> adc(d, 6)));
        operationMap.put(0x71, NesAddressing.indexedIndirectY("ADC", d -> adc(d, 5)));
        // SBC
        operationMap.put(0xe9, NesAddressing.immediate("SBC", d -> sbc(d, 2)));
        operationMap.put(0xe5, NesAddressing.zeroPage("SBC", d -> sbc(d, 3)));
        operationMap.put(0xf5, NesAddressing.indexedZeroPageX("SBC", d -> sbc(d, 4)));
        operationMap.put(0xed, NesAddressing.absolute("SBC", d -> sbc(d, 4)));
        operationMap.put(0xfd, NesAddressing.indexedAbsoluteX("SBC", d -> sbc(d, 4)));
        operationMap.put(0xf9, NesAddressing.indexedAbsoluteY("SBC", d -> sbc(d, 4)));
        operationMap.put(0xe1, NesAddressing.indexedIndirectX("SBC", d -> sbc(d, 6)));
        operationMap.put(0xf1, NesAddressing.indexedIndirectY("SBC", d -> sbc(d, 5)));
        // AND
        operationMap.put(0x29, NesAddressing.immediate("AND", d -> and(d, 2)));
        operationMap.put(0x25, NesAddressing.zeroPage("AND", d -> and(d, 3)));
        operationMap.put(0x35, NesAddressing.indexedZeroPageX("AND", d -> and(d, 4)));
        operationMap.put(0x2d, NesAddressing.absolute("AND", d -> and(d, 4)));
        operationMap.put(0x3d, NesAddressing.indexedAbsoluteX("AND", d -> and(d, 4)));
        operationMap.put(0x39, NesAddressing.indexedAbsoluteY("AND", d -> and(d, 4)));
        operationMap.put(0x21, NesAddressing.indexedIndirectX("AND", d -> and(d, 6)));
        operationMap.put(0x31, NesAddressing.indexedIndirectY("AND", d -> and(d, 5)));
        // ORA
        operationMap.put(0x09, NesAddressing.immediate("ORA", d -> ora(d, 2)));
        operationMap.put(0x05, NesAddressing.zeroPage("ORA", d -> ora(d, 3)));
        operationMap.put(0x15, NesAddressing.indexedZeroPageX("ORA", d -> ora(d, 4)));
        operationMap.put(0x0d, NesAddressing.absolute("ORA", d -> ora(d, 4)));
        operationMap.put(0x1d, NesAddressing.indexedAbsoluteX("ORA", d -> ora(d, 4)));
        operationMap.put(0x19, NesAddressing.indexedAbsoluteY("ORA", d -> ora(d, 4)));
        operationMap.put(0x01, NesAddressing.indexedIndirectX("ORA", d -> ora(d, 6)));
        operationMap.put(0x11, NesAddressing.indexedIndirectY("ORA", d -> ora(d, 5)));
        // EOR
        operationMap.put(0x49, NesAddressing.immediate("EOR", d -> eor(d, 2)));
        operationMap.put(0x45, NesAddressing.zeroPage("EOR", d -> eor(d, 3)));
        operationMap.put(0x55, NesAddressing.indexedZeroPageX("EOR", d -> eor(d, 4)));
        operationMap.put(0x4d, NesAddressing.absolute("EOR", d -> eor(d, 4)));
        operationMap.put(0x5d, NesAddressing.indexedAbsoluteX("EOR", d -> eor(d, 4)));
        operationMap.put(0x59, NesAddressing.indexedAbsoluteY("EOR", d -> eor(d, 4)));
        operationMap.put(0x41, NesAddressing.indexedIndirectX("EOR", d -> eor(d, 6)));
        operationMap.put(0x51, NesAddressing.indexedIndirectY("EOR", d -> eor(d, 5)));
        // ASL
        operationMap.put(0x0a, NesAddressing.accumulator("ASL", d -> asl(d, 2)));
        operationMap.put(0x06, NesAddressing.zeroPage("ASL", d -> asl(d, 5)));
        operationMap.put(0x16, NesAddressing.indexedZeroPageX("ASL", d -> asl(d, 6)));
        operationMap.put(0x0e, NesAddressing.absolute("ASL", d -> asl(d, 6)));
        operationMap.put(0x1e, NesAddressing.indexedAbsoluteX("ASL", d -> asl(d, 6)));
        // LSR
        operationMap.put(0x4a, NesAddressing.accumulator("LSR", d -> lsr(d, 2)));
        operationMap.put(0x46, NesAddressing.zeroPage("LSR", d -> lsr(d, 5)));
        operationMap.put(0x56, NesAddressing.indexedZeroPageX("LSR", d -> lsr(d, 6)));
        operationMap.put(0x4e, NesAddressing.absolute("LSR", d -> lsr(d, 6)));
        operationMap.put(0x5e, NesAddressing.indexedAbsoluteX("LSR", d -> lsr(d, 6)));
        // ROL
        operationMap.put(0x2a, NesAddressing.accumulator("ROL", d -> rol(d, 2)));
        operationMap.put(0x26, NesAddressing.zeroPage("ROL", d -> rol(d, 5)));
        operationMap.put(0x36, NesAddressing.indexedZeroPageX("ROL", d -> rol(d, 6)));
        operationMap.put(0x2e, NesAddressing.absolute("ROL", d -> rol(d, 6)));
        operationMap.put(0x3e, NesAddressing.indexedAbsoluteX("ROL", d -> rol(d, 6)));
        // ROR
        operationMap.put(0x6a, NesAddressing.accumulator("ROR", d -> ror(d, 2)));
        operationMap.put(0x66, NesAddressing.zeroPage("ROR", d -> ror(d, 5)));
        operationMap.put(0x76, NesAddressing.indexedZeroPageX("ROR", d -> ror(d, 6)));
        operationMap.put(0x6e, NesAddressing.absolute("ROR", d -> ror(d, 6)));
        operationMap.put(0x7e, NesAddressing.indexedAbsoluteX("ROR", d -> ror(d, 6)));
        // RelativeJump
        operationMap.put(0x90, NesAddressing.relative("BCC", d -> relativeJump(d, !d.cpu.getPS().c())));
        operationMap.put(0xb0, NesAddressing.relative("BCS", d -> relativeJump(d, d.cpu.getPS().c())));
        operationMap.put(0xf0, NesAddressing.relative("BEQ", d -> relativeJump(d, d.cpu.getPS().z())));
        operationMap.put(0xd0, NesAddressing.relative("BNE", d -> relativeJump(d, !d.cpu.getPS().z())));
        operationMap.put(0x50, NesAddressing.relative("BVC", d -> relativeJump(d, !d.cpu.getPS().v())));
        operationMap.put(0x70, NesAddressing.relative("BVS", d -> relativeJump(d, d.cpu.getPS().v())));
        operationMap.put(0x10, NesAddressing.relative("BPL", d -> relativeJump(d, !d.cpu.getPS().n())));
        operationMap.put(0x30, NesAddressing.relative("BMI", d -> relativeJump(d, d.cpu.getPS().n())));
        // BIT
        operationMap.put(0x24, NesAddressing.zeroPage("BIT", d -> bit(d, 3)));
        operationMap.put(0x2c, NesAddressing.absolute("BIT", d -> bit(d, 4)));
        // JMP
        operationMap.put(0x4c, NesAddressing.absolute("JMP", d -> jmp(d, 3)));
        operationMap.put(0x6c, NesAddressing.indirect("JMP", d -> jmp(d, 5)));
        // JSR
        operationMap.put(0x20, NesAddressing.absolute("JSR", d -> {
            int pushAddr = d.startPc + 2;
            d.cpu.push(pushAddr >> 8);
            d.cpu.push(pushAddr & 0xff);
            d.nextPc = d.address;
            return 6;
        }));
        // RTS
        operationMap.put(0x60, NesAddressing.accumulator("RTS", d -> {
            int addr = d.cpu.pop();
            addr |= (d.cpu.pop() << 8);
            d.nextPc = addr + 1;
            return 6;
        }));
        // BRK
        operationMap.put(0x00, NesAddressing.accumulator("BRK", d -> {
            if (d.cpu.getPS().i()) {
                return 7;
            }
            d.nextPc++;
            d.cpu.getPS().b(true);
            d.cpu.push(d.nextPc >> 8);
            d.cpu.push(d.nextPc & 0xff);
            d.cpu.push(d.cpu.getPS().getValue());
            d.nextPc = d.memory.read(0xfffe) | (d.memory.read(0xffff) << 8);
            d.cpu.getPS().i(true);
            return 7;
        }));
        // RTI
        operationMap.put(0x40, NesAddressing.accumulator("RTI", d -> {
            d.cpu.getPS().setValue(d.cpu.pop());
            int addr = d.cpu.pop();
            addr |= (d.cpu.pop() << 8);
            d.nextPc = addr;
            return 6;
        }));
        // CMP
        operationMap.put(0xc9, NesAddressing.immediate("CMP", d -> cmp(d.cpu.getA(), d, 2)));
        operationMap.put(0xc5, NesAddressing.zeroPage("CMP", d -> cmp(d.cpu.getA(), d, 3)));
        operationMap.put(0xd5, NesAddressing.indexedZeroPageX("CMP", d -> cmp(d.cpu.getA(), d, 4)));
        operationMap.put(0xcd, NesAddressing.absolute("CMP", d -> cmp(d.cpu.getA(), d, 4)));
        operationMap.put(0xdd, NesAddressing.indexedAbsoluteX("CMP", d -> cmp(d.cpu.getA(), d, 4)));
        operationMap.put(0xd9, NesAddressing.indexedAbsoluteY("CMP", d -> cmp(d.cpu.getA(), d, 4)));
        operationMap.put(0xc1, NesAddressing.indexedAbsoluteX("CMP", d -> cmp(d.cpu.getA(), d, 6)));
        operationMap.put(0xd1, NesAddressing.indexedIndirectY("CMP", d -> cmp(d.cpu.getA(), d, 5)));
        // CPX
        operationMap.put(0xe0, NesAddressing.immediate("CPX", d -> cmp(d.cpu.getX(), d, 2)));
        operationMap.put(0xe4, NesAddressing.zeroPage("CPX", d -> cmp(d.cpu.getX(), d, 3)));
        operationMap.put(0xec, NesAddressing.absolute("CPX", d -> cmp(d.cpu.getX(), d, 4)));
        // CPY
        operationMap.put(0xc0, NesAddressing.immediate("CPY", d -> cmp(d.cpu.getY(), d, 2)));
        operationMap.put(0xc4, NesAddressing.zeroPage("CPY", d -> cmp(d.cpu.getY(), d, 3)));
        operationMap.put(0xcc, NesAddressing.absolute("CPY", d -> cmp(d.cpu.getY(), d, 4)));
        // INC
        operationMap.put(0xe6, NesAddressing.zeroPage("INC", d -> {
            d.store(flagNZ(d.value() + 1, d));
            return 5;
        }));
        Function<NesAddressing.OperationData, Integer> inc6 = d -> {
            d.store(flagNZ(d.value() + 1, d));
            return 6;
        };
        operationMap.put(0xf6, NesAddressing.indexedZeroPageX("INC", inc6));
        operationMap.put(0xee, NesAddressing.absolute("INC", inc6));
        operationMap.put(0xfe, NesAddressing.indexedAbsoluteX("INC", inc6));
        // DEC
        operationMap.put(0xc6, NesAddressing.zeroPage("DEC", d -> {
            d.store(flagNZ(d.value() - 1, d));
            return 5;
        }));
        Function<NesAddressing.OperationData, Integer> dec6 = d -> {
            d.store(flagNZ(d.value() - 1, d));
            return 6;
        };
        operationMap.put(0xd6, NesAddressing.indexedZeroPageX("DEC", dec6));
        operationMap.put(0xce, NesAddressing.absolute("DEC", dec6));
        operationMap.put(0xde, NesAddressing.indexedAbsoluteX("DEC", dec6));
        // INX/DEC/INY/DEY
        operationMap.put(0xe8, NesAddressing.accumulator("INX", d -> {
            d.cpu.getX().setValue(flagNZ(d.cpu.getX().getValue() + 1, d));
            return 2;
        }));
        operationMap.put(0xca, NesAddressing.accumulator("DEX", d -> {
            d.cpu.getX().setValue(flagNZ(d.cpu.getX().getValue() - 1, d));
            return 2;
        }));
        operationMap.put(0xc8, NesAddressing.accumulator("INY", d -> {
            d.cpu.getY().setValue(flagNZ(d.cpu.getY().getValue() + 1, d));
            return 2;
        }));
        operationMap.put(0x88, NesAddressing.accumulator("DEY", d -> {
            d.cpu.getY().setValue(flagNZ(d.cpu.getY().getValue() - 1, d));
            return 2;
        }));
        // FLAG
        operationMap.put(0x18, NesAddressing.accumulator("CLC", d -> {
            d.cpu.getPS().c(false);
            return 2;
        }));
        operationMap.put(0x38, NesAddressing.accumulator("SEC", d -> {
            d.cpu.getPS().c(true);
            return 2;
        }));
        operationMap.put(0x58, NesAddressing.accumulator("CLI", d -> {
            d.cpu.getPS().i(false);
            return 2;
        }));
        operationMap.put(0x78, NesAddressing.accumulator("SEI", d -> {
            d.cpu.getPS().i(true);
            return 2;
        }));
        operationMap.put(0xd8, NesAddressing.accumulator("CLD", d -> {
            d.cpu.getPS().d(false);
            return 2;
        }));
        operationMap.put(0xf8, NesAddressing.accumulator("SED", d -> {
            d.cpu.getPS().d(true);
            return 2;
        }));
        operationMap.put(0xb8, NesAddressing.accumulator("CLV", d -> {
            d.cpu.getPS().v(false);
            return 2;
        }));
        // LDA
        operationMap.put(0xa9, NesAddressing.immediate("LDA", d -> load(d.cpu.getA(), d, 2)));
        operationMap.put(0xa5, NesAddressing.zeroPage("LDA", d -> load(d.cpu.getA(), d, 3)));
        operationMap.put(0xb5, NesAddressing.indexedZeroPageX("LDA", d -> load(d.cpu.getA(), d, 4)));
        operationMap.put(0xad, NesAddressing.absolute("LDA", d -> load(d.cpu.getA(), d, 4)));
        operationMap.put(0xbd, NesAddressing.indexedAbsoluteX("LDA", d -> load(d.cpu.getA(), d, 4)));
        operationMap.put(0xb9, NesAddressing.indexedAbsoluteY("LDA", d -> load(d.cpu.getA(), d, 4)));
        operationMap.put(0xa1, NesAddressing.indexedIndirectX("LDA", d -> load(d.cpu.getA(), d, 6)));
        operationMap.put(0xb1, NesAddressing.indexedIndirectY("LDA", d -> load(d.cpu.getA(), d, 5)));
        // LDX
        operationMap.put(0xa2, NesAddressing.immediate("LDX", d -> load(d.cpu.getX(), d, 2)));
        operationMap.put(0xa6, NesAddressing.zeroPage("LDX", d -> load(d.cpu.getX(), d, 3)));
        operationMap.put(0xb6, NesAddressing.indexedZeroPageY("LDX", d -> load(d.cpu.getX(), d, 3)));
        operationMap.put(0xae, NesAddressing.absolute("LDX", d -> load(d.cpu.getX(), d, 4)));
        operationMap.put(0xbe, NesAddressing.indexedAbsoluteY("LDX", d -> load(d.cpu.getX(), d, 4)));
        // LDY
        operationMap.put(0xa0, NesAddressing.immediate("LDY", d -> load(d.cpu.getY(), d, 2)));
        operationMap.put(0xa4, NesAddressing.zeroPage("LDY", d -> load(d.cpu.getY(), d, 3)));
        operationMap.put(0xb4, NesAddressing.indexedZeroPageX("LDY", d -> load(d.cpu.getY(), d, 3)));
        operationMap.put(0xac, NesAddressing.absolute("LDY", d -> load(d.cpu.getY(), d, 4)));
        operationMap.put(0xbc, NesAddressing.indexedAbsoluteX("LDY", d -> load(d.cpu.getY(), d, 4)));
        // STA
        operationMap.put(0x85, NesAddressing.zeroPage("STA", d -> store(d, d.cpu.getA(), 3)));
        operationMap.put(0x95, NesAddressing.indexedZeroPageX("STA", d -> store(d, d.cpu.getA(), 4)));
        operationMap.put(0x8d, NesAddressing.absolute("STA", d -> store(d, d.cpu.getA(), 4)));
        operationMap.put(0x9d, NesAddressing.indexedAbsoluteX("STA", d -> store(d, d.cpu.getA(), 4)));
        operationMap.put(0x99, NesAddressing.indexedAbsoluteY("STA", d -> store(d, d.cpu.getA(), 4)));
        operationMap.put(0x81, NesAddressing.indexedIndirectX("STA", d -> store(d, d.cpu.getA(), 6)));
        operationMap.put(0x91, NesAddressing.indexedIndirectY("STA", d -> store(d, d.cpu.getA(), 5)));
        // STX
        operationMap.put(0x86, NesAddressing.zeroPage("STX", d -> store(d, d.cpu.getX(), 3)));
        operationMap.put(0x96, NesAddressing.indexedZeroPageY("STX", d -> store(d, d.cpu.getX(), 4)));
        operationMap.put(0x8e, NesAddressing.absolute("STX", d -> store(d, d.cpu.getX(), 4)));
        // STY
        operationMap.put(0x84, NesAddressing.zeroPage("STY", d -> store(d, d.cpu.getY(), 3)));
        operationMap.put(0x94, NesAddressing.indexedZeroPageX("STY", d -> store(d, d.cpu.getY(), 4)));
        operationMap.put(0x8c, NesAddressing.absolute("STY", d -> store(d, d.cpu.getY(), 4)));
        // 転送
        operationMap.put(0xaa, NesAddressing.accumulator("TAX", d -> transfer(d.cpu.getA(), d.cpu.getX(), d)));
        operationMap.put(0x8a, NesAddressing.accumulator("TXA", d -> transfer(d.cpu.getX(), d.cpu.getA(), d)));
        operationMap.put(0xa8, NesAddressing.accumulator("TAY", d -> transfer(d.cpu.getA(), d.cpu.getY(), d)));
        operationMap.put(0x98, NesAddressing.accumulator("TYA", d -> transfer(d.cpu.getY(), d.cpu.getA(), d)));
        operationMap.put(0x9a, NesAddressing.accumulator("TXS", d -> transfer(d.cpu.getSP(), d.cpu.getX(), d)));
        operationMap.put(0xba, NesAddressing.accumulator("TSX", d -> {
            d.cpu.getSP().setValue(d.cpu.getX().getValue());
            return 2;
        }));
        // stack
        operationMap.put(0x48, NesAddressing.accumulator("PHA", d -> {
            d.cpu.push(d.cpu.getA().getValue());
            return 3;
        }));
        operationMap.put(0x68, NesAddressing.accumulator("PLA", d -> {
            d.cpu.getA().setValue(flagNZ(d.cpu.pop(), d));
            return 4;
        }));
        operationMap.put(0x08, NesAddressing.accumulator("PHP", d -> {
            d.cpu.push(d.cpu.getPS().getValue() | PsRegister.FLAG_B);
            return 3;
        }));
        operationMap.put(0x28, NesAddressing.accumulator("PLP", d -> {
            d.cpu.getPS().setValue(d.cpu.pop());
            return 4;
        }));
        // NOP
        operationMap.put(0xea, NesAddressing.accumulator("NOP", v -> 2));
    }

    @Override
    public IOpecode getOpecode(int code) {
        return operationMap.get(code);
    }

    @Override
    public int interrupt(NesCpu cpu, InterruptType type) {
        int pc = cpu.getPC().getValue();
        switch (type) {
            case NMI:
                cpu.getPS().b(false);
                cpu.push(pc >> 8);
                cpu.push(pc & 0xff);
                cpu.push(cpu.getPS().getValue());
                cpu.getPS().i(true);
                cpu.getPC().setValue(cpu.getMemory().read(0xfffa) | (cpu.getMemory().read(0xfffb) << 8));
                return 7;
            case IRQ:
                if (!cpu.getPS().i()) {
                    cpu.getPS().b(false);
                    cpu.push(pc >> 8);
                    cpu.push(pc & 0xff);
                    cpu.push(cpu.getPS().getValue());
                    cpu.getPS().i(true);
                    cpu.getPC().setValue(cpu.getMemory().read(0xfffe) | (cpu.getMemory().read(0xffff) << 8));
                    return 8;
                }
                break;
            default:
                break;
        }
        return 0;
    }
}
