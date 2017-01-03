package lrunner.play;

import famicom.api.pad.IFamicomPad;
import famicom.api.ppu.IFamicomPPU;
import lrunner.data.StageData;

import java.awt.*;
import java.io.InputStream;

public class StageStart implements PlayBase {
	private StageData stageData;

	private PlayData playData;

	public enum ScrollType {
		ViewStart, ViewRight, ViewDown, ViewLeft, ViewUp, StartHorizontal, StartVertical
	}

	private ScrollType scrollType;

	private int addX;

	public StageStart(StageData data, PlayData play) {
		this(data, play, ScrollType.ViewStart);
		/*
		InputStream is = SampleROM.class.getResourceAsStream("/sound2.txt");
		SampleROM.soundManager.removeSequencer(0);
		SampleROM.soundManager.addSequencer(0, new PsgSoundData(is), true);
		*/
	}

	public StageStart(StageData data, PlayData play, ScrollType tp) {
		stageData = data;
		playData = play;
		scrollType = tp;
		addX = data.getHeight() > 14 ? 2: 1;
		addX = 2;
	}

	@Override
	public PlayBase stepFrame(IFamicomPad pad, IFamicomPPU ppu) {
		Point pos = stageData.getScrollPosition();
		Dimension sz = stageData.getScrollSize();
		Rectangle area = stageData.getPrefferedArea();
		Point newpos = null;
		boolean flag = false;
		while (newpos == null) {
			switch(scrollType) {
			case ViewStart:
				flag = true;
				newpos = new Point(0, 0);
				scrollType = ScrollType.ViewRight;
				break;
			case ViewRight:
				if (pos.x < sz.width - 256) {
					newpos = new Point(pos.x + addX, pos.y);
				} else {
					scrollType = ScrollType.ViewDown;
				}
				break;
			case ViewDown:
				if (pos.y < sz.height - 240) {
					newpos = new Point(pos.x, pos.y + 1);
				} else {
					scrollType = ScrollType.ViewLeft;
				}
				break;
			case ViewLeft:
				if (pos.x > 0) {
					newpos = new Point(pos.x - addX, pos.y);
				} else {
					scrollType = ScrollType.ViewUp;
				}
				break;
			case ViewUp:
				if (pos.y > 0) {
					newpos = new Point(pos.x, pos.y - 1);
				} else {
					scrollType = ScrollType.StartHorizontal;
				}
				break;
			case StartHorizontal:
				if (pos.x < area.x) {
					newpos = new Point(pos.x + addX, pos.y);
				} else if (pos.x > area.x + area.width) {
					newpos = new Point(pos.x - addX, pos.y);
				} else {
					scrollType = ScrollType.StartVertical;
				}
				break;
			case StartVertical:
				if (pos.y < area.y) {
					newpos = new Point(pos.x, pos.y + 1);
				} else if (pos.y > area.y + area.height) {
					newpos = new Point(pos.x, pos.y - 1);
				} else {
					return playData;
				}
				break;
			}
		}
		stageData.setScrollPosition(ppu, newpos.x, newpos.y);
		stageData.drawStage(ppu, flag);
		return this;
	}

}
