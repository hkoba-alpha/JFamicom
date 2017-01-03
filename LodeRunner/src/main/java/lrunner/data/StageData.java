package lrunner.data;

import famicom.api.ppu.IFamicomPPU;
import lrunner.EnemyData;
import lrunner.RunnerData;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class StageData {
	/**
	 * 何もない
	 */
	public static final byte BLK_SPACE = 0;

	/**
	 * 掘れるレンガ
	 */
	public static final byte BLK_BLOCK = 1;

	/**
	 * 掘れないレンガ
	 */
	public static final byte BLK_STONE = 2;

	/**
	 * はしご
	 */
	public static final byte BLK_STEP = 3;

	/**
	 * バー
	 */
	public static final byte BLK_BAR = 4;

	/**
	 * 落とし穴
	 */
	public static final byte BLK_TRAP = 5;

	/**
	 * 金塊
	 */
	public static final byte BLK_GOLD = 7;

	/**
	 * 脱出はしご
	 */
	public static final byte BLK_ESCAPE = 6;

	/**
	 * 敵
	 */
	public static final byte BLK_ENEMY = 8;

	/**
	 * プレイヤー
	 */
	public static final byte BLK_PLAYER = 9;

	/**
	 * 穴を掘る
	 */
	public static final int CHARA_HOLE = 10;

	/**
	 * 穴が復活
	 */
	public static final int CHARA_FILL = 20;

	/**
	 * ブロックの色
	 */
	private static int[] blockColor = {
			// キャラ
			0, 1, 1, 2, 2, 1, 2, 3, 0, 0,
			// 掘る
			1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
			// 復活
			1, 1, 1, 1 };
	private static byte[] charaData;

	private int goldCount;

	static {
		charaData = new byte[blockColor.length * 4];
		for (int i = 0; i < charaData.length; i++) {
			int ix = ((i & 1) << 1) | ((i & 2) >> 1);
			charaData[i] = (byte) ((i & 0xfc) + ix + 0x60);
		}
	}

	public static class BlockData {
		public byte curBlock;
		public byte orgBlock;
		public byte stageBlock;
		private int charaIndex;

		public void setCharaIndex(int chara) {
			if (chara == charaIndex) {
				return;
			}
			charaIndex = 0x40 | chara;
		}

		public int getCharaIndex() {
			return charaIndex & 0x3f;
		}
	}

	private BlockData[][] blockData;

	private BlockData outsideBlock;

	private int offsetX = 1;
	private int offsetY = 1;

	private RunnerData runnerData;

	private ArrayList<EnemyData> enemyList;

	private Random randData;

	private int scrollX;
	private int scrollY;
	private int enemyDrawIndex;

	private StageConfig stageConfig;

	private int stageNum;

	public StageConfig getStageConfig() {
		return stageConfig;
	}

	public void setStageNum(int num) {
		stageNum = num;
	}

	public int getStageNum() {
		return stageNum;
	}

	public StageData(File file, StageConfig config) {
		stageConfig = config;
		outsideBlock = new BlockData();
		outsideBlock.curBlock = outsideBlock.orgBlock = BLK_STONE;
		try {
			loadStage(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public StageData(InputStream is, byte out, StageConfig config) {
		stageConfig = config;
		outsideBlock = new BlockData();
		outsideBlock.curBlock = outsideBlock.orgBlock = out;
		loadStage(is);
	}

	private void loadStage(InputStream is) {
		offsetY = 0;
		int wd = 14;
		int ht = 16;
		if (this.stageConfig != null) {
			wd = stageConfig.getStageWidth();
			ht = stageConfig.getStageHeight();
			offsetY = stageConfig.getStageOffset();
		}
		blockData = new BlockData[ht][wd];
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		for (int y = 0; y < blockData.length; y++) {
			String lnstr = null;
			try {
				lnstr = rd.readLine();
			} catch (IOException e) {
			}
			for (int x = 0; x < blockData[y].length; x++) {
				BlockData blk = new BlockData();
				if (lnstr != null && x < lnstr.length()) {
					blk.stageBlock = (byte) " OX#~V!$EP".indexOf(lnstr
							.charAt(x));
					if (blk.stageBlock < 0) {
						blk.stageBlock = 0;
					}
				}
				blockData[y][x] = blk;
			}
		}
	}

	public void initStage() {
		randData = new Random();
		enemyList = new ArrayList<EnemyData>();
		goldCount = 0;
		for (int y = blockData.length - 1; y >= 0; y--) {
			for (int x = blockData[y].length - 1; x >= 0; x--) {
				BlockData blk = blockData[y][x];
				blk.orgBlock = blk.stageBlock;
				blk.curBlock = blk.orgBlock;
				blk.charaIndex = blk.stageBlock;
				switch (blk.stageBlock) {
				case BLK_TRAP:
					blk.charaIndex = BLK_BLOCK;
					break;
				case BLK_ESCAPE:
					blk.orgBlock = blk.curBlock = BLK_SPACE;
					blk.charaIndex = BLK_SPACE;
					break;
				case BLK_ENEMY:
				case BLK_PLAYER:
					blk.orgBlock = BLK_SPACE;
					blk.charaIndex = BLK_SPACE;
					break;
				case BLK_GOLD:
					goldCount++;
					break;
				}
				if (blk.curBlock == StageData.BLK_PLAYER) {
					runnerData = new RunnerData(x, y);
				} else if (blk.curBlock == StageData.BLK_ENEMY) {
					enemyList.add(new EnemyData(x, y));
				}
			}
		}
	}

	public int nextRandom(int size) {
		return randData.nextInt(size);
	}

	public void decrementGold() {
		goldCount--;
	}

	public int getGoldCount() {
		return goldCount;
	}

	private static void setBlock(IFamicomPPU ppu, int lx, int ly, int blk) {
		ppu.getNameTable(lx < 32 ? 0 : 1)
				.print(lx & 0x1f, ly, charaData, 2, 2, blk * 4, 2)
				.setColor(lx & 0x1f, ly, blockColor[blk]);
	}

	private void drawStage(IFamicomPPU ppu, int ly, int y, boolean forceFlag) {
		if (y < 0) {
			// 外
			if (forceFlag) {
				for (int lx = 0; lx < 32; lx++) {
					setBlock(ppu, lx * 2, ly, BLK_SPACE);
				}
			}
			return;
		} else if (y >= blockData.length) {
			// 地下
			if (forceFlag) {
				for (int lx = 0; lx < 32; lx++) {
					setBlock(ppu, lx * 2, ly, outsideBlock.orgBlock);
				}
			}
			return;
		}
		if (forceFlag) {
			setBlock(ppu, 0, ly, outsideBlock.orgBlock);
			for (int lx = blockData[y].length + 1; lx < 32; lx++) {
				setBlock(ppu, lx * 2, ly, outsideBlock.orgBlock);
			}
		}
		for (int lx = 1; lx <= blockData[y].length; lx++) {
			BlockData blk = blockData[y][lx - 1];
			if (forceFlag || (blk.charaIndex & 0x40) == 0x40) {
				blk.charaIndex &= 0x3f;
				setBlock(ppu, lx * 2, ly, blk.charaIndex);
			}
		}
	}

	/**
	 * xxxxyyyyzzzz xxxx:オリジナル、yyyy:Spriteなし、zzzz:みため通り
	 * 
	 * @param bx
	 * @param by
	 * @return
	 */
	public BlockData getBlock(int bx, int by) {
		if (by < 0 || by >= blockData.length) {
			return outsideBlock;
		} else {
			if (bx < 0 || bx >= blockData[0].length) {
				return outsideBlock;
			}
		}
		return blockData[by][bx];
	}

	public int getWidth() {
		return blockData[0].length;
	}

	public int getHeight() {
		return blockData.length;
	}

	public Point getOffset() {
		return new Point(offsetX * 16, offsetY * 16);
	}

	public RunnerData getRunner() {
		return runnerData;
	}

	public ArrayList<EnemyData> getEnemyList() {
		return enemyList;
	}

	public Point getScrollPosition() {
		return new Point(scrollX, scrollY);
	}

	public Dimension getScrollSize() {
		return new Dimension((getWidth() + offsetX + 1) * 16 - 8, (getHeight()
				+ offsetY + 1) * 16);
	}

	public void setScrollPosition(IFamicomPPU ppu, int sx, int sy) {
		Point offset = getOffset();
		int wd = (getWidth() + 1) * 16 + offset.x;
		int ht = (getHeight() + 1) * 16 + offset.y;
		if (sx < 0) {
			sx = 0;
		} else if (sx > wd - 256) {
			sx = wd - 256;
		}
		if (sy < 0) {
			sy = 0;
		} else if (sy > ht - 240) {
			sy = ht - 240;
		}
		if (scrollX == sx && scrollY == sy) {
			return;
		}
		int y1 = (scrollY + 8) >> 4;
		int y2 = (sy + 8) >> 4;
		if (y1 < y2) {
			// 下にスクロール
			drawStage(ppu, ((y2 + 14) * 2) % 30, (y2 - offset.y / 16) + 14,
					true);
		} else if (y1 > y2) {
			// 上にスクロール
			drawStage(ppu, (y2 * 2) % 30, y2 - offset.y / 16, true);
		}
		scrollX = sx;
		scrollY = sy;
		ppu.getControlData().setScroll(scrollX, scrollY);
	}

	public void drawStage(IFamicomPPU ppu, boolean bgFlag) {
		Point offset = getOffset();
		drawSprite(ppu, offset);
		int dy2 = (scrollY + 8) / 16 - offset.y / 16;
		int dy1 = (scrollY + 8) / 16;
		for (int y = 0; y < 15; y++) {
			drawStage(ppu, ((y + dy1) * 2) % 30, y + dy2, bgFlag);
		}
	}

	private void drawSprite(IFamicomPPU ppu, Point offset) {
		runnerData.setSprite(ppu, 0, scrollX - offset.x, scrollY - offset.y);
		int ix = enemyDrawIndex;
		for (int i = 0; i < enemyList.size(); i++) {
			enemyList.get(i).setSprite(ppu, ix + 1, scrollX - offset.x,
					scrollY - offset.y);
			ix = (ix + 1) % enemyList.size();
		}
		enemyDrawIndex = (enemyDrawIndex + 3) % enemyList.size();
	}

	public Rectangle getPrefferedArea() {
		Point pt = runnerData.getPoint();
		Dimension sz = getScrollSize();
		int x2 = pt.x - 112;
		int y2 = pt.y - 96;
		int x1 = pt.x + 112 - 256;
		int y1 = pt.y + 96 - 240;
		if (x1 > sz.width - 256) {
			x1 = sz.width - 256;
		}
		if (x1 < 0) {
			x1 = 0;
		}
		if (x2 > sz.width - 256) {
			x2 = sz.width - 256;
		}
		if (x2 < 0) {
			x2 = 0;
		}
		if (y1 > sz.height - 240) {
			y1 = sz.height - 240;
		}
		if (y1 < 0) {
			y1 = 0;
		}
		if (y2 > sz.height - 240) {
			y2 = sz.height - 240;
		}
		if (y2 < 0) {
			y2 = 0;
		}
		return new Rectangle(x1, y1, x2 - x1, y2 - y1);
	}

	public static final void drawString(IFamicomPPU ppu, int lx, int ly,
                                        String str) {
		// 0x30からのオフセット
		final String textData = "0123456789*/****@ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		byte[] data = new byte[str.length()];
		for (int i = 0; i < data.length; i++) {
			int ix = textData.indexOf(str.charAt(i));
			if (ix < 0) {
				data[i] = 0x60;
			} else {
				data[i] = (byte) (ix + 0x30);
			}
			// 色
			ppu.getNameTable(0).setColor(lx + i, ly,
					0);
		}
		ppu.getNameTable(0).print(lx, ly, data,
				data.length, 1, 0, 32);
	}
}
