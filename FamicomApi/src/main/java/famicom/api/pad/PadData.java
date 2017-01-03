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

    public boolean isDown(ButtonType type) {
        boolean ret = (readFlag & type.buttonFlag) > 0;
        readFlag = (readFlag & ~type.buttonFlag) | (buttonFlag & type.buttonFlag);
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
