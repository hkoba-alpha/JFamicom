package lrunner.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class StageConfig {
	/**
	 * 敵が穴に落ちている時間
	 */
	public static final String KEY_HOLE_UP_COUNT = "enemy.holeUpCount";

	/**
	 * 敵の移動フラグ
	 */
	public static final String KEY_ENEMY_MOVE_FLAG = "enemy.moveFlag";

	/**
	 * 敵の金塊輸送最大カウント
	 */
	public static final String KEY_ENEMY_GOLD_COUNT = "enemy.goldCount";

	/**
	 * 穴が復活するまでのカウント
	 */
	public static final String KEY_HOLE_COUNT = "block.holeCount";

	/**
	 * 最大
	 */
	public static final String KEY_STAGE_MAX = "stage.max";

	/**
	 * 横幅
	 */
	public static final String KEY_STAGE_WIDTH = "stage.width";

	/**
	 * 高さ
	 */
	public static final String KEY_STAGE_HEIGHT = "stage.height";

	/**
	 * オフセットY
	 */
	public static final String KEY_STAGE_OFFSET = "stage.offset";

	private URL stageUrl;

	private int enemyHoleUpCount;

	private int enemyGoldCount;

	private int[] enemyMoveFlag;

	private int[] holeCount;

	private int stageMax;

	private int stageWidth;

	private int stageHeight;

	private int stageOffset;

	public StageConfig(URL url) {
		stageUrl = url;
		Properties prop = new Properties();
		try {
			InputStream is = new URL(url, "stage.properties").openStream();
			prop.load(is);
			is.close();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		enemyHoleUpCount = Integer.parseInt(prop.getProperty(KEY_HOLE_UP_COUNT,
				"38"));
		enemyGoldCount = Integer.parseInt(prop.getProperty(
				KEY_ENEMY_GOLD_COUNT, "128"));
		enemyMoveFlag = getIntArray(prop.getProperty(KEY_ENEMY_MOVE_FLAG,
				"000000,010100,010101,010301,030301,"
						+ "030303,070303,070703,070707,0f0707,"
						+ "0f0f07,0f0f0f,1f0f0f,1f1f0f,1f1f1f,"
						+ "3f1f1f,3f3f1f,3f3f3f,7f3f3f,7f7f3f,7f7f7f"));
		holeCount = getIntArray(prop.getProperty(KEY_HOLE_COUNT,
				"ff,14,0f,0a,05"));
		// ステージの最大
		stageMax = Integer.parseInt(prop.getProperty(KEY_STAGE_MAX, "50"));
		stageWidth = Integer.parseInt(prop.getProperty(KEY_STAGE_WIDTH, "14"));
		stageHeight = Integer.parseInt(prop.getProperty(KEY_STAGE_HEIGHT, "14"));
		stageOffset = Integer.parseInt(prop.getProperty(KEY_STAGE_OFFSET, "0"));
	}

	private int[] getIntArray(String val) {
		String[] dtlst = val.split(",");
		int[] ret = new int[dtlst.length];
		for (int i = 0; i < dtlst.length; i++) {
			ret[i] = Integer.parseInt(dtlst[i].trim(), 16);
		}
		return ret;
	}

	public int getEnemyHoleUpCount() {
		return enemyHoleUpCount;
	}

	public int getEnemyGoldCount() {
		return enemyGoldCount;
	}

	public int getEnemyMoveFlag(int num) {
		if (num >= enemyMoveFlag.length) {
			num = enemyMoveFlag.length - 1;
		}
		return enemyMoveFlag[num];
	}

	public int[] getHoleCount() {
		return holeCount;
	}

	public int getStageMax() {
		return stageMax;
	}

	public int getStageWidth() {
		return stageWidth;
	}

	public int getStageHeight() {
		return stageHeight;
	}

	public int getStageOffset() {
		return stageOffset;
	}

	public StageData loadStage(int num) {
		try {
			URL stage = new URL(stageUrl, num + ".txt");
			InputStream is = stage.openStream();
			if (is != null) {
				StageData ret = new StageData(is, StageData.BLK_STONE, this);
				ret.setStageNum(num);
				return ret;
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// ステージがなかった
		}
		return null;
	}
}
