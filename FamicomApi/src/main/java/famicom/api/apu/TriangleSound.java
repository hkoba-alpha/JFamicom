package famicom.api.apu;

public class TriangleSound {
	protected int lengthCounter;
	protected int lineCounter;
	protected int lineCounterData;
	protected boolean loopFlag;
	protected int timerCount;
	protected boolean enableFlag;
	protected boolean updateFlag;

	public TriangleSound setLinear(boolean loopFlag, int lineCount) {
		this.loopFlag = loopFlag;
		this.lineCounterData = lineCount;
		this.lineCounter = lineCount;
		return this;
	}

	public TriangleSound setTimer(int lengthIndex, int timerCount) {
		if (!enableFlag) {
			return this;
		}
		updateFlag = true;
		lineCounter = lineCounterData;
		lengthCounter = FamicomAPU.lengthIndexData[lengthIndex];
		setTimerCount(timerCount + 1);
		return this;
	}

	protected TriangleSound setTimerCount(int count) {
		timerCount = count;
		return this;
	}

	protected int getTimerCount() {
		return timerCount;
	}

	/**
	 * 長さカウンタがゼロではないかを返す
	 * 
	 * @return
	 */
	public boolean isPlaying() {
		return lengthCounter > 0 && lineCounter > 0;
	}

	public TriangleSound setEnabled(boolean flag) {
		enableFlag = flag;
		if (!enableFlag) {
			lengthCounter = 0;
		}
		return this;
	}


	void stepCounter() {
		if (!loopFlag && lengthCounter > 0) {
			lengthCounter--;
		}
	}

	void stepOutput() {
		doOutput(lengthCounter > 0 && lineCounter > 0, timerCount, updateFlag);
		updateFlag = false;
	}

	/**
	 * 240Hzごとに呼び出される
	 * @param keyOnFlag
	 * @param timer
	 * @param updateFlag
	 */
	protected void doOutput(boolean keyOnFlag, int timer, boolean updateFlag) {

	}
}
