package famicom.api.apu;

public class NoiseSound {

	static final int[] noizeTimerIndex = { 4, 8, 16, 32, 64, 96, 128, 160, 202,
			254, 380, 508, 762, 1016, 2034, 4068 };

	private class EnvelopeData {
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

	protected int lengthCounter;
	protected boolean loopFlag;
	protected int volumeValue;
	protected EnvelopeData envData;
	protected EnvelopeData nextEnv;
	protected int shiftRegister = 1;
	protected boolean shortFlag;
	protected int lastIndex;
	protected boolean enableFlag;
	protected int timerCount;
	protected boolean updateFlag;

	/**
	 * ボリュームを設定する. エンベロープは無効となる.
	 * 
	 * @param stopFlag
	 *            長さカウンタを止めるかどうかのフラグ.
	 * @param volume
	 *            ボリューム:[0-15]
	 * @return
	 */
	public NoiseSound setVolume(boolean stopFlag, int volume) {
		volumeValue = volume;
		loopFlag = stopFlag;
		envData = null;
		nextEnv = null;
		return this;
	}

	/**
	 * エンベロープを設定する. ボリュームは無効となる.
	 * 
	 * @param loopFlag
	 *            ループして続けるかのフラグ
	 * @param period
	 *            周期:[0-15]
	 * @return
	 */
	public NoiseSound setEnvelope(boolean loopFlag, int period) {
		this.loopFlag = loopFlag;
		nextEnv = new EnvelopeData(period + 1);
		volumeValue = 0;
		return this;
	}

	public NoiseSound setRandomMode(boolean shortFlag, int timerIndex) {
		if (!enableFlag) {
			return this;
		}
		this.shortFlag = shortFlag;
		setTimerCount(noizeTimerIndex[timerIndex]);
		lastIndex = -1;
		updateFlag = true;
		return this;
	}

	public NoiseSound setLength(int lengthIndex) {
		if (nextEnv != null) {
			envData = nextEnv;
			nextEnv = null;
		}
		if (envData != null) {
			envData.count = envData.period;
			volumeValue = 15;
		}
		lengthCounter = FamicomAPU.lengthIndexData[lengthIndex];
		shiftRegister = 0x4000;
		return this;
	}

	public NoiseSound setEnabled(boolean flag) {
		enableFlag = flag;
		if (!enableFlag) {
			lengthCounter = 0;
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

	protected NoiseSound setTimerCount(int count) {
		timerCount = count;
		return this;
	}

	protected int getTimerCount() {
		return timerCount;
	}

	void stepCounter() {
		if (!loopFlag && lengthCounter > 0) {
			lengthCounter--;
		}
	}
	void stepEnvelope() {
		if (envData != null) {
			envData.step();
		}
	}
	protected int getShiftBit() {
		int ret = (shiftRegister & 1) ^ 1;
		if (shortFlag) {
			shiftRegister = (shiftRegister << 1)
					| (((shiftRegister >> 14) & 1) ^ ((shiftRegister >> 8) & 1));
		} else {
			shiftRegister = (shiftRegister << 1)
					| (((shiftRegister >> 14) & 1) ^ ((shiftRegister >> 13) & 1));
		}
		return ret;
	}
	void stepOutput() {
		doOutput(lengthCounter > 0, timerCount, volumeValue, updateFlag);
		updateFlag = false;
	}


	/**
	 * 240Hzごとに呼び出される
	 * @param keyOnFlag
	 * @param timer
	 * @param volume
	 * @param updateFlag
	 */
	protected void doOutput(boolean keyOnFlag, int timer, int volume, boolean updateFlag) {
	}
}
