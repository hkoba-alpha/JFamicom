package lrunner;

import famicom.api.pad.PadData;
import lrunner.data.StageData;
import lrunner.data.StageData.BlockData;
import lrunner.play.RunnerDeadException;

import java.util.ArrayList;
import java.util.HashMap;

public class RunnerData extends SpriteData {

	private int[][] holeCharacter = new int[][] {
			{ 19, 19, 18, 17, 16, 15, 14, 13, 12, 12, 11, 10 }, {},
			{ 14, 14, 13, 13, 13, 12, 12, 11, 11, 11, 10, 10 },
			{ 19, 19, 18, 18, 18, 17, 17, 16, 16, 16, 15, 15 } };

	/**
	 * 掘った穴のデータ
	 * 
	 * @author hkoba
	 *
	 */
	private class HoleData {
		private int bx;
		private int by;
		private int count;

		HoleData(int x, int y, int cnt) {
			bx = x;
			by = y;
			count = cnt;
		}

		boolean animate(StageData data) {
			count--;
			if (count == 0) {
				BlockData blk = data.getBlock(bx, by);
				blk.curBlock = StageData.BLK_BLOCK;
				blk.setCharaIndex(StageData.BLK_BLOCK);
			} else {
				int[] holeCount = data.getStageConfig().getHoleCount();
				for (int i = 1; i < holeCount.length; i++) {
					if (count == holeCount[i]) {
						// キャラクタを変える
						int ch = StageData.CHARA_FILL + i - 1;
						data.getBlock(bx, by).setCharaIndex(ch);
						break;
					}
				}
			}
			return count > 0;
		}
	}

	/**
	 * マイナスは左、プラスは右
	 */
	private int beamCount;

	/**
	 * 歩いているカウンタ
	 */
	private int walkCount;

	private ArrayList<HoleData> holeList;

	//private PsgSoundData beamSound;
	//private PsgSoundData fallSound;
	//private PsgSoundData goldSound;

	//private HashMap<IconType, PsgSoundData[]> walkSoundMap;

	public RunnerData(int bx, int by) {
		super(bx, by, StageData.BLK_PLAYER);
		entryIcon(IconType.Walk, new int[] { 0, 1, 2, 3 });
		entryIcon(IconType.Fall, new int[] { 4, 5, 6, 7 });
		entryIcon(IconType.UpDown, new int[] { 8, 9, 10, 11 });
		entryIcon(IconType.Bar, new int[] { 12, 13, 14, 15 });
		entryIcon(IconType.Beam, new int[] { 17 });
		entryIcon(IconType.None, new int[] { 18, 19 });
		entryIcon(IconType.BornDead,
				new int[] { 20, 21, 22, 23, 24, 25, 26, 27 });
		entryIcon(IconType.Clear, new int[] { 60, 61, 62, 63 });
		holeList = new ArrayList<HoleData>();
		/*
		beamSound = new PsgSoundData(
				SampleROM.class.getResourceAsStream("/sound4.txt"));
		fallSound = new PsgSoundData(
				SampleROM.class.getResourceAsStream("/fall.txt"));
		goldSound = new PsgSoundData(
				SampleROM.class.getResourceAsStream("/gold.txt"));
		walkSoundMap = new HashMap<IconType, PsgSoundData[]>();
		*/
		final String[] soundList = { "0:-- -- -- --|80 00 ab 11|",
				"0:-- -- -- --|80 00 80 12|", "0:-- -- -- --|80 00 14 10|",
				"0:-- -- -- --|80 00 28 10|", "0:-- -- -- --|80 00 50 10|",
				"0:-- -- -- --|80 00 a0 10|" };
		final IconType[] iconList = { IconType.Walk, IconType.UpDown,
				IconType.Bar };
				/*
		for (int i = 0; i < 6; i++) {
			PsgSoundData[] sample.data = walkSoundMap.get(iconList[i / 2]);
			if (sample.data == null) {
				sample.data = new PsgSoundData[2];
				walkSoundMap.put(iconList[i / 2], sample.data);
			}
			sample.data[i & 1] = new PsgSoundData(soundList[i]);
		}
		*/
	}

