package famicom.api.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hkoba on 2016/12/31.
 */
public class AnnotationUtil {
    private static <T> void checkAnnotation(Annotation[] annotations, Class<T> annotationType, List<Map<String, Object>> resultList, Map<String, Object> valueMap) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        for (Annotation annotation: annotations) {
            if (annotation.annotationType().getPackage().getName().startsWith("java.lang.annotation")) {
                continue;
            }
            if (annotationType.isInstance(annotation)) {
                // 対象だった
                Map<String, Object> result = new HashMap<>(valueMap);
                for (Method method: annotationType.getDeclaredMethods()) {
                    if (!valueMap.containsKey(method.getName())) {
                        result.put(method.getName(), method.invoke(annotation));
                    }
                }
                resultList.add(result);
            } else {
                // 対象外
                Map<String, Object> newType = new HashMap<>(valueMap);
                for (Method method: annotation.annotationType().getDeclaredMethods()) {
                    newType.put(method.getName(), method.invoke(annotation));
                }
                checkAnnotation(annotation.annotationType().getAnnotations(), annotationType, resultList, newType);
            }
        }
    }

    public static <T> List<Map<String, Object>> getAnnotation(Annotation[] annotations, Class<T> annotationType) {
        List<Map<String, Object>> resultList = new ArrayList<>();

        try {
            for (Field fld: annotationType.getDeclaredFields()) {
                System.out.println("Field:" + fld.getName());
            }
            checkAnnotation(annotations, annotationType, resultList, new HashMap<>());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return resultList;
    }
}
