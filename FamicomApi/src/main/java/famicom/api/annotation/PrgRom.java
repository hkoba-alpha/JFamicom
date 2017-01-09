package famicom.api.annotation;

import famicom.api.memory.AbstractMemoryFile;

import java.lang.annotation.*;

/**
 * PRG-ROMのデータ
 * 最終バンクが c000-ffff にマップ
 * 8000-bfffはバンク切り替えが可能
 * Created by hkoba on 2017/01/03.
 */
@Repeatable(PrgRom.PrgRomList.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PrgRom {
    String fileName();

    Class<? extends AbstractMemoryFile> type();

    String[] names() default {};

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PrgRomList {
        PrgRom[] value();
    }
}
