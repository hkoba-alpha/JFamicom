package famicom.api.state;

/**
 * Created by hkoba on 2017/02/04.
 */
public class PowerControl {
    protected boolean resetFlag;
    protected boolean offFlag;

    public void reset() {
        resetFlag = true;
    }

    public void powerOff() {
        offFlag = true;
    }
}
