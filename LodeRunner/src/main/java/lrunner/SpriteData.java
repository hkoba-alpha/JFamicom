package lrunner;

import famicom.api.ppu.IFamicomPPU;
import lrunner.data.StageData;
import lrunner.play.RunnerDeadException;

import java.awt.*;
import java.util.HashMap;

public abstract class SpriteData {
	public enum MoveType {
		MoveLeft, MoveRight, MoveUp, MoveDown, MoveFall, MoveNone, BeamLeft, BeamRight
	}

	public enum IconType {
		Walk, Bar, Fall, UpDown, Beam, BornDead, None, Hide, Clear
	}

	public static class IconData {
		private IconType iconType;
		private boolean rightFlag;
		private int iconIndex;

		private IconData() {
			iconType = IconType.Walk;
		}

		public void setIcon(IconType icon) {
			iconType = icon;
			iconIndex = 0;
		}

		public void animate() {
			iconIndex++;
		}

		public IconType getIconType() {
			return iconType;
		}

		public void setRightFlag(boolean flag) {
			rightFlag = flag;
		}

		@Override
		public String toString() {
			return iconType.toString() + "[" + iconIndex + "]";
		}
	}

	protected int blockX;
	protected int blockY;
	protected int subX;
	protected int subY;

	protected int limitLeft = -6;

	/**
	 * ロボットは6,ランナーは8
	 */
	protected int limitRight = 8;

	protected int limitUp = -6;

	protected int limitDown = 6;

	protected IconData iconData;

	private MoveType lastMove;

	private HashMap<IconType, int[]> iconDataMap;

	/**
	 * スプライト番号
	 */
	private int spriteNumber;

	/**
	 * 同じ移動の種類がどれだけ続いたか
	 */
	private int moveIndex;

	/**
	 * キャラのブロック番号
	 */
	private byte spriteBlock;

	protected SpriteData(int bx, int by, byte blk) {
		blockX = bx;
		blockY = by;
		spriteBlock = blk;
		if (blk == StageData.BLK_ENEMY) {
			// 敵
			limitRight = 6;
		}
		iconDataMap = new HashMap<IconType, int[]>();
		iconData = new IconData();
	}

	protected void entryIcon(IconType icon, int[] data) {
		if (iconDataMap.size() == 0) {
			// 初めての登録
			spriteNumber = data[0];
		}
		iconDataMap.put(icon, data);
	}

	/**
	 * 対象の方向へ移動できるかをチェック
	 * 
	 * @param data
	 * @param mv
	 * @return
	 */
	protected boolean canMove(StageData data, MoveType mv) {
		switch (mv) {
		case MoveLeft:
			if (subX > 0) {
				return true;
			} else if (blockX > 0
					&& canMove(data.getBlock(blockX - 1, blockY), mv)) {
				// 移動できる
				return true;
			}
			break;
		case MoveRight:
			if (subX < 0) {
				return true;
			} else if (blockX < data.getWidth() - 1
					&& canMove(data.getBlock(blockX + 1, blockY), mv)) {
				// 移動できる
				return true;
			}
			break;
		case MoveUp:
			if (subY > 0) {
				return true;
			} else if (blockY > 0
					&& canMove(data.getBlock(blockX, blockY - 1), mv)) {
				// 移動できるかも
				StageData.BlockData blk = data.getBlock(blockX, blockY);
				if (blk.curBlock == StageData.BLK_ENEMY
						|| blk.orgBlock == StageData.BLK_STEP) {
					// 移動可能
					return true;
				}
			}
			break;
		case MoveDown:
		case MoveFall:
			if (subY < 0) {
				return true;
			} else if (blockY < data.getHeight() - 1
					&& canMove(data.getBlock(blockX, blockY + 1), mv)) {
				// 移動できる
				return true;
			}
			break;
		case BeamLeft:
			if (data.getBlock(blockX - 1, blockY + 1).curBlock == StageData.BLK_BLOCK
					&& data.getBlock(blockX - 1, blockY).curBlock == StageData.BLK_SPACE) {
				return true;
			}
			break;
		case BeamRight:
			if (data.getBlock(blockX + 1, blockY + 1).curBlock == StageData.BLK_BLOCK
					&& data.getBlock(blockX + 1, blockY).curBlock == StageData.BLK_SPACE) {
				return true;
			}
			break;
		default:
			break;
		}
		return false;
	}

