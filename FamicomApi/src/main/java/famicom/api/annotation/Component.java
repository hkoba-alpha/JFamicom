package famicom.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by hkoba on 2016/12/31.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {
    /**
     * 種類
     */
    public enum ComponentType {
        API(0),
        LIBRARY(2),
        APPLICATION(1);

        private int priority;

        ComponentType(int pri) {
            priority = pri;
        }

        public int getPriority() {
            return priority;
        }
    }

    ComponentType value();

    /**
     * 大きいほど優先度が高い
     * @return
     */
    int priority() default 0;
}
