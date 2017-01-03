package famicom.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by hkoba on 2016/12/31.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@FamicomApplication
public @interface FamicomRom {
    public enum MirrorMode {
        HORIZONTAL,
        VERTICAL,
        FOUR_HORIZONTAL,
        FOUR_VERTICAL
    }


    String name();

    int priority() default 1000;

    String initState() default "";

    MirrorMode mirror() default MirrorMode.HORIZONTAL;

    String[] packages() default {};
}
