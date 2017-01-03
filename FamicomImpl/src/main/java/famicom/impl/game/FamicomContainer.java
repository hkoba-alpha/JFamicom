package famicom.impl.game;

import famicom.api.core.ExecuteManager;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

/**
 * Created by hkoba on 2017/01/01.
 */
public class FamicomContainer extends StateBasedGame {
    public FamicomContainer() {
        super(ExecuteManager.getInstance().getFamicomRom().name());
    }

    @Override
    public void initStatesList(GameContainer container) throws SlickException {
        super.addState(FamicomMainState.getInstance());
    }
}
