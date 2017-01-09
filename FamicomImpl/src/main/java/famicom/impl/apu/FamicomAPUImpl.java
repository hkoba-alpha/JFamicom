package famicom.impl.apu;

import famicom.api.annotation.FamicomLibrary;
import famicom.api.annotation.HBlank;
import famicom.api.annotation.VBlank;
import famicom.api.apu.FamicomAPU;
import famicom.api.apu.NoiseSound;
import famicom.api.apu.SquareSound;
import famicom.api.apu.TriangleSound;
import famicom.api.state.ScanState;
import org.lwjgl.Sys;

import javax.sound.sampled.*;
import java.util.Arrays;

/**
 * Created by hkoba on 2017/01/06.
 */
@FamicomLibrary(priority = 0)
public class FamicomAPUImpl extends FamicomAPU {
    private static final byte[][] squareSampleDataList = {{0, 1, 0, 0, 0, 0, 0, 0},
            {0, 1, 1, 0, 0, 0, 0, 0}, {0, 1, 1, 1, 1, 0, 0, 0},
            {1, 0, 0, 1, 1, 1, 1, 1}};
    private static final byte[] triangleSampleData = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
            14, 15, 15, 14, 13, 12, 11, 10, 9, 8, 7,
            6, 5, 4, 3, 2, 1, 0, 0 };

    private class SquareSoundImpl extends SquareSound {
        private SoundMaker soundMaker;
        private int lastDuty = -1;
        private int lastVolume = -1;
        private byte[] sampleData = new byte[8];

        private SquareSoundImpl(boolean second) {
            super(second);
            soundMaker = new SoundMaker(sampleData);
        }

        @Override
        protected void doOutput(boolean keyOnFlag, int duty, int timer, int volume, boolean updateFlag) {
            byte[] sample = sampleBuffer[super.secondFlag ? 1: 0];
            if (duty != lastDuty || volume != lastVolume) {
                if (volume == 0) {
                    Arrays.fill(sampleData, (byte) 0);
                } else {
                    System.arraycopy(squareSampleDataList[duty], 0, sampleData, 0, 8);
                    if (volume != 1) {
                        for (int i = 0; i < 8; i++) {
                            sampleData[i] *= volume;
                        }
                    }
                    lastDuty = duty;
                    lastVolume = volume;
                }
            }
            if (keyOnFlag) {
                soundMaker.setFrameSample(timer * 2, sample);
            } else {
                soundMaker.setFrameSample(0, sample);
            }
        }
    }
    private class TriangleSoundImpl extends TriangleSound {
        private SoundMaker soundMaker;

        private TriangleSoundImpl() {
            soundMaker = new SoundMaker(triangleSampleData);
        }
        @Override
        protected void doOutput(boolean keyOnFlag, int timer, boolean updateFlag) {
            if (keyOnFlag) {
                soundMaker.setFrameSample(timer, sampleBuffer[2]);
            } else {
                soundMaker.setFrameSample(0, sampleBuffer[2]);
            }
        }
    }
    private class NoiseSoundImpl extends NoiseSound {
        private int curClock;
        private int lastTimer;
        private byte lastLevel;
        private int clockIndex;

        @Override
        protected void doOutput(boolean keyOnFlag, int timer, int volume, boolean updateFlag) {
            if (lastTimer != timer) {
                if (lastTimer > 0) {
                    curClock = curClock * timer / lastTimer;
                }
                lastTimer = timer;
            }
            byte[] sample = sampleBuffer[4];
            if (keyOnFlag && timer > 0) {
                for (int i = 0; i < sample.length; i++) {
                    sample[i] = lastLevel;
                    curClock += SoundMaker.addClock[clockIndex];
                    while (curClock >= timer) {
                        lastLevel = (byte) (super.getShiftBit() * volume);
                        curClock -= timer;
                    }
                    clockIndex = (clockIndex + 1) % SoundMaker.SAMPLE_RATE;
                }
            } else {
                Arrays.fill(sample, lastLevel);
            }
        }
    }

    private byte[][] sampleBuffer = new byte[6][SoundMaker.SAMPLE_RATE];
    private SourceDataLine dataLine;
    private int maxAvailable;
    private int adjustCount;
    private boolean adjustFlag;
    private SoundMixer soundMixer = new SoundMixer();

    public FamicomAPUImpl() {
    }

    @Override
    protected void init() {
        super.init();
        squareSound[0] = new SquareSoundImpl(false);
        squareSound[1] = new SquareSoundImpl(true);
        triangleSound = new TriangleSoundImpl();
        noiseSound = new NoiseSoundImpl();
    }

    @Override
    protected void flushOutput() {
        if (dataLine == null) {
            AudioFormat fmt = new AudioFormat(SoundMaker.SAMPLE_RATE * 240, 8, 1, false, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
            try {
                dataLine = (SourceDataLine) AudioSystem.getLine(info);
                dataLine.open();
                dataLine.start();
                maxAvailable = dataLine.available();
                // 余裕を持たす
                dataLine.write(new byte[800], 0, 800);
            } catch (LineUnavailableException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        int rest = (maxAvailable - dataLine.available());
        soundMixer.mixData(sampleBuffer[0], sampleBuffer[1], sampleBuffer[2], sampleBuffer[3], sampleBuffer[4], sampleBuffer[5]);
        dataLine.write(sampleBuffer[5], 0, sampleBuffer[5].length);
        if (rest < SoundMaker.SAMPLE_RATE * 12 || !adjustFlag) {
            if (rest < SoundMaker.SAMPLE_RATE * 2 && adjustFlag) {
                // 少し増やす
                adjustCount--;
                if (adjustCount < -8) {
                    adjustCount = 0;
                    for (int i = 0; i < sampleBuffer.length; i++) {
                        sampleBuffer[i] = new byte[sampleBuffer[i].length + 1];
                    }
                    System.out.println("Too Low:" + rest + ", length=" + sampleBuffer[0].length);
                }
                System.out.println("Timing:" + rest);
                adjustFlag = false;
                flushOutput();
            }
        } else {
            // 大きくなりすぎた
            System.out.println("Timing:" + rest);
            adjustFlag = false;
            adjustCount++;
            if (adjustCount > 8) {
                adjustCount = 0;
                if (sampleBuffer[0].length > SoundMaker.SAMPLE_RATE) {
                    for (int i = 0; i < sampleBuffer.length; i++) {
                        sampleBuffer[i] = new byte[sampleBuffer[i].length - 1];
                    }
                }
                System.out.println("Too BIG:" + rest + ", length=" + sampleBuffer[0].length);
            }
        }
    }

    @VBlank
    private void timingCheck(ScanState scanState) {
        adjustFlag = true;
    }
}
