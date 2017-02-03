package emu.cpu;

import famicom.api.annotation.Attach;
import famicom.api.annotation.FamicomApplication;
import famicom.api.annotation.HBlank;
import famicom.api.annotation.PostReset;
import famicom.api.memory.FamicomMemory;
import famicom.api.state.ScanState;

/**
 * Created by hkoba on 2017/01/14.
 */
@FamicomApplication
public class NesCpu {
    public static final int FLAG_C = 1;
    public static final int FLAG_Z = 2;
    public static final int FLAG_I = 4;
    public static final int FLAG_D = 8;
    public static final int FLAG_B = 16;
    public static final int FLAG_V = 64;
    public static final int FLAG_N = 128;

    /**
     * スキャンラインのクロック数
     */
    public static final int SCAN_CLOCK = 7457 * 4;

    private int cycleCount;

    @Attach
    protected FamicomMemory famicomMemory;

    @Attach
    protected IOpecodeManager opecodeManager;

    protected NesRegister regA = new NesRegister(8);
    protected NesRegister regX = new NesRegister(8);
    protected NesRegister regY = new NesRegister(8);

    protected NesRegister regPC = new NesRegister(16);
    protected NesRegister regSP = new NesRegister(8);
    protected PsRegister regPS = new PsRegister();

    public NesRegister getA() {
        return regA;
    }
    public NesRegister getX() {
        return regX;
    }
    public NesRegister getY() {
        return regY;
    }
    public NesRegister getPC() {
        return regPC;
    }
    public NesRegister getSP() {
        return regSP;
    }
    public PsRegister getPS() {
        return regPS;
    }

    public void push(int value) {
        regSP.setValue(regSP.getValue() - 1);
        famicomMemory.write(0x100 | regSP.getValue(), value);
    }
    public int pop() {
        int ret = famicomMemory.read(0x100 | regSP.getValue());
        regSP.setValue(regSP.getValue() + 1);
        return ret;
    }

    public FamicomMemory getMemory() {
        return famicomMemory;
    }

    @PostReset
    public void reset() {
        regPC.setValue(famicomMemory.read(0xfffc) | (famicomMemory.read(0xfffd) << 8));
        regSP.setValue(0xfd);
        regPS.setValue(0x34);
        regA.setValue(0);
        regX.setValue(0);
        regY.setValue(0);
        famicomMemory.write(0x4017, 0);
        famicomMemory.write(0x4015, 0);
        for (int i = 0x4000; i < 0x4010; i++) {
            famicomMemory.write(i, 0);
        }
    }

    @HBlank
    private void hBlank(ScanState state) {
        if (state.getLineCount() == 240) {
            // VBlank
        }
        while (cycleCount < SCAN_CLOCK) {
            IOpecode code = opecodeManager.getOpecode(famicomMemory.read(regPC.getValue()));
            if (code == null) {
                // NOP
                cycleCount += 2;
                regPC.setValue(regPC.getValue() + 1);
            } else {
                cycleCount += code.execute(this);
            }
        }
        cycleCount -= SCAN_CLOCK;
    }
}
