package lrunner;

import lrunner.data.StageData;
import lrunner.data.StageData.BlockData;
import lrunner.play.RunnerDeadException;

import java.awt.*;
import java.util.ArrayList;

public class EnemyData extends SpriteData {
	/**
	 * 敵が穴から這い上がる時の揺れ始めるカウント
	 */
	public static final int HOLE_SWAY_COUNT = 13;

	/**
	 * 敵が這い上がり始めるカウント
	 */
	public static final int HOLE_ESCAPE_COUNT = 8;

	/**
	 * 金塊の輸送カウント
	 */
	private int goldCount;

	/**
	 * 穴から這い出すカウント
	 */
	private int holeUpCount;

	/**
	 * 移動できないフラグ
	 */
	private boolean waitFlag;

	private int bornCount;

	private static BlockData outsideBlock;
	private static BlockData underBlock;

	static {
		outsideBlock = new BlockData();
		outsideBlock.orgBlock = outsideBlock.curBlock = StageData.BLK_SPACE;
		outsideBlock.stageBlock = -1;
		underBlock = new BlockData();
		underBlock.orgBlock = underBlock.curBlock = StageData.BLK_STONE;
		underBlock.stageBlock = -1;
	}

	public EnemyData(int bx, int by) {
		super(bx, by, StageData.BLK_ENEMY);
		// limitRight = 6;
		entryIcon(IconType.Walk, new int[] { 28, 29, 30, 31 });
		entryIcon(IconType.Fall, new int[] { 32, 33, 34, 35 });
		entryIcon(IconType.UpDown, new int[] { 36, 37, 38, 39 });
		entryIcon(IconType.Bar, new int[] { 40, 41, 42, 43 });
		entryIcon(IconType.BornDead, new int[] { 44, 45, 46, 47 });
	}

	@Override
	protected void blockMoved(int oldX, int oldY, int newX, int newY,
			StageData data) throws RunnerDeadException {
		if (data.getBlock(newX, newY).curBlock == StageData.BLK_PLAYER) {
			throw new RunnerDeadException();
		}
		if (newY > oldY) {
			// 落ちた
			BlockData blk = data.getBlock(newX, newY);
			if (blk.orgBlock == StageData.BLK_BLOCK
					&& blk.curBlock == StageData.BLK_SPACE) {
				// 穴に落ちた
				holeUpCount = data.getStageConfig().getEnemyHoleUpCount();
				waitFlag = true;
				// ゴールドをチェックする
				if (goldCount > 0) {
					BlockData blk2 = data.getBlock(oldX, oldY);
					if (blk2.orgBlock == StageData.BLK_SPACE) {
						// ゴールドを吐き出す
						blk2.orgBlock = blk2.curBlock = StageData.BLK_GOLD;
						blk2.setCharaIndex(StageData.BLK_GOLD);
					} else {
						data.decrementGold();
					}
					goldCount = 0;
				}
				return;
			}
		} else if (goldCount == 1) {
			// 置く
			BlockData blk = data.getBlock(oldX, oldY);
			if (blk.orgBlock == StageData.BLK_SPACE) {
				blk.orgBlock = blk.curBlock = StageData.BLK_GOLD;
				blk.setCharaIndex(StageData.BLK_GOLD);
				goldCount = 0;
			}
		} else if (holeUpCount > 0) {
			// 穴から這い上がっていて、かつ今のブロックが掘ったレンガなら
			BlockData blk = data.getBlock(newX, newY);
			if (blk.orgBlock == StageData.BLK_BLOCK) {
				holeUpCount = 4;
				return;
			}
		}
		waitFlag = false;
	}

	@Override
	protected MoveType checkMove(StageData data, int bx, int by, int stick,
			int button) {
		if (holeUpCount > 0) {
			holeUpCount--;
		}
		if (waitFlag) {
			if (bornCount > 0) {
				return MoveType.MoveNone;
			}
			if (holeUpCount == 0) {
				// 穴から這い上がれなかった
				if (subY > 0) {
					waitFlag = false;
				} else {
					holeUpCount = data.getStageConfig().getEnemyHoleUpCount();
				}
			} else if (holeUpCount <= HOLE_ESCAPE_COUNT) {
				subX = 0;
				return MoveType.MoveUp;
			} else if (holeUpCount <= HOLE_SWAY_COUNT) {
				subX = 1 - (holeUpCount & 1) * 2;
			}
			if (subY < 0) {
				return MoveType.MoveFall;
			}
			return MoveType.MoveNone;
		}
		if (holeUpCount == 0 && isFall(data)) {
			return MoveType.MoveFall;
		}
		Point runner = data.getRunner().getBlockPoint();
		MoveType mv = checkAdjoining(data, runner);
		if (mv != null) {
			return mv;
		}
		ArrayList<Point> pointList = checkHorizontalArea(data, runner);
		Point pt = getPriorityPoint(pointList, runner);
		if (pt != null) {
			if (pt.x < blockX) {
				return MoveType.MoveLeft;
			} else if (pt.x > blockX) {
				return MoveType.MoveRight;
			} else if (pt.y < blockY) {
				return MoveType.MoveUp;
			}
		}
		return MoveType.MoveDown;
	}

