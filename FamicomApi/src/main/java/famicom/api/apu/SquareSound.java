package famicom.api.apu;

/**
 * 矩形波インタフェース
 *
 * @author hkoba
 */
public class SquareSound {
    /**
     * duty値
     */
    protected int dutyValue;


    /**
     * ボリューム
     */
    protected int volumeValue;

    /**
     * カウンタ
     */
    protected int lengthCounter;

    /**
     * ２番目のチャネル
     */
    protected boolean secondFlag;
    protected boolean loopFlag;
    protected boolean updateFlag;

    protected class EnvelopeData {
        int period;
        int count;

        EnvelopeData(int period) {
            this.period = period;
            count = period;
        }

        boolean step() {
            count--;
            if (count == 0) {
                if (volumeValue > 0) {
                    volumeValue--;
                } else if (loopFlag) {
                    volumeValue = 15;
                }
                count = period;
            }
            return false;
        }
    }

    private class SweepData {
        int period;
        boolean upFlag;
        int count;
        int value;

        SweepData(boolean upFlag, int period, int val) {
            this.upFlag = upFlag;
            this.period = period;
            this.value = val;
            count = period;
        }

        void step() {
            if (lengthCounter == 0) {
                // 処理不要
                return;
            }
            count--;
            if (count == 0) {
                int tm = getTimerCount();
                if (upFlag) {
                    tm -= (tm >> value);
                    if (secondFlag) {
                        tm--;
                    }
                } else {
                    tm += (tm >> value);
                }
                if (tm < 8 || tm > 0x7ff) {
                    // 無効化する
                    lengthCounter = 0;
                } else {
                    setTimerCount(tm);
                }
                count = period;
            }
        }
    }

    protected EnvelopeData envData;
    protected EnvelopeData nextEnv;
    protected SweepData sweepData;
    protected SweepData nextSweep;

    protected int timerCount;

    /**
     * 有効・無効
     */
    protected boolean enableFlag;

    public SquareSound(boolean second) {
        secondFlag = second;
    }

    /**
     * ボリュームを設定する. エンベロープは無効となる.
     *
     * @param duty     デューティー比:[0-3]
     * @param stopFlag 長さカウンタを止めるかどうかのフラグ.
     * @param volume   ボリューム:[0-15]
     * @return
     */
    public SquareSound setVolume(int duty, boolean stopFlag, int volume) {
        volumeValue = volume;
        loopFlag = stopFlag;
        dutyValue = duty;
        envData = null;
        nextEnv = null;
        return this;
    }

    /**
     * エンベロープを設定する. ボリュームは無効となる.
     *
     * @param duty     デューティー比:[0-3]
     * @param loopFlag ループして続けるかのフラグ
     * @param period   周期:[0-15]
     * @return
     */
    public SquareSound setEnvelope(int duty, boolean loopFlag, int period) {
        this.loopFlag = loopFlag;
        // timerを変更しなくても変わるらしい
        //nextEnv = new EnvelopeData(period + 1);
        envData = new EnvelopeData(period + 1);
        //volumeValue = 15;
        dutyValue = duty;
        return this;
    }

    /**
     * 周波数と長さを設定する.
     * <table>
     * <tr>
     * <tr>
     * <th></th>
     * <th>0</th>
     * <th>1</th>
     * <th>2</th>
     * </tr>
     * </tr>
     * </table>
     *
     * @param lengthIndex 長さカウンタへのインデックス
     * @param timerCount  周波数の周期カウンタ
     * @return
     */
    public SquareSound setTimer(int lengthIndex, int timerCount) {
        if (!enableFlag) {
            return this;
        }
        updateFlag = true;
        if (nextEnv != null) {
            envData = nextEnv;
            nextEnv = null;
        }
        if (nextSweep != null) {
            sweepData = nextSweep;
            nextSweep = null;
        }
        if (envData != null) {
            envData.count = envData.period;
            volumeValue = 15;
        }
        if (sweepData != null) {
            sweepData.count = sweepData.period;
        }
        if (timerCount < 7 || timerCount > 0x7fe) {
            // 無効
            lengthCounter = 0;
        } else {
            lengthCounter = FamicomAPU.lengthIndexData[lengthIndex];
            setTimerCount(timerCount + 1);
        }
        return this;
    }

    /**
     * スイープを設定する
     *
     * @param enableFlag 有効フラグ
     * @param period     周期:[0-7]
     * @param upMode     方向: false=低くなっていく,true=高くなっていく
     * @param value      スィープ量:[0-7]
     * @return
     */
    public SquareSound setSweep(boolean enableFlag, int period, boolean upMode,
                                int value) {
        if (enableFlag && value > 0) {
            nextSweep = new SweepData(upMode, period + 1, value);
        } else {
            nextSweep = null;
            sweepData = null;
        }
        return this;
    }

    /**
     * 長さカウンタがゼロではないかを返す
     *
     * @return
     */
    public boolean isPlaying() {
        return lengthCounter > 0;
    }

    public SquareSound setEnabled(boolean flag) {
        enableFlag = flag;
        if (!enableFlag) {
            lengthCounter = 0;
        }
        return this;
    }

    protected SquareSound setTimerCount(int count) {
        timerCount = count;
        return this;
    }

    protected int getTimerCount() {
        return timerCount;
    }

    void stepCounter() {
        if (sweepData != null) {
            sweepData.step();
        }
        if (!loopFlag && lengthCounter > 0) {
            lengthCounter--;
        }
    }

    void stepEnvelope() {
        if (envData != null) {
            envData.step();
        }
    }

    void stepOutput() {
        doOutput(lengthCounter > 0, dutyValue, timerCount, volumeValue, updateFlag);
    }

    /**
     * 240Hzごとに呼び出される
     * @param keyOnFlag
     * @param duty
     * @param timer
     * @param volume
     * @param updateFlag
     */
    protected void doOutput(boolean keyOnFlag, int duty, int timer, int volume, boolean updateFlag) {

    }
}
