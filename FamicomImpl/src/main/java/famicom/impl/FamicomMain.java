package famicom.impl;

import famicom.api.annotation.*;
import famicom.api.core.AnnotationUtil;
import famicom.api.core.ComponentManager;
import famicom.api.core.EventManager;
import famicom.api.core.ExecuteManager;
import famicom.api.state.GameState;
import famicom.api.state.ScanState;

import java.lang.annotation.Annotation;

/**
 * Created by hkoba on 2016/12/31.
 */
@FamicomRom(name = "テストメイン", initState = "title")
public class FamicomMain {

    @Initialize
    @PostReset
    private void init(int a, boolean b, GameState state, ScanState scan) {
        System.out.println("Init:" + state + ", scan=" + scan);
    }

    public static void main(String[] args) {
        ComponentManager.getInstance().scanComponent(FamicomMain.class);
        ExecuteManager.getInstance().initialize(FamicomMain.class);
    }
}
