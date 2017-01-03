package famicom.api.annotation;

import famicom.api.ppu.IChrRom;

import java.lang.annotation.*;

/**
 * Created by hkoba on 2017/01/03.
 */
@Repeatable(ChrRomList.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ChrRom {
    String[] args();

    Class<? extends IChrRom> type();

    int ppuAddr() default 0x0000;

    int size() default 0x2000;
}
