package lrunner.play;

import famicom.api.pad.IFamicomPad;
import famicom.api.ppu.IFamicomPPU;
//import famicom.util.apu.PsgSoundData;
import lrunner.data.StageData;

public class RunnerDead implements PlayBase {
	private StageData stageData;

	private int deadCount;

	//private PsgSoundData deadSound;

	public RunnerDead(StageData data) {
		stageData = data;
		/*
		deadSound = new PsgSoundData(
				RunnerDead.class.getResourceAsStream("/dead.txt"));
		SampleROM.soundManager.removeSequencer(0).removeSequencer(5)
				.removeSequencer(10).addSequencer(1, deadSound, false);
				*/
	}

	@Override
	public PlayBase stepFrame(IFamicomPad pad, IFamicomPPU ppu) {
		if ((deadCount & 7) == 0) {
			if (deadCount < 64) {
				stageData.getRunner().setDeadIcon(deadCount == 0);
			} else {
				// 消す
				stageData.getRunner().hideSprite();
			}
		}
		deadCount++;
		stageData.drawStage(ppu, false);
		if (deadCount > 100) {
			// 終了
			return new StartPlay(stageData);
		}
		return this;
	}

}