	public void move(StageData data, int stick, int button) throws RunnerDeadException {
		MoveType mv = checkMove(data, blockX, blockY, stick, button);
		int ax = (int) (-Math.signum(subX) * 2);
		int ay = (int) (-Math.signum(subY) * 2);
		switch (mv) {
		case MoveLeft:
			if (subX > 0) {
				ax = -2;
			} else if (blockX > 0
					&& canMove(data.getBlock(blockX - 1, blockY), mv)) {
				// 移動できる
				ax = -2;
			} else {
				// 移動できない
				ax = ay = 0;
				mv = MoveType.MoveNone;
			}
			break;
		case MoveRight:
			if (subX < 0) {
				ax = 2;
			} else if (blockX < data.getWidth() - 1
					&& canMove(data.getBlock(blockX + 1, blockY), mv)) {
				// 移動できる
				ax = 2;
			} else {
				// 移動できない
				ax = ay = 0;
				mv = MoveType.MoveNone;
			}
			break;
		case MoveUp:
			if (subY > 0) {
				ay = -2;
			} else if (blockY > 0
					&& canMove(data.getBlock(blockX, blockY - 1), mv)) {
				// 移動できるかも
				StageData.BlockData blk = data.getBlock(blockX, blockY);
				if (blk.curBlock == StageData.BLK_ENEMY
						|| blk.orgBlock == StageData.BLK_STEP) {
					// 移動可能
					ay = -2;
				} else {
					// 移動できない
					ax = ay = 0;
					mv = MoveType.MoveNone;
				}
			} else {
				// 移動できない
				ax = ay = 0;
				mv = MoveType.MoveNone;
			}
			break;
		case MoveDown:
		case MoveFall:
			if (subY < 0) {
				ay = 2;
			} else if (blockY < data.getHeight() - 1
					&& canMove(data.getBlock(blockX, blockY + 1), mv)) {
				// 移動できる
				ay = 2;
			} else {
				// 移動できない
				ax = ay = 0;
				mv = MoveType.MoveNone;
			}
			break;
		case BeamLeft:
			break;
		case BeamRight:
			break;
		default:
			ax = ay = 0;
			break;
		}
		int bx = blockX;
		int by = blockY;
		if (ax != 0 || ay != 0) {
			subX += ax;
			subY += ay;
			if (subX < limitLeft) {
				blockX--;
				subX += 16;
				if (subX > 8) {
					subX = 8;
				}
			} else if (subX > limitRight) {
				blockX++;
				subX = -6;
			}
			if (subY < limitUp) {
				blockY--;
				subY += 16;
				if (subY > 8) {
					subY = 8;
				}
			} else if (subY > limitDown) {
				blockY++;
				subY = -6;
			}
		}

		/*
		 * if (mv == MoveType.MoveNone || mv == MoveType.BeamLeft || mv ==
		 * MoveType.BeamRight) { moveIndex++; }
		 */
		if (mv != lastMove) {
			moveIndex = 0;
			lastMove = mv;
		} else {
			moveIndex++;
		}
		setIcon(iconData, mv, moveIndex, data);

		if (blockX != bx || blockY != by) {
			StageData.BlockData blk = data.getBlock(bx, by);
			if (blk.orgBlock == StageData.BLK_BLOCK) {
				// 空白にする
				blk.curBlock = StageData.BLK_SPACE;
			} else {
				blk.curBlock = blk.orgBlock;
			}
			blockMoved(bx, by, blockX, blockY, data);
			data.getBlock(blockX, blockY).curBlock = spriteBlock;
		}
	}

	protected void setIcon(IconData icon, MoveType newMove, int index,
			StageData data) {
		IconType newIcon = icon.iconType;
		switch (newMove) {
		case MoveUp:
		case MoveDown:
			newIcon = IconType.UpDown;
			break;
		case MoveFall:
			newIcon = IconType.Fall;
			if (data.getBlock(blockX, blockY).orgBlock == StageData.BLK_BAR) {
				// バー
				if (subY == 0) {
					newIcon = IconType.Bar;
				}
			}
			break;
		case MoveLeft:
		case MoveRight:
			icon.rightFlag = (newMove == MoveType.MoveRight);
			newIcon = IconType.Walk;
			if (data.getBlock(blockX, blockY).orgBlock == StageData.BLK_BAR) {
				// バー
				if (subY == 0) {
					newIcon = IconType.Bar;
					break;
				}
			}
			break;
		case BeamLeft:
		case BeamRight:
			if (index == 0) {
				icon.rightFlag = (newMove == MoveType.BeamRight);
				if (icon.iconType == IconType.Bar
						|| icon.iconType == IconType.UpDown) {
					// そのまま
					icon.animate();
					return;
				}
				icon.setIcon(IconType.Beam);
			}
			return;
		case MoveNone:
			return;
		default:
			break;
		}
		if (icon.iconType != newIcon) {
			icon.setIcon(newIcon);
		} else {
			icon.animate();
		}
	}

