package famicom.api.state;

import famicom.api.annotation.Event;

import java.util.*;

/**
 * Created by hkoba on 2016/12/31.
 */
public class GameState {
    protected class EventData {
        protected String eventName;
        protected Map<String, Object> eventArgs;

        protected EventData(String name) {
            eventName = name;
        }
        public String getEventName() {
            return eventName;
        }
    }
    protected String currentState;

    protected Stack<String> stateStack = new Stack<>();

    protected Map<Event.EventType, List<EventData>> eventMap = new HashMap<>();

    protected GameState() {
        eventMap.put(Event.EventType.ENTER_STATE, new ArrayList<>());
        eventMap.put(Event.EventType.LEAVE_STATE, new ArrayList<>());
        eventMap.put(Event.EventType.RESUME_STATE, new ArrayList<>());
        eventMap.put(Event.EventType.PAUSE_STATE, new ArrayList<>());
        eventMap.put(Event.EventType.SKIP_STATE, new ArrayList<>());
    }

    /**
     * leave:current, enter:newState
     * @param newState
     * @return
     */
    public GameState changeState(String newState) {
        if (stateStack.size() > 0) {
            eventMap.get(Event.EventType.LEAVE_STATE).add(new EventData(stateStack.pop()));
        } else {
            eventMap.get(Event.EventType.LEAVE_STATE).add(new EventData(""));
        }
        eventMap.get(Event.EventType.ENTER_STATE).add(new EventData(newState));
        return this;
    }

    public String getCurrentState() {
        return currentState;
    }

    /**
     * pause:current, enter:newState
     * @param newState
     * @return
     */
    public GameState callState(String newState) {
        if (stateStack.size() > 0) {
            eventMap.get(Event.EventType.PAUSE_STATE).add(new EventData(stateStack.peek()));
        } else {
            eventMap.get(Event.EventType.PAUSE_STATE).add(new EventData(""));
        }
        stateStack.push(newState);
        eventMap.get(Event.EventType.ENTER_STATE).add(new EventData(newState));
        return this;
    }

    /**
     * leave:current, skip:..., resume:newState
     * newStateがなければ、
     * leave:current, skip:..., enter:newState
     * @param newState
     * @return
     */
    public GameState returnState(String newState) {
        if (stateStack.size() > 0) {
            eventMap.get(Event.EventType.LEAVE_STATE).add(new EventData(stateStack.peek()));
            while (stateStack.size() > 0) {
                if (stateStack.peek().equals(newState)) {
                    eventMap.get(Event.EventType.RESUME_STATE).add(new EventData(newState));
                    return this;
                }
                eventMap.get(Event.EventType.SKIP_STATE).add(new EventData(stateStack.pop()));
            }
        } else {
            eventMap.get(Event.EventType.LEAVE_STATE).add(new EventData(""));
        }
        stateStack.push(newState);
        eventMap.get(Event.EventType.ENTER_STATE).add(new EventData(newState));
        return this;
    }

    /**
     * leave:current, resume:prev
     * @return
     */
    public GameState returnState() {
        if (stateStack.size() > 0) {
            eventMap.get(Event.EventType.LEAVE_STATE).add(new EventData(stateStack.pop()));
        } else {
            eventMap.get(Event.EventType.LEAVE_STATE).add(new EventData(""));
        }
        if (stateStack.size() > 0) {
            eventMap.get(Event.EventType.RESUME_STATE).add(new EventData(stateStack.peek()));
        } else {
            eventMap.get(Event.EventType.RESUME_STATE).add(new EventData(""));
        }
        return this;
    }
}
