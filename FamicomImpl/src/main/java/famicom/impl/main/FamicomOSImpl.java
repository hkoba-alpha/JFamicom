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

        try {
            AppGameContainer container = new AppGameContainer(new FamicomContainer(romDataList));
            container.setDisplayMode(256 * 2, 224 * 2, false);
            container.setTargetFrameRate(60);
            container.setVSync(true);
            container.setShowFPS(true);
            container.start();
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }
}
