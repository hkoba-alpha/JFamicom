package emu.cpu;

/**
 * Created by hkoba on 2017/01/14.
 */
public interface IOpecode {
    int execute(NesCpu cpu);
}
