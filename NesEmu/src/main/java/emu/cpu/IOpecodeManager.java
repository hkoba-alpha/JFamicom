package emu.cpu;

import famicom.api.annotation.FamicomApplication;

/**
 * Created by hkoba on 2017/01/28.
 */
@FamicomApplication
public interface IOpecodeManager {
    IOpecode getOpecode(int code);
}