	protected abstract MoveType checkMove(StageData data, int bx, int by,
			int stick, int button);

	protected abstract void blockMoved(int oldX, int oldY, int newX, int newY,
			StageData data) throws RunnerDeadException;

	/**
	 * 落ちるかどうかをチェックする
	 * 
	 * @param data
	 * @return
	 */
	protected boolean isFall(StageData data) {
		// 落ちるかどうかをチェックする
		// 今いるところがはしごなら落ちない
		if (data.getBlock(blockX, blockY).orgBlock == StageData.BLK_STEP) {
			// はしご
			return false;
		} else if (subY < 0) {
			return true;
		} else {
			if (subY == 0
					&& data.getBlock(blockX, blockY).orgBlock == StageData.BLK_BAR) {
				// バーなら落ちない
				return false;
			}
			// 下があるかチェックする
			switch (data.getBlock(blockX, blockY + 1).curBlock) {
			case StageData.BLK_BLOCK:
			case StageData.BLK_STONE:
			case StageData.BLK_STEP:
			case StageData.BLK_ENEMY:
				return false;
			}
		}
		// 落ちる
		return true;
	}

	/**
	 * 移動できるかをチェックする
	 * 
	 * @param blk
	 * @param mv
	 * @return
	 */
	protected boolean canMove(StageData.BlockData blk, MoveType mv) {
		if (blk.curBlock == StageData.BLK_ENEMY && spriteBlock == StageData.BLK_ENEMY) {
			return false;
		}
		switch (blk.curBlock) {
		case StageData.BLK_BLOCK:
		case StageData.BLK_STONE:
			// 移動できない
			return false;
		}
		switch (mv) {
		case MoveLeft:
		case MoveRight:
		case MoveUp:
			if (blk.orgBlock == StageData.BLK_TRAP) {
				return false;
			}
		default:
			break;
		}
		return true;
	}

	public Point getPoint() {
		return new Point(blockX * 16 + subX, blockY * 16 + subY);
	}

	/**
	 * 移動後のチェック
	 * @param data
	 */
	public abstract void postMove(StageData data) throws RunnerDeadException;

	public void setSprite(IFamicomPPU ppu, int spriteNum, int scrollX,
                          int scrollY) {
		int spX = blockX * 16 + subX - scrollX;
		int spY = blockY * 16 + subY - scrollY;
		int spix = spriteNum * 4;
		int dx = 0;
		int[] list = iconDataMap.get(iconData.iconType);
		if (list != null) {
			spriteNumber = list[iconData.iconIndex % list.length];
		}
		int chnum = spriteNumber * 4;
		if (iconData.rightFlag) {
			dx = 2;
		}
		if (iconData.iconType == IconType.Hide) {
			// 消す
			spY = -16;
		}
		int colorNum = (this.spriteBlock == StageData.BLK_PLAYER ? 0 : 1);
		setSprite(
				ppu.getSpriteData(spix)
						.setAttribute(false, iconData.rightFlag, false,
								colorNum).setPattern(chnum + dx), spX, spY);
		setSprite(
				ppu.getSpriteData(spix + 1)
						.setAttribute(false, iconData.rightFlag, false,
								colorNum).setPattern(chnum + dx + 1), spX,
				spY + 8);
		setSprite(
				ppu.getSpriteData(spix + 2)
						.setAttribute(false, iconData.rightFlag, false,
								colorNum).setPattern(chnum + (dx ^ 2)),
				spX + 8, spY);
		setSprite(
				ppu.getSpriteData(spix + 3)
						.setAttribute(false, iconData.rightFlag, false,
								colorNum).setPattern(chnum + (dx ^ 2) + 1),
				spX + 8, spY + 8);
	}

	public Point getBlockPoint() {
		return new Point(blockX, blockY);
	}

	private void setSprite(famicom.api.ppu.SpriteData sp, int sx, int sy) {
		if (sx < 0 || sx >= 256 || sy < -1 || sy >= 232) {
			sp.setX(0).setY(0);
		} else {
			sp.setX(sx).setY(sy);
		}
	}

	public void hideSprite() {
		iconData.setIcon(IconType.Hide);
	}
}
