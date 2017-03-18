package emu.cpu;

/**
 * Created by hkoba on 2017/01/28.
 */
public class PsRegister extends NesRegister {
    public static final int FLAG_C = 1;
    public static final int FLAG_Z = 2;
    public static final int FLAG_I = 4;
    public static final int FLAG_D = 8;
    public static final int FLAG_B = 16;
    public static final int FLAG_V = 64;
    public static final int FLAG_N = 128;

    public  PsRegister() {
        super(8);
    }
    public boolean c() {
        return (getValue() & FLAG_C) > 0;
    }
    public boolean z() {
        return (getValue() & FLAG_Z) > 0;
    }
    public boolean i() {
        return (getValue() & FLAG_I) > 0;
    }
    public boolean d() {
        return (getValue() & FLAG_D) > 0;
    }
    public boolean b() {
        return (getValue() & FLAG_B) > 0;
    }
    public boolean v() {
        return (getValue() & FLAG_V) > 0;
    }
    public boolean n() {
        return (getValue() & FLAG_N) > 0;
    }

    @Override
    public String toString() {
        StringBuilder txt = new StringBuilder()
                .append(n() ? 'N': '-')
                .append(v() ? 'V': '-')
                .append('-')
                .append(b() ? 'B': '-')
                .append(d() ? 'D': '-')
                .append(i() ? 'I': '-')
                .append(z() ? 'Z': '-')
                .append(c() ? 'C': '-');
        return txt.toString();
    }

    public PsRegister c(boolean flag) {
        setValue((getValue() & ~FLAG_C) | (flag ? FLAG_C: 0));
        return this;
    }
    public PsRegister z(boolean flag) {
        setValue((getValue() & ~FLAG_Z) | (flag ? FLAG_Z: 0));
        return this;
    }
    public PsRegister i(boolean flag) {
        setValue((getValue() & ~FLAG_I) | (flag ? FLAG_I: 0));
        return this;
    }
    public PsRegister d(boolean flag) {
        setValue((getValue() & ~FLAG_D) | (flag ? FLAG_D: 0));
        return this;
    }
    public PsRegister b(boolean flag) {
        setValue((getValue() & ~FLAG_B) | (flag ? FLAG_B: 0));
        return this;
    }
    public PsRegister v(boolean flag) {
        setValue((getValue() & ~FLAG_V) | (flag ? FLAG_V: 0));
        return this;
    }
    public PsRegister n(boolean flag) {
        setValue((getValue() & ~FLAG_N) | (flag ? FLAG_N: 0));
        return this;
    }
}
