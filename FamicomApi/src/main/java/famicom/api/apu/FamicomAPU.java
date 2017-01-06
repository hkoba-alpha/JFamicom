package famicom.api.apu;

import famicom.api.annotation.FamicomApi;
import famicom.api.annotation.HBlank;
import famicom.api.annotation.Initialize;
import famicom.api.annotation.PostReset;
import famicom.api.core.ExecuteManager;

/**
 * Created by hkoba on 2017/01/05.
 */
@FamicomApi
public class FamicomAPU {
    /**
     * 長さインデックス用
     */
    public static final int[] lengthIndexData = { 0x0a, 0xfe, 0x14, 0x02, 0x28,
            0x04, 0x50, 0x06, 0xa0, 0x08, 0x3c, 0x0a, 0x0e, 0x0c, 0x1a, 0x0e,
            0x0c, 0x10, 0x18, 0x12, 0x30, 0x14, 0x60, 0x16, 0xc0, 0x18, 0x48,
            0x1a, 0x10, 0x1c, 0x20, 0x1e };

    public enum StepMode {
        MODE_4STEP,
        MODE_5STEP
    }

    protected StepMode stepMode = StepMode.MODE_4STEP;

    protected int sequenceCounter;

    protected int frameCounter;

    protected SquareSound[] squareSound = new SquareSound[2];
    protected TriangleSound triangleSound;
    protected NoiseSound noiseSound;

    public FamicomAPU setStepMode(StepMode mode) {
        stepMode = mode;
        sequenceCounter = frameCounter = 0;
        return this;
    }

    /**
     *
     * @param channel 0 or 1
     * @return
     */
    public SquareSound getSquare(int channel) {
        return squareSound[channel & 1];
    }

    public TriangleSound getTriangle() {
        return triangleSound;
    }

    public NoiseSound getNoise() {
        return noiseSound;
    }

    @Initialize
    protected void init() {
        // 必要に応じてオーバーライド
        squareSound[0] = new SquareSound(false);
        squareSound[1] = new SquareSound(true);
        triangleSound = new TriangleSound();
        noiseSound = new NoiseSound();
    }

    @PostReset
    protected void reset() {
        frameCounter = 0;
        squareSound[0].setEnabled(false);
        squareSound[1].setEnabled(false);
        triangleSound.setEnabled(false);
        noiseSound.setEnabled(false);
    }

    @HBlank
    protected void hBlank() {
        boolean lFlag = false;
        boolean eFlag = false;
        boolean oFlag = false;
        switch (frameCounter) {
            case 0:
            case 66:
            case 131:
            case 197:
                oFlag = true;
                if (stepMode == StepMode.MODE_4STEP) {
                    lFlag = ((sequenceCounter & 1) == 1);
                    eFlag = true;
                } else {
                    lFlag = (((sequenceCounter % 5) & 1) == 0);
                    eFlag = ((sequenceCounter % 5) < 4);
                }
                sequenceCounter++;
                break;
        }
        if (lFlag) {
            getSquare(0).stepCounter();
            getSquare(1).stepCounter();
            getTriangle().stepCounter();
            getNoise().stepCounter();
        }
        if (eFlag) {
            getSquare(0).stepEnvelope();
            getSquare(1).stepEnvelope();
            getNoise().stepEnvelope();
        }
        if (oFlag) {
            getSquare(0).stepOutput();
            getSquare(1).stepOutput();
            getTriangle().stepOutput();
            getNoise().stepOutput();
            flushOutput();
        }
        frameCounter++;
        if (frameCounter == ExecuteManager.SCAN_LINE_SIZE) {
            frameCounter = 0;
        }
    }

    protected void flushOutput() {

    }
}
