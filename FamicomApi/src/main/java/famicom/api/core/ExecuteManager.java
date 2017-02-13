package famicom.api.core;

import famicom.api.annotation.Event;
import famicom.api.annotation.FamicomRom;
import famicom.api.state.GameState;
import famicom.api.state.PowerControl;
import famicom.api.state.ScanState;

import java.util.*;

/**
 * Created by hkoba on 2016/12/31.
 */
public class ExecuteManager {
    private static final ExecuteManager thisInstance = new ExecuteManager();

    public static final int SCAN_LINE_SIZE = 262;
    public static final int VSYNC_COUNT = 240;

    public static ExecuteManager getInstance() {
        return thisInstance;
    }

    private FamicomRom famicomRom;
    private Class<?> romClass;

    private class GameStateImpl extends GameState {
        private GameStateImpl(String state) {
            super.stateStack.push(state);
        }

        private void dispatchStateEvent() {
            List<EventData> enter = new ArrayList<>(eventMap.get(Event.EventType.ENTER_STATE));
            List<EventData> leave = new ArrayList<>(eventMap.get(Event.EventType.LEAVE_STATE));
            List<EventData> pause = new ArrayList<>(eventMap.get(Event.EventType.PAUSE_STATE));
            List<EventData> resume = new ArrayList<>(eventMap.get(Event.EventType.RESUME_STATE));
            List<EventData> skip = new ArrayList<>(eventMap.get(Event.EventType.SKIP_STATE));
            eventMap.values().forEach(list -> list.clear());
            if (stateStack.size() > 0) {
                currentState = stateStack.peek();
            } else {
                currentState = "";
                stateStack.push("");
            }
            leave.forEach(e -> EventManager.getInstance().dispatchEvent(Event.EventType.LEAVE_STATE, e.getEventName(), eventParam));
            pause.forEach(e -> EventManager.getInstance().dispatchEvent(Event.EventType.PAUSE_STATE, e.getEventName(), eventParam));
            skip.forEach(e -> EventManager.getInstance().dispatchEvent(Event.EventType.SKIP_STATE, e.getEventName(), eventParam));
            resume.forEach(e -> EventManager.getInstance().dispatchEvent(Event.EventType.RESUME_STATE, e.getEventName(), eventParam));
            enter.forEach(e -> EventManager.getInstance().dispatchEvent(Event.EventType.ENTER_STATE, e.getEventName(), eventParam));
        }
    }

    private class ScanStateImpl extends ScanState {
        private boolean scanLine() {
            EventManager.getInstance().dispatchEvent(Event.EventType.HBLANK, gameState.getCurrentState(), eventParam);
            if (super.lineCount == VSYNC_COUNT) {
                EventManager.getInstance().dispatchEvent(Event.EventType.VBLANK, gameState.getCurrentState(), eventParam);
            }
            super.lineCount++;
            if (super.lineCount >= SCAN_LINE_SIZE) {
                super.lineCount = 0;
                super.frameCount++;
                return false;
            }
            return true;
        }
    }
    private class PowerControlImpl extends PowerControl {
        public boolean isReset() {
            return resetFlag;
        }
        public boolean isPowerOff() {
            return offFlag;
        }
        public void init() {
            resetFlag = false;
            offFlag = false;
        }
    }


    private Map<String, Object> eventParam = new HashMap<>();

    private GameStateImpl gameState;
    private ScanStateImpl scanState;
    private PowerControlImpl powerControl;

    private ExecuteManager() {
        eventParam.put("byte", new Byte((byte) 0));
        eventParam.put("char", new Character((char) 0));
        eventParam.put("short", new Short((short) 0));
        eventParam.put("int", new Integer(0));
        eventParam.put("long", new Long(0));
        eventParam.put("float", new Float(0));
        eventParam.put("double", new Double(0));
        eventParam.put("boolean", Boolean.FALSE);
    }

    public ExecuteManager initialize(Class<?> romClass) {
        this.romClass = romClass;
        List<String> packages = new ArrayList<>();
        famicomRom = romClass.getAnnotation(FamicomRom.class);
        for (String pkg: famicomRom.packages()) {
            packages.add(pkg);
        }
        if (packages.size() == 0) {
            // デフォルト
            packages.add(romClass.getPackage().getName());
        }
        EventManager.getInstance().initialize(romClass, packages);
        return initGame(true);
    }

    private ExecuteManager initGame(boolean initFlag) {
        gameState = new GameStateImpl(famicomRom.initState());
        scanState = new ScanStateImpl();
        powerControl = new PowerControlImpl();
        eventParam.put(GameState.class.getName(), gameState);
        eventParam.put(ScanState.class.getName(), scanState);
        eventParam.put(PowerControl.class.getName(), powerControl);
        if (initFlag) {
            EventManager.getInstance().dispatchEvent(Event.EventType.INITIALIZE, "", eventParam);
        }
        EventManager.getInstance().dispatchEvent(Event.EventType.POST_RESET, "", eventParam);
        EventManager.getInstance().dispatchEvent(Event.EventType.ENTER_STATE, famicomRom.initState(), eventParam);
        return this;
    }

    public ExecuteManager nextFrame() {
        gameState.dispatchStateEvent();
        while (scanState.scanLine()) {
            // 何もしない
        }
        if (powerControl.isReset() || powerControl.isPowerOff()) {
            reset();
        }
        return this;
    }

    private ExecuteManager reset() {
        EventManager.getInstance().dispatchEvent(Event.EventType.PRE_RESET, "", eventParam);
        return initGame(false);
    }

    public FamicomRom getFamicomRom() {
        return famicomRom;
    }
    public Class<?> getRomClass() {
        return romClass;
    }
}