	@Override
	protected void blockMoved(int oldX, int oldY, int newX, int newY,
			StageData data) throws RunnerDeadException {
		if (data.getBlock(newX, newY).curBlock == StageData.BLK_ENEMY) {
			throw new RunnerDeadException();
		}
	}

	@Override
	protected void setIcon(IconData icon, MoveType newMove, int index,
			StageData data) {
		super.setIcon(icon, newMove, index, data);
		if (newMove == MoveType.MoveNone) {
			switch (icon.getIconType()) {
			case Fall:
				icon.setIcon(IconType.None);
				break;
			case None:
				if ((index & 0xf) == 0) {
					icon.animate();
				}
				break;
			case Walk:
				if (index >= 16) {
					icon.setIcon(IconType.None);
				}
				break;
			default:
				break;
			}
		} else if (beamCount == 0 && icon.getIconType() != IconType.Fall) {
			if ((walkCount & 3) == 0) {
				/*
				PsgSoundData[] sound = walkSoundMap.get(icon.getIconType());
				if (sound != null) {
					SampleROM.soundManager.addSequencer(5,
							sound[(walkCount >> 2) & 1], false);
				}
				*/
			}
			walkCount++;
		}
	}

	@Override
	protected MoveType checkMove(StageData data, int bx, int by, int stick,
			int button) {
		if (beamCount < 0) {
			if (canMove(data, MoveType.BeamLeft)) {
				return MoveType.BeamLeft;
			} else {
				// キャンセル
				//SampleROM.soundManager.removeSequencer(5);
				beamCount = 0;
				data.getBlock(bx - 1, by + 1)
						.setCharaIndex(StageData.BLK_BLOCK);
			}
		} else if (beamCount > 0) {
			if (canMove(data, MoveType.BeamRight)) {
				return MoveType.BeamRight;
			} else {
				// キャンセル
				//SampleROM.soundManager.removeSequencer(5);
				beamCount = 0;
				data.getBlock(bx + 1, by + 1)
						.setCharaIndex(StageData.BLK_BLOCK);
			}
		}
		if (isFall(data)) {
			/*
			if (!SampleROM.soundManager.isPlaying(fallSound)) {
				SampleROM.soundManager.addSequencer(5, fallSound, false);
			}
			*/
			return MoveType.MoveFall;
		} else {
			//SampleROM.soundManager.removeSequencer(5);
		}
		if ((button & PadData.ButtonType.B.getButtonFlag()) > 0) {
			// 左
			if (canMove(data, MoveType.BeamLeft)) {
				beamCount = -holeCharacter[0].length - 1;
				//SampleROM.soundManager.addSequencer(5, beamSound, false);
				return MoveType.BeamLeft;
			}
		} else if ((button & PadData.ButtonType.A.getButtonFlag()) > 0) {
			// 右
			if (canMove(data, MoveType.BeamRight)) {
				beamCount = holeCharacter[0].length + 1;
				//SampleROM.soundManager.addSequencer(5, beamSound, false);
				return MoveType.BeamRight;
			}
		}
		if ((stick & PadData.ButtonType.UP.getButtonFlag()) > 0) {
			if (canMove(data, MoveType.MoveUp)) {
				return MoveType.MoveUp;
			}
		} else if ((stick & PadData.ButtonType.DOWN.getButtonFlag()) > 0) {
			if (canMove(data, MoveType.MoveDown)) {
				return MoveType.MoveDown;
			}
		}
		if ((stick & PadData.ButtonType.LEFT.getButtonFlag()) > 0) {
			return MoveType.MoveLeft;
		} else if ((stick & PadData.ButtonType.RIGHT.getButtonFlag()) > 0) {
			return MoveType.MoveRight;
		}
		return MoveType.MoveNone;
	}

