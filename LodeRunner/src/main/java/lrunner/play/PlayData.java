package lrunner.play;

import famicom.api.pad.IFamicomPad;
import famicom.api.pad.PadData;
import famicom.api.ppu.IFamicomPPU;
import lrunner.EnemyData;
import lrunner.RunnerData;
import lrunner.data.StageData;

import java.awt.*;
import java.util.ArrayList;

public class PlayData implements PlayBase {
	private RunnerData runner;

	private ArrayList<EnemyData> enemyList;

	private StageData stageData;

	private int frameNum;

	private int enemyIndex;
	private int enemyMoveFlag;

	private boolean escapeFlag;

	private boolean pauseFlag;

	public PlayData(StageData data) {
		stageData = data;
		runner = data.getRunner();
		enemyList = data.getEnemyList();
		enemyMoveFlag = data.getStageConfig().getEnemyMoveFlag(enemyList.size());
	}

	@Override
	public PlayBase stepFrame(IFamicomPad pad, IFamicomPPU ppu) {
		int stick = pad.getPad(0).getStick();
		int button = pad.getPad(0).getButton();
		if ((button & PadData.ButtonType.START.getButtonFlag()) > 0) {
			if (pauseFlag) {
				pauseFlag = false;
				return new PausePlay(stageData, this);
			}
		} else if ((button & PadData.ButtonType.SELECT.getButtonFlag()) > 0) {
			// ステージ選択
			return new SelectPlay(stageData);
		} else {
			pauseFlag = true;
		}
		frameNum++;
		if ((frameNum & 1) != 0) {
			return this;
		}
		try {
			runner.move(stageData, stick, button);
			runner.postMove(stageData);

			int flag = enemyMoveFlag & 0xff;
			for (int i = 0; i < 8; i++) {
				if ((enemyMoveFlag & 1) > 0) {
					enemyList.get(enemyIndex).move(stageData, stick, button);
					enemyIndex = (enemyIndex + 1) % enemyList.size();
				}
				enemyMoveFlag >>= 1;
			}
			enemyMoveFlag |= (flag << 16);
			for (EnemyData ene : enemyList) {
				ene.postMove(stageData);
			}
		} catch (RunnerDeadException e) {
			stageData.drawStage(ppu, false);
			return new RunnerDead(stageData);
		}
		// クリアチェック
		if (!escapeFlag && stageData.getGoldCount() == 0) {
			// 脱出梯子を作る
			for (int y = 0; y < stageData.getHeight(); y++) {
				for (int x = 0; x < stageData.getWidth(); x++) {
					StageData.BlockData blk = stageData.getBlock(x, y);
					if (blk.stageBlock == StageData.BLK_ESCAPE) {
						blk.orgBlock = StageData.BLK_STEP;
						if (blk.curBlock == StageData.BLK_SPACE) {
							blk.curBlock = blk.orgBlock;
						}
						blk.setCharaIndex(blk.orgBlock);
					}
				}
			}
			escapeFlag = true;
			/*
			SampleROM.soundManager.addSequencer(10, new PsgSoundData(
					PlayData.class.getResourceAsStream("/clear.txt")), false);
					*/
		}

		Rectangle area = stageData.getPrefferedArea();
		Point pos = stageData.getScrollPosition();
		int sx = pos.x;
		int sy = pos.y;
		if (sx < area.x) {
			sx = area.x;
		} else if (sx > area.x + area.width) {
			sx = area.x + area.width;
		}
		if (sy < area.y) {
			sy = area.y;
		} else if (sy > area.y + area.height) {
			sy = area.y + area.height;
		}
		stageData.setScrollPosition(ppu, sx, sy);
		stageData.drawStage(ppu, false);
		if (escapeFlag && runner.getPoint().y <= 0) {
			// Clear
			return new ClearPlay(stageData);
		}
		return this;
	}
}
