package famicom.impl;

import famicom.api.annotation.*;
import famicom.api.core.AnnotationUtil;
import famicom.api.core.ComponentManager;
import famicom.api.core.EventManager;
import famicom.api.core.ExecuteManager;
import famicom.api.ppu.IFamicomPPU;
import famicom.api.state.GameState;
import famicom.api.state.ScanState;
import famicom.impl.game.FamicomContainer;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;

import java.lang.annotation.Annotation;

/**
 * Created by hkoba on 2016/12/31.
 */
public class FamicomMain {
    @FamicomRom(name = "Testrom")
    public static class TestRom {
        @Attach
        private IFamicomPPU famicomPPU;

        @Initialize
        private void init() {
            for (int i = 0; i < 256 * 16; i++) {
                famicomPPU.getPatternTable(0).write(i, (int)(Math.random() * 256));
            }
            for (int i = 0; i < 32 * 32; i++) {
                famicomPPU.getNameTable(0).write(i, (int)(Math.random() * 256));
            }
            for (int i = 0; i < 16; i++) {
                famicomPPU.getPaletteTable().write(i, (int)(Math.random() * 256));
            }
            famicomPPU.getControlData().setSpriteEnabled(true).setSpriteSize(1).setScreenMask(true);
            famicomPPU.getPatternTable(0)
                    .write(0, 0x7e)
                    .write(1, 0xc3)
                    .write(2, 0xff)
                    .write(3, 0x3c);
            famicomPPU.getPaletteTable()
                    .write(17, 5)
                    .write(18, 6)
                    .write(19, 24);
            famicomPPU.getSpriteData(0).setY(30).setX(80).setColor(0);
        }
    }
    public static void main(String[] args) {
        LibraryLoader.init();

        ComponentManager.getInstance().scanComponent(FamicomMain.class);
        ExecuteManager.getInstance().initialize(TestRom.class).nextFrame();

        try {
            AppGameContainer container = new AppGameContainer(new FamicomContainer());
            container.setDisplayMode(512, 448, false);
            container.setTargetFrameRate(60);
            container.setVSync(true);
            container.setShowFPS(true);
            //container.setDefaultFont(new UnicodeFont(new Font("Dialog", Font.BOLD, 14)));
            container.start();
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }
}
