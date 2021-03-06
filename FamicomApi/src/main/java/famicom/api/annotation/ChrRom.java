package famicom.api.annotation;

import famicom.api.memory.AbstractMemoryFile;

import java.lang.annotation.*;

/**
 * Created by hkoba on 2017/01/03.
 */
@Repeatable(ChrRomList.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ChrRom {
    String fileName();

    Class<? extends AbstractMemoryFile> type();

    String[] names() default {};
}
