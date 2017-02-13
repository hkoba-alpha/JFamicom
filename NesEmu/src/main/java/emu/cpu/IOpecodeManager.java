package emu.cpu;

import famicom.api.annotation.FamicomApplication;

/**
 * Created by hkoba on 2017/01/28.
 */
@FamicomApplication
public interface IOpecodeManager {
    public enum InterruptType {
        NMI, IRQ
    }

    IOpecode getOpecode(int code);

    int interrupt(NesCpu cpu, InterruptType type);
}