	@Override
	public void postMove(StageData data) {
		// 穴に埋まったチェック
		if (bornCount > 0) {
			if (getBlock(data, blockX, blockY).curBlock != StageData.BLK_SPACE) {
				return;
			}
			if ((bornCount & 3) == 0) {
				iconData.animate();
			}
			bornCount--;
			if (bornCount == 0) {
				waitFlag = false;
				iconData.setIcon(IconType.Fall);
				data.getBlock(blockX, blockY).curBlock = StageData.BLK_ENEMY;
			}
		} else if (data.getBlock(blockX, blockY).curBlock == StageData.BLK_BLOCK) {
			// 埋まった
			if (goldCount > 0) {
				// 金塊を持っていた
				data.decrementGold();
			}
			bornCount = 15;
			waitFlag = true;
			holeUpCount = 0;
			iconData.setRightFlag(false);
			iconData.setIcon(IconType.BornDead);
			subX = subY = 0;
			int bx = data.nextRandom(28);
			int by = 1;
			while (by < data.getHeight()) {
				BlockData blk = getBlock(data, bx, by);
				if (blk.orgBlock == StageData.BLK_SPACE) {
					// ここに決定
					blockX = bx;
					blockY = by;
					break;
				}
				bx++;
				if (bx >= data.getWidth()) {
					bx = 0;
					by++;
				}
			}
		} else if (goldCount == 0 && subX == 0 && subY == 0) {
			BlockData blk = data.getBlock(blockX, blockY);
			if (blk.orgBlock == StageData.BLK_GOLD) {
				// 金塊だった
				goldCount = data.nextRandom(data.getStageConfig().getEnemyGoldCount()) + 3;
				blk.orgBlock = StageData.BLK_SPACE;
				blk.setCharaIndex(StageData.BLK_SPACE);
			}
		}
	}

	@Override
	protected void setIcon(IconData icon, MoveType newMove, int index,
			StageData data) {
		if (newMove != MoveType.MoveNone && goldCount > 1) {
			goldCount--;
		}
		super.setIcon(icon, newMove, index, data);
	}

	/**
	 * 地続きのチェック
	 * 
	 * @param data
	 * @return
	 */
	private MoveType checkAdjoining(StageData data, Point runner) {
		if (blockY != runner.y) {
			return null;
		}
		int bx = blockX;
		int ax = 1;
		if (runner.x < bx) {
			ax = -1;
		}
		bx += ax;
		while (bx != runner.x) {
			switch (getBlock(data, bx, blockY + 1).orgBlock) {
			case StageData.BLK_BLOCK:
			case StageData.BLK_STONE:
			case StageData.BLK_STEP:
			case StageData.BLK_BAR:
			case StageData.BLK_GOLD:
				break;
			default: {
				int blk = getBlock(data, bx, blockY).orgBlock;
				if (blk != StageData.BLK_STEP && blk != StageData.BLK_BAR) {
					// 地続きではない
					return null;
				}
			}
			}
			bx += ax;
		}
		return ax < 0 ? MoveType.MoveLeft : MoveType.MoveRight;
	}

