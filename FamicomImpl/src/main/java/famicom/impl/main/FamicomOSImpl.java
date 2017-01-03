package famicom.impl.main;

import famicom.api.annotation.FamicomLibrary;
import famicom.api.core.ComponentManager;
import famicom.api.core.ExecuteManager;
import famicom.api.core.RomManager;
import famicom.api.main.IFamicomOS;
import famicom.impl.LibraryLoader;
import famicom.impl.game.FamicomContainer;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;

import java.util.List;

/**
 * Created by hkoba on 2017/01/02.
 */
@FamicomLibrary(priority = 0)
public class FamicomOSImpl implements IFamicomOS {
    @Override
    public void execute(String[] args, List<RomManager.RomData> romDataList) {
        LibraryLoader.init();

        Class<?> romClass;
        try {
            romClass = romDataList.get(0).getRomClass();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        ComponentManager.getInstance().scanComponent(romClass);
        ExecuteManager.getInstance().initialize(romClass);

        try {
            AppGameContainer container = new AppGameContainer(new FamicomContainer());
            container.setDisplayMode(256 * 2, 224 * 2, false);
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
