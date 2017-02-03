package famicom.impl.game;

import famicom.api.core.ExecuteManager;
import org.newdawn.slick.*;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

/**
 * Created by hkoba on 2017/01/01.
 */
public class FamicomMainState extends BasicGameState {
    public static final int STATE_ID = 1;

    private static final FamicomMainState thisInstance = new FamicomMainState();

    private ImageBuffer imageBuffer;
    private Color bgColor;

    public static FamicomMainState getInstance() {
        return thisInstance;
    }

    private FamicomMainState() {
        bgColor = Color.black;
    }

    public void setImageBuffer(ImageBuffer buffer) {
        imageBuffer = buffer;
    }

    public void setBgColor(int r, int g, int b) {
        bgColor = new Color(r, g, b);
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
        g.setColor(bgColor);
        g.fillRect(0, 0, 256 * 2, 224 * 2);
        g.drawImage(imageBuffer.getImage(), 0, 0);
        //g.drawImage(imageBuffer.getImage(), 0, 0, 256 * 3, 224 * 3, 0, 0, 512, 448);
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        ExecuteManager.getInstance().nextFrame();
    }
}
