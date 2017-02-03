package famicom.impl.game;

import famicom.api.core.ComponentManager;
import famicom.api.core.ExecuteManager;
import famicom.api.core.RomManager;
import famicom.impl.pad.FamicomPadImpl;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.command.Control;
import org.newdawn.slick.command.KeyControl;
import org.newdawn.slick.state.StateBasedGame;

import java.util.List;

/**
 * Created by hkoba on 2017/01/01.
 */
public class FamicomContainer extends StateBasedGame {
    private List<RomManager.RomData> romDataList;
    private String romTitle = "Javaファミコン";

    public FamicomContainer(List<RomManager.RomData> romList) {
        super("Javaファミコン");
        romDataList = romList;
    }

    @Override
    public String getTitle() {
        return romTitle;
    }

    @Override
    public void enterState(int id) {
        super.enterState(id);
        romTitle = ExecuteManager.getInstance().getFamicomRom().name();
    }

    @Override
    public void initStatesList(GameContainer container) throws SlickException {
        super.addState(new FamicomTitleState(romDataList));
        super.addState(FamicomMainState.getInstance());
    }
}
