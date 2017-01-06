package famicom.impl.apu;

/**
 * Created by hkoba on 2017/01/06.
 */
public class SoundMaker {
    /**
     * 240Hzの1シーケンスでのクロック数
     */
    public static final int SEQUENCE_CLOCK = 7457;

    /**
     * 1シーケンスのサンプルレート
     */
    public static final int SAMPLE_RATE = 200;

    /**
     * サンプリングインデックスごとに進めるクロック数
     */
    public static byte[] addClock = new byte[SAMPLE_RATE];

    static {
        for (int i = 0; i < SAMPLE_RATE; i++) {
            addClock[i] = (byte) ((i + 1) * SEQUENCE_CLOCK / SAMPLE_RATE - i
                    * SEQUENCE_CLOCK / SAMPLE_RATE);
        }
    }

    private byte[] sampleData;
    private int curClock;

    /**
     * 現在の音でのサンプリングの位置
     */
    private int sampleIndex;

    /**
     * 処理中のクロック位置
     */
    private int clockIndex;

    private int lastTimerCount;

    public SoundMaker(byte[] data) {
        sampleData = data;
    }

    public int setFrameSample(int timer, byte[] data) {
        if (timer != lastTimerCount) {
            if (lastTimerCount > 0) {
                curClock = curClock * timer / lastTimerCount;
            }
            lastTimerCount = timer;
        }
        if (timer < 1) {
            // 無音
            for (int i = 0; i < data.length; i++) {
                data[i] = sampleData[sampleIndex];
            }
        } else {
            for (int i = 0; i < data.length; i++) {
                data[i] = sampleData[sampleIndex];
                curClock += addClock[clockIndex];
                sampleIndex = (sampleIndex + (curClock / timer)) % sampleData.length;
                curClock %= timer;
                clockIndex = (clockIndex + 1) % SAMPLE_RATE;
            }
        }
        return sampleIndex;
    }
}
