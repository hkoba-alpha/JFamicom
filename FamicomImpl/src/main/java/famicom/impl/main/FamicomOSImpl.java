package famicom.impl.main;

import famicom.api.annotation.FamicomLibrary;
import famicom.api.annotation.VBlank;
import famicom.api.core.ComponentManager;
import famicom.api.core.ExecuteManager;
import famicom.api.core.RomManager;
import famicom.api.main.IFamicomOS;
import famicom.api.state.PowerControl;
import famicom.impl.LibraryLoader;
import famicom.impl.game.FamicomContainer;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Created by hkoba on 2017/01/02.
 */
@FamicomLibrary(priority = 0)
public class FamicomOSImpl implements IFamicomOS, ActionListener {
    private boolean resetFlag;

    @Override
    public void execute(String[] args, List<RomManager.RomData> romDataList) {
        LibraryLoader.init();

        try {
            JFrame frame = new JFrame();
            MenuBar menuBar = new MenuBar();
            Menu fileMenu = new Menu("ファイル");
            MenuItem resetMenu = new MenuItem("リセット");
            resetMenu.addActionListener(this);
            fileMenu.add(resetMenu);
            menuBar.add(fileMenu);
            frame.setMenuBar(menuBar);
            Canvas canvas = new Canvas();
            canvas.setPreferredSize(new Dimension(256 * 2, 224 * 2));
            frame.getContentPane().add(canvas);
            frame.setResizable(false);
            frame.pack();
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Display.setParent(canvas);

            AppGameContainer container = new AppGameContainer(new FamicomContainer(romDataList));
            container.setDisplayMode(256 * 2, 224 * 2, false);
            container.setTargetFrameRate(60);
            container.setVSync(true);
            container.setShowFPS(true);
            container.start();
        } catch (SlickException e) {
            e.printStackTrace();
        } catch (LWJGLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        resetFlag = true;
    }

    @VBlank
    public void check(PowerControl control) {
        if (resetFlag) {
            control.reset();
            resetFlag = false;
        }
    }
}
