package famicom.impl.game;

import famicom.api.core.ComponentManager;
import famicom.api.core.ExecuteManager;
import famicom.impl.pad.FamicomPadImpl;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.command.Control;
import org.newdawn.slick.command.KeyControl;
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
        ComponentManager.getInstance().getObject(FamicomPadImpl.class).initPad(container.getInput(), new Control[][] {
                {
                        new KeyControl(Input.KEY_X),
                        new KeyControl(Input.KEY_Z),
                        new KeyControl(Input.KEY_SPACE),
                        new KeyControl(Input.KEY_ENTER),
                        new KeyControl(Input.KEY_UP),
                        new KeyControl(Input.KEY_DOWN),
                        new KeyControl(Input.KEY_LEFT),
                        new KeyControl(Input.KEY_RIGHT)
                },
                {}
        });
    }
}
