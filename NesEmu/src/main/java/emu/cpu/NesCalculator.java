package emu.cpu;

import java.util.function.Consumer;

/**
 * Created by hkoba on 2017/01/28.
 */
public abstract class NesCalculator {
    protected abstract int calculate(int a, int b, PsRegister ps);

    public static NesCalculator ADC = new NesCalculator() {
        @Override
        protected int calculate(int a, int b, PsRegister ps) {
            return a + b;
        }
    };
    public static NesCalculator SBC = new NesCalculator() {
        @Override
        protected int calculate(int a, int b, PsRegister ps) {
            return a + b;
        }
    };
    public static NesCalculator AND = new NesCalculator() {
        @Override
        protected int calculate(int a, int b, PsRegister ps) {
            return a + b;
        }
    };
    public static NesCalculator ORA = new NesCalculator() {
        @Override
        protected int calculate(int a, int b, PsRegister ps) {
            return a + b;
        }
    };
    public static NesCalculator EOR = new NesCalculator() {
        @Override
        protected int calculate(int a, int b, PsRegister ps) {
            return a + b;
        }
    };
}
