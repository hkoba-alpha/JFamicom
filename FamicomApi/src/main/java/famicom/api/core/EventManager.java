package famicom.api.core;

import famicom.api.annotation.Component;
import famicom.api.annotation.Event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hkoba on 2016/12/31.
 */
public class EventManager {
    private class EventInvoker {
        private List<String> typeList;
        private Object target;
        private Method method;

        private EventInvoker(Object obj, Method mt) {
            target = obj;
            method = mt;
            typeList = new ArrayList<>();
            for (Class<?> type: method.getParameterTypes()) {
                typeList.add(type.getName());
            }
        }

        private void dispatchEvent(Map<String, Object> param) {
            Object[] args = new Object[typeList.size()];
            for (int i = 0; i < args.length; i++) {
                args[i] = param.get(typeList.get(i));
            }
            try {
                method.invoke(target, args);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private static final EventManager thisInstance = new EventManager();

    public static EventManager getInstance() {
        return thisInstance;
    }

    private Map<Event.EventType, List<EventInvoker>> globalEventMap = new HashMap<>();
    private Map<String, Map<Event.EventType, List<EventInvoker>>> stateEventMap = new HashMap<>();

    private EventManager() {
    }

    EventManager initialize() {
        globalEventMap.clear();
        stateEventMap.clear();
        ComponentManager.getInstance().getComponentClasses().forEach(def -> {
            for (Method method: def.getDeclaredMethods()) {
                AnnotationUtil.getAnnotation(method.getAnnotations(), Event.class).forEach(attr -> {
                    // Eventが見つかった
                    entryEvent(def, method, attr);
                });
            }
        });
        return this;
    }

    public EventManager dispatchEvent(Event.EventType type, String state, Map<String, Object> param) {
        List<EventInvoker> list = globalEventMap.get(type);
        if (list != null) {
            list.forEach(v -> v.dispatchEvent(param));
        }
        Map<Event.EventType, List<EventInvoker>> map = stateEventMap.get(state);
        if (map == null) {
            return this;
        }
        list = map.get(type);
        if (list != null) {
            list.forEach(v -> v.dispatchEvent(param));
        }
        return this;
    }

    private EventManager entryEvent(Class<?> classDef, Method method, Map<String, Object> attr) {
        method.setAccessible(true);
        Event.EventType type = (Event.EventType)attr.get("value");
        List<EventInvoker> list;
        EventInvoker invoker = new EventInvoker(ComponentManager.getInstance().getObject(classDef), method);
        String state = (String)attr.get("state");
        Map<Event.EventType, List<EventInvoker>> map;
        if (Event.ALL_STATE.equals(state)) {
            // インスタンスで一致を判断する
            map = globalEventMap;
        } else {
            map = stateEventMap.get(state);
            if (map == null) {
                map = new HashMap<>();
                stateEventMap.put(state, map);
            }
        }
        list = map.get(type);
        if (list == null) {
            list = new ArrayList<>();
            map.put(type, list);
        }
        if ((Boolean)attr.get("reverse")) {
            list.add(0, invoker);
        } else {
            list.add(invoker);
        }
        return this;
    }
}
