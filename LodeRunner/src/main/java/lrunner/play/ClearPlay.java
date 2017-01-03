package lrunner.play;

import famicom.api.pad.IFamicomPad;
import famicom.api.ppu.IFamicomPPU;
//import famicom.util.apu.PsgSoundData;
import lrunner.SpriteData.IconType;
import lrunner.data.StageData;

public class ClearPlay implements PlayBase {
	private StageData stageData;

	private int frameCount;

	//private PsgSoundData clearSound;

	private StageData nextStage;

	public ClearPlay(StageData data) {
		stageData = new StageData(
				ClearPlay.class
						.getResourceAsStream("/lrunner/clear.txt"),
				StageData.BLK_SPACE, null);
		int num = data.getStageNum();
		nextStage = data.getStageConfig().loadStage(num + 1);
		if (nextStage == null) {
			nextStage = data.getStageConfig().loadStage(1);
		}
		stageData.initStage();
		frameCount = 0;
		//clearSound = new PsgSoundData(ClearPlay.class.getResourceAsStream("/clear_stage.txt"));
		//SampleROM.soundManager.removeSequencer(0).removeSequencer(5).removeSequencer(10).addSequencer(0, clearSound, false);
	}

	@Override
	public PlayBase stepFrame(IFamicomPad pad, IFamicomPPU ppu) {
		if (frameCount == 0) {
			for (int i = 0; i < 64; i++) {
				ppu.getSpriteData(i).setY(240);
			}
			ppu.getControlData().setScroll(0, 0);
			stageData.getRunner().getIconData().setIcon(IconType.UpDown);
		}
		stageData.drawStage(ppu, frameCount == 0);
		if (frameCount == 0) {
			StageData.drawString(ppu, 14, 7, "    0 POINTS");
			StageData.drawString(ppu, 14, 11, "    0 POINTS");
			StageData.drawString(ppu, 8, 19, "TOTAL     0 POINTS");
		}
		frameCount++;
		if (frameCount < 64 * 4) {
			if ((frameCount & 7) == 0) {
				stageData.getRunner().moveY(-2);
				stageData.getRunner().getIconData().animate();
			}
		} else if (frameCount == 64 * 4) {
			stageData.getRunner().getIconData().setIcon(IconType.Clear);
		} else {
			if ((frameCount & 3) == 0) {
				stageData.getRunner().getIconData().animate();
			}
			if (frameCount > 600) {
				return new StartPlay(nextStage);
			}
		}
		return this;
	}

}
