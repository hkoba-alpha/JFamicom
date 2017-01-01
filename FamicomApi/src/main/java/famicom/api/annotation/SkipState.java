package famicom.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by hkoba on 2016/12/31.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Event(Event.EventType.SKIP_STATE)
public @interface SkipState {
    String state() default "";
}
