package famicom.api.annotation;

/**
 * Created by hkoba on 2016/12/31.
 */
@Component(Component.ComponentType.LIBRARY)
public @interface FamicomLibrary {
    int priority();
}