	private void setBeamCharacter(StageData data, int bx, int by, int count) {
		BlockData blk1 = data.getBlock(bx, by);
		BlockData blk2 = data.getBlock(bx, by - 1);
		int ch1 = StageData.BLK_SPACE;
		int ch2 = blk2.getCharaIndex();
		if (count == 0) {
			ch2 = ch1;
		} else {
			ch1 = holeCharacter[0][count - 1];
			if (ch2 == StageData.BLK_SPACE
					|| (ch2 >= StageData.CHARA_HOLE && ch2 < StageData.CHARA_FILL)) {
				int[] chlst = holeCharacter[1];
				if (count - 1 < chlst.length) {
					ch2 = chlst[count - 1];
				} else {
					ch2 = StageData.BLK_SPACE;
				}
			}
		}
		blk1.setCharaIndex(ch1);
		blk2.setCharaIndex(ch2);
	}

	@Override
	public void postMove(StageData data) throws RunnerDeadException {
		if (subX == 0 && subY == 0) {
			BlockData blk = data.getBlock(blockX, blockY);
			if (blk.orgBlock == StageData.BLK_GOLD) {
				// 金塊を獲得
				blk.orgBlock = StageData.BLK_SPACE;
				blk.setCharaIndex(StageData.BLK_SPACE);
				data.decrementGold();
				//SampleROM.soundManager.removeSequencer(goldSound);
				//SampleROM.soundManager.addSequencer(10, goldSound, false);
			}
		}
		for (int i = 0; i < holeList.size(); i++) {
			HoleData dt = holeList.get(i);
			if (!dt.animate(data)) {
				holeList.remove(i);
				i--;
			}
		}
		if (beamCount < 0) {
			beamCount++;
			setBeamCharacter(data, blockX - 1, blockY + 1, -beamCount);
			BlockData blk = data.getBlock(blockX - 1, blockY + 1);
			if (beamCount == 0) {
				blk.curBlock = StageData.BLK_SPACE;
				holeList.add(new HoleData(blockX - 1, blockY + 1, data
						.getStageConfig().getHoleCount()[0]));
			} else {
				int ch = StageData.CHARA_FILL - Math.abs(beamCount) / 2 - 1;
				if (ch < StageData.CHARA_HOLE) {
					ch = StageData.CHARA_HOLE;
				}
				ch = holeCharacter[0][-beamCount - 1];
				// blk.setCharaIndex(ch);
			}
		} else if (beamCount > 0) {
			beamCount--;
			setBeamCharacter(data, blockX + 1, blockY + 1, beamCount);
			BlockData blk = data.getBlock(blockX + 1, blockY + 1);
			if (beamCount == 0) {
				blk.curBlock = StageData.BLK_SPACE;
				// blk.setCharaIndex(StageData.BLK_SPACE);
				holeList.add(new HoleData(blockX + 1, blockY + 1, data
						.getStageConfig().getHoleCount()[0]));
			} else {
				int ch = StageData.CHARA_FILL - Math.abs(beamCount) / 2 - 1;
				if (ch < StageData.CHARA_HOLE) {
					ch = StageData.CHARA_HOLE;
				}
				ch = holeCharacter[0][beamCount - 1];
				// blk.setCharaIndex(ch);
			}
		}
		if (data.getBlock(blockX, blockY).curBlock == StageData.BLK_BLOCK) {
			// 壁に埋まった
			throw new RunnerDeadException();
		}
	}

	public void setDeadIcon(boolean initFlag) {
		if (initFlag) {
			iconData.setIcon(IconType.BornDead);
		} else {
			iconData.animate();
		}
	}

	public IconData getIconData() {
		return iconData;
	}

	public int moveY(int addY) {
		subY += addY;
		return getPoint().y;
	}
}
