package famicom.impl.game;

import famicom.api.core.ComponentManager;
import famicom.api.core.ExecuteManager;
import famicom.api.core.RomManager;
import famicom.impl.pad.FamicomPadImpl;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.command.Control;
import org.newdawn.slick.command.KeyControl;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import java.util.List;

/**
 * Created by hkoba on 2017/01/09.
 */
public class FamicomTitleState extends BasicGameState {
    public static final int STATE_ID = 0;

    private List<RomManager.RomData> romDataList;

    public FamicomTitleState(List<RomManager.RomData> romList) {
        romDataList = romList;
    }

    @Override
    public int getID() {
        return STATE_ID;
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {

    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {

    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        if (container.getInput().isMousePressed(0)) {

            Class<?> romClass;
            try {
                romClass = romDataList.get(0).getRomClass();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return;
            }

            ComponentManager.getInstance().scanComponent(romClass);
            ExecuteManager.getInstance().initialize(romClass);
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
            game.enterState(FamicomMainState.STATE_ID);
        }
    }
}
