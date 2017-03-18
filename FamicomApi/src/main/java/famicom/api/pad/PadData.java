package famicom.api.pad;

/**
 * Created by hkoba on 2017/01/03.
 */
public class PadData {
    public enum ButtonType {
        A(1),
        B(2),
        SELECT(4),
        START(8),
        UP(16),
        DOWN(32),
        LEFT(64),
        RIGHT(128);

        private int buttonFlag;

        ButtonType(int flag) {
            buttonFlag = flag;
        }

        public int getButtonFlag() {
            return buttonFlag;
        }
    }

    /**
     * 現在のボタンの状態
     */
    protected int buttonFlag;

    /**
     * 読み取る時の値
     * 読むまで押したフラグは保持しておく
     */
    protected int readFlag;

    /**
     * 押した状態をリセットする
     * @return
     */
    public PadData reset() {
        readFlag = buttonFlag;
        return this;
    }

    public boolean isDown(int padNum) {
        int flag = 1 << padNum;
        boolean ret = ((readFlag | buttonFlag) & flag) > 0;
        readFlag &= ~flag;
        return ret;
    }

    public boolean isDown(ButtonType type) {
        boolean ret = ((readFlag | buttonFlag) & type.buttonFlag) > 0;
        readFlag &= ~type.buttonFlag;
        return ret;
    }

    public int getButtonFlag() {
        int ret = readFlag;
        readFlag = buttonFlag;
        return ret;
    }

    public int getButton() {
        int ret = readFlag & 0xf;
        readFlag = (readFlag & 0xf0) | (buttonFlag & 0xf);
        return ret;
    }

    public int getStick() {
        int ret = readFlag & 0xf0;
        readFlag = (readFlag & 0xf) | (buttonFlag & 0xf0);
        return ret;
    }
}