	/**
	 * 縦視界の行けるところをチェック
	 * 
	 * @param data
	 * @param bx
	 * @param pointList
	 * @param runner
	 * @return true:まだ続きがある, false:ここで終了
	 */
	private boolean checkVerticalMovePoint(StageData data, int bx,
			ArrayList<Point> pointList, Point runner) {
		boolean retFlag = true;
		BlockData blk = getBlock(data, bx, blockY);
		if (blk.curBlock == StageData.BLK_BLOCK
				|| blk.curBlock == StageData.BLK_STONE) {
			return false;
		} else if (blk.curBlock != StageData.BLK_STEP
				&& blk.curBlock != StageData.BLK_BAR) {
			// オリジナルでチェック
			BlockData blk2 = getBlock(data, bx, blockY + 1);
			if (blk2.orgBlock != StageData.BLK_BLOCK
					&& blk2.orgBlock != StageData.BLK_STONE
					&& blk2.orgBlock != StageData.BLK_STEP) {
				retFlag = false;
			}
		}
		// 縦視界をチェックする
		// 上視界
		if (blk.orgBlock == StageData.BLK_STEP) {
			// 上視界をチェックできる
			int by = blockY - 1;
			while (getBlock(data, bx, by).orgBlock == StageData.BLK_STEP) {
				if (getBlock(data, bx - 1, by).orgBlock == StageData.BLK_BAR) {
					pointList.add(new Point(bx, by));
				} else if (getBlock(data, bx + 1, by).orgBlock == StageData.BLK_BAR) {
					pointList.add(new Point(bx, by));
				} else {
					int blk1 = getBlock(data, bx - 1, by + 1).orgBlock;
					int blk2 = getBlock(data, bx + 1, by + 1).orgBlock;
					if (blk1 == StageData.BLK_BLOCK
							|| blk1 == StageData.BLK_STONE
							|| blk1 == StageData.BLK_STEP
							|| blk2 == StageData.BLK_BLOCK
							|| blk2 == StageData.BLK_STONE
							|| blk2 == StageData.BLK_STEP) {
						pointList.add(new Point(bx, by));
					}
				}
				by--;
			}
			// 一番上を設定する
			pointList.add(new Point(bx, by));
		}
		// 下視界
		int by = blockY + 1;
		blk = getBlock(data, bx, by);
		boolean okFlag = false;
		int ht = data.getHeight();
		while (by < ht && blk.orgBlock != StageData.BLK_BLOCK
				&& blk.orgBlock != StageData.BLK_STONE) {
			okFlag = false;
			if (by >= runner.y && blk.orgBlock != StageData.BLK_SPACE) {
				// いろいろチェック
				BlockData blkL = getBlock(data, bx - 1, by);
				BlockData blkR = getBlock(data, bx + 1, by);
				BlockData blkLD = getBlock(data, bx - 1, by + 1);
				BlockData blkRD = getBlock(data, bx + 1, by + 1);
				if (blkLD.orgBlock == StageData.BLK_BLOCK
						|| blkLD.orgBlock == StageData.BLK_STONE
						|| blkLD.orgBlock == StageData.BLK_STEP
						|| blkRD.orgBlock == StageData.BLK_BLOCK
						|| blkRD.curBlock == StageData.BLK_STONE
						|| blkRD.orgBlock == StageData.BLK_STEP
						|| blkL.orgBlock == StageData.BLK_BAR) {
					okFlag = true;
				} else if (bx > 0 && blkRD.orgBlock == StageData.BLK_BAR) {
					okFlag = true;
				} else if (bx == 0 && blkR.orgBlock == StageData.BLK_BAR) {
					okFlag = true;
				}
			}
			if (okFlag) {
				pointList.add(new Point(bx, by));
			}
			by++;
			blk = getBlock(data, bx, by);
		}
		if (!okFlag && by - 1 > blockY) {
			// 一番下を追加する
			pointList.add(new Point(bx, by - 1));
		}
		return retFlag;
	}

	private ArrayList<Point> checkHorizontalArea(StageData data, Point runner) {
		ArrayList<Point> retList = new ArrayList<Point>();
		checkVerticalMovePoint(data, blockX, retList, runner);
		for (int bx = blockX - 1; bx >= 0; bx--) {
			if (!checkVerticalMovePoint(data, bx, retList, runner)) {
				break;
			}
		}
		int wd = data.getWidth();
		for (int bx = blockX + 1; bx < wd; bx++) {
			if (!checkVerticalMovePoint(data, bx, retList, runner)) {
				break;
			}
		}
		return retList;
	}

	private Point getPriorityPoint(ArrayList<Point> pointList, Point runner) {
		Point ret = null;

		// ランナーの高さに行ける場合はそこにいく
		for (Point pt : pointList) {
			if (pt.y == runner.y) {
				// 近い方、さらに左優先
				if (ret == null) {
					ret = pt;
				} else {
					int diff = Math.abs(ret.x - blockX)
							- Math.abs(pt.x - blockX);
					if (diff > 0 || (diff == 0 && pt.x < ret.x)) {
						ret = pt;
					}
				}
			}
		}
		if (ret != null) {
			return ret;
		}
		// ランナーより上の段優先、同じ段なら左優先
		for (Point pt : pointList) {
			if (ret == null) {
				ret = pt;
			} else if (ret.y == pt.y) {
				// 真上、真下優先、左
				if (pt.x == blockX || (ret.x != blockX && pt.x < ret.x)) {
					ret = pt;
				}
			} else if (pt.y < runner.y) {
				if (ret.y > runner.y || pt.y > ret.y) {
					ret = pt;
				}
			} else if (ret.y > runner.y && pt.y < ret.y) {
				ret = pt;
			}
		}
		return ret;
	}

	/**
	 * ブロックが含まれるかをチェックする
	 * 
	 * @param blk
	 * @param check
	 * @return
	 */
	private static boolean isInBlock(int blk, int... check) {
		for (int chk : check) {
			if (blk == chk) {
				return true;
			}
		}
		return false;
	}

	private static boolean isNotBlock(int blk, int... check) {
		for (int chk : check) {
			if (blk == chk) {
				return false;
			}
		}
		return true;
	}

	private BlockData getBlock(StageData data, int bx, int by) {
		if (bx < 0 || bx >= data.getWidth() || by < 0) {
			return outsideBlock;
		}
		if (by >= data.getHeight()) {
			return underBlock;
		}
		return data.getBlock(bx, by);
	}
}
