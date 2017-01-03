package lrunner.play;

import famicom.api.pad.IFamicomPad;
import famicom.api.pad.PadData;
import famicom.api.ppu.IFamicomPPU;
import lrunner.data.StageData;

import java.util.Arrays;

public class StartPlay implements PlayBase {
	private StageData stageData;

	//private PsgSoundData startSound;

	private int frameCount;

	public StartPlay(StageData data) {
		stageData = data;
		/*
		SampleROM.soundManager.removeSequencer(0).removeSequencer(5)
				.removeSequencer(10);
				*/
	}

	@Override
	public PlayBase stepFrame(IFamicomPad pad, IFamicomPPU ppu) {
		if (frameCount == 0) {
			/*
			startSound = new PsgSoundData(
					StartPlay.class.getResourceAsStream("/start_stage.txt"));
			SampleROM.soundManager.addSequencer(0, startSound, false);
			*/
			ppu.getControlData().setScroll(0, 0);
			byte[] blank = new byte[32 * 30];
			Arrays.fill(blank, (byte) 0x60);
			for (int i = 0; i < blank.length; i++) {
				//blank[i] = (byte)(i & 255);
			}
			ppu.getNameTable(0).write(0, blank.length,
					blank);
			for (int i = 0; i < 64; i++) {
				ppu.getSpriteData(i).setY(240);
			}
			StageData.drawString(ppu, 12, 8, "PLAYER 1");
			StageData.drawString(ppu, 8, 14, String.format("STAGE %2d LEFT 5", stageData.getStageNum()));
			StageData.drawString(ppu, 8, 18, "SCORE    00000000");
			StageData.drawString(ppu, 8, 20, "HISCORE  00000000");
			stageData.initStage();
		} else if (frameCount == 100) {
			return new StageStart(stageData, new PlayData(stageData));
		}
		if (pad.getPad(0).isDown(PadData.ButtonType.START)) {
			return new SelectPlay(stageData);
		}
		frameCount++;
		return this;
	}

}
