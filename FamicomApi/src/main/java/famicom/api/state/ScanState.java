package famicom.api.state;

/**
 * Created by hkoba on 2016/12/31.
 */
public class ScanState {
    protected long frameCount;
    protected int lineCount;

    public long getFrameCount() {
        return frameCount;
    }
    public int getLineCount() {
        return lineCount;
    }
}
