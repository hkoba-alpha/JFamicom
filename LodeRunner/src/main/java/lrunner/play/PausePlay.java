package lrunner.play;

import famicom.api.pad.IFamicomPad;
import famicom.api.pad.PadData;
import famicom.api.ppu.IFamicomPPU;
import lrunner.data.StageData;

import java.awt.*;

public class PausePlay implements PlayBase {
	private StageData stageData;

	private PlayData playData;

	private boolean replayFlag;

	public PausePlay(StageData data, PlayData play) {
		stageData = data;
		playData = play;
	}

	@Override
	public PlayBase stepFrame(IFamicomPad pad, IFamicomPPU ppu) {
		if (replayFlag) {
			if (pad.getPad(0).isDown(PadData.ButtonType.START)) {
				return new StageStart(stageData, playData, StageStart.ScrollType.StartHorizontal);
			}
		} else if (pad.getPad(0).isDown(PadData.ButtonType.START)) {
			replayFlag = true;
		}

		PadData stick = pad.getPad(0);
		Point pos = stageData.getScrollPosition();
		Dimension sz = stageData.getScrollSize();
		if (stick.isDown(PadData.ButtonType.LEFT) && pos.x > 0) {
			pos.x -= 2;
		} else if (stick.isDown(PadData.ButtonType.RIGHT) && pos.x < sz.width - 256) {
			pos.x += 2;
		}
		if (stick.isDown(PadData.ButtonType.UP) && pos.y > 0) {
			pos.y -= 2;
		} else if (stick.isDown(PadData.ButtonType.DOWN) && pos.y < sz.height - 240) {
			pos.y += 2;
		}
		stageData.setScrollPosition(ppu, pos.x, pos.y);
		stageData.drawStage(ppu, false);
		return this;
	}

}
