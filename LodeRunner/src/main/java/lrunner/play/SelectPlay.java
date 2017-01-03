package lrunner.play;

import famicom.api.pad.IFamicomPad;
import famicom.api.pad.PadData;
import famicom.api.ppu.IFamicomPPU;
import lrunner.data.StageConfig;
import lrunner.data.StageData;

import java.util.Arrays;

public class SelectPlay implements PlayBase {
	private StageData stageData;

	//private PsgSoundData soundData;

	private boolean pushFlag;

	private boolean firstFlag;

	public SelectPlay(StageData data) {
		stageData = data;
		firstFlag = true;
		/*
		SampleROM.soundManager.removeSequencer(0).removeSequencer(5).removeSequencer(10);
		*/
	}

	@Override
	public PlayBase stepFrame(IFamicomPad pad, IFamicomPPU ppu) {
		if (firstFlag) {
			//soundData = new PsgSoundData("     0:-- -- -- --|80 00 a0 10|");
			ppu.getControlData().setScroll(0, 0);
			byte[] blank = new byte[32 * 30];
			Arrays.fill(blank, (byte) 0x60);
			ppu.getNameTable(0).write(0, blank.length,
					blank);
			for (int i = 0; i < 64; i++) {
				ppu.getSpriteData(i).setY(240);
			}
			StageData.drawString(ppu, 8, 14, String.format("STAGE %2d", stageData.getStageNum()));
		}
		int button = pad.getPad(0).getButton();
		if (pushFlag) {
			if (button == 0) {
				pushFlag = false;
			}
		} else if (button > 0) {
			StageConfig config = stageData.getStageConfig();
			if ((button & PadData.ButtonType.A.getButtonFlag()) > 0) {
				// 追加
				int num = stageData.getStageNum() + 1;
				if (num > config.getStageMax()) {
					num = 1;
				}
				stageData = config.loadStage(num);
				//SampleROM.soundManager.addSequencer(0, soundData, false);
			} else if ((button & PadData.ButtonType.B.getButtonFlag()) > 0) {
				int num = stageData.getStageNum() - 1;
				if (num < 1) {
					num = config.getStageMax();
				}
				stageData = config.loadStage(num);				
				//SampleROM.soundManager.addSequencer(0, soundData, false);
			} else if ((button & PadData.ButtonType.START.getButtonFlag()) > 0) {
				// 開始
				return new StartPlay(stageData);
			}
			if (stageData == null) {
				// ステージ１にする
				stageData = config.loadStage(1);
			}
			StageData.drawString(ppu, 8, 14, String.format("STAGE %2d", stageData.getStageNum()));
			pushFlag = true;
		}
		return this;
	}

}
