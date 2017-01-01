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
public @interface Event {
    public enum EventType {
        INITIALIZE,
        PRE_RESET,
        POST_RESET,
        HBLANK,
        VBLANK,
        ENTER_STATE,    // 始まり
        LEAVE_STATE,    // 終わり
        PAUSE_STATE,    // 他を呼び出して一時中断
        RESUME_STATE,   // 他から戻ってきて再開
        SKIP_STATE,     // 一気にステータスが切り替わってスキップされた
        SAVE_STATE,     // 状態保存
        LOAD_STATE      // 状態復元
    }

    EventType value();
    String state() default "";

    /**
     * 優先度の逆転フラグ
     * @return
     */
    boolean reverse() default false;
}
