package emu.cpu;

import famicom.api.annotation.Attach;
import famicom.api.annotation.FamicomApplication;
import famicom.api.annotation.HBlank;
import famicom.api.annotation.PostReset;
import famicom.api.memory.FamicomMemory;
import famicom.api.ppu.IFamicomPPU;
import famicom.api.state.ScanState;
import org.lwjgl.Sys;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
    //public static final int SCAN_CLOCK = 7457 * 4;
    public static final int SCAN_CLOCK = 114;

    private int cycleCount;

    protected boolean loggingFlag;

    @Attach
    protected FamicomMemory famicomMemory;

    @Attach
    protected IOpecodeManager opecodeManager;

    @Attach
    protected IFamicomPPU famicomPPU;

    protected NesRegister regA = new NesRegister(8) {
        @Override
        public int setValue(int v) {
            NesCpu.this.log(l -> l.append(" A=" + Integer.toString(v, 16)));
            return super.setValue(v);
        }
    };
    protected NesRegister regX = new NesRegister(8) {
        @Override
        public int setValue(int v) {
            NesCpu.this.log(l -> l.append(" X=" + Integer.toString(v, 16)));
            return super.setValue(v);
        }
    };
    protected NesRegister regY = new NesRegister(8) {
        @Override
        public int setValue(int v) {
            NesCpu.this.log(l -> l.append(" Y=" + Integer.toString(v, 16)));
            return super.setValue(v);
        }
    };

    protected NesRegister regPC = new NesRegister(16);
    protected NesRegister regSP = new NesRegister(8);
    protected PsRegister regPS = new PsRegister() {
        @Override
        public PsRegister c(boolean flag) {
            NesCpu.this.log(l -> l.append(" =" + flag));
            return super.c(flag);
        }

        @Override
        public PsRegister z(boolean flag) {
            NesCpu.this.log(l -> l.append(" z=" + flag));
            return super.z(flag);
        }

        @Override
        public PsRegister i(boolean flag) {
            NesCpu.this.log(l -> l.append(" i=" + flag));
            return super.i(flag);
        }

        @Override
        public PsRegister d(boolean flag) {
            NesCpu.this.log(l -> l.append(" d=" + flag));
            return super.d(flag);
        }

        @Override
        public PsRegister b(boolean flag) {
            NesCpu.this.log(l -> l.append(" b=" + flag));
            return super.b(flag);
        }

        @Override
        public PsRegister v(boolean flag) {
            NesCpu.this.log(l -> l.append(" v=" + flag));
            return super.v(flag);
        }

        @Override
        public PsRegister n(boolean flag) {
            NesCpu.this.log(l -> l.append(" n=" + flag));
            return super.n(flag);
        }
    };

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
        cycleCount = 6;
    }

    private int execNumber;

    @HBlank
    private void hBlank(ScanState state) {
        if (state.getLineCount() == 240 && famicomPPU.getControlData().isNmiEnabled()) {
            // VBlank
            cycleCount += opecodeManager.interrupt(this, IOpecodeManager.InterruptType.NMI);
        }
        while (cycleCount < SCAN_CLOCK) {
            IOpecode code = opecodeManager.getOpecode(famicomMemory.read(regPC.getValue()));
            switch (regPC.getValue()) {
                case 0xdfec:
                    //loggingFlag = true;
                    break;
                case 0xc2a8:
                case 0xc2ab:
                case 0xc2ad:
                case 0xc2b0:
                    break;
                default:
                    if (loggingFlag) {
                        logText = new StringBuilder();
                        log(l -> l.append(String.format("(%6d)%04X: ", execNumber, regPC.getValue())));
                        execNumber++;
                    }
                    break;
            }
            if (code == null) {
                // NOP
                System.out.println("Invalid:#" + Integer.toString(regPC.getValue(), 16) + ", " + Integer.toString(famicomMemory.read(regPC.getValue()), 16));
                cycleCount += 2;
                regPC.setValue(regPC.getValue() + 1);
            } else {
                cycleCount += code.execute(this);
            }
            if (logText != null) {
                System.out.println(logText);
                /*
                logList.add(logText.toString());
                if (logList.size() > 50) {
                    logList.remove(0);
                }
                */
                logText = null;
            }
        }
        cycleCount -= SCAN_CLOCK;
    }

    private StringBuilder logText;
    private List<String> logList = new ArrayList<>();

    public void log(Consumer<StringBuilder> proc) {
        if (logText != null) {
            proc.accept(logText);
        }
    }
}
