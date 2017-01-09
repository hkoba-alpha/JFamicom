package famicom.api.apu;

public class DeltaSound {
	public static final int[] periodIndexData = {
		0x1ac, 0x17c, 0x154, 0x140, 0x11e, 0x0fe, 0x0e2, 0x0d6,
			0x0be, 0x0a0, 0x08e, 0x080, 0x06a, 0x054, 0x048, 0x036
	};

	protected int timerCount;

	protected byte[] sampleData;

	protected int sampleAddr;

	protected int restCount;

	protected int deltaValue;

	protected int shiftRegister;

	protected int shiftBit = 0;

	protected int lengthCounter;

	protected boolean enableFlag;

	protected boolean loopFlag;

	protected int periodIndex;

	public DeltaSound setPeriod(boolean loopFlag, int periodIndex) {
		lengthCounter = periodIndexData[periodIndex & 15];
		this.periodIndex = periodIndex;
		this.loopFlag = loopFlag;
		return this;
	}

	public DeltaSound setDelta(int delta) {
		deltaValue = delta;
		return this;
	}

	public DeltaSound setSample(byte[] data, int start, int length) {
		sampleData = data;
		sampleAddr = start;
		restCount = length;
		return this;
	}

	public DeltaSound setEnabled(boolean flag) {
		enableFlag = flag;
		if (!enableFlag) {
			lengthCounter = 0;
		}
		return this;
	}

	void stepCounter() {
		if (lengthCounter > 0) {
			lengthCounter--;
			if (loopFlag && lengthCounter == 0) {
				lengthCounter = periodIndexData[periodIndex & 15];
			}
		}
	}

	protected byte getDelta() {
		byte ret = (byte)deltaValue;
		if (enableFlag && lengthCounter > 0) {
			if (shiftBit == 0) {
				if (restCount > 0) {
					shiftRegister = sampleData[sampleAddr] & 255;
					shiftBit = 1;
					restCount--;
					sampleAddr = (sampleAddr + 1) % sampleData.length;
				}
				return ret;
			}
			if ((shiftRegister & shiftBit) > 0) {
				if (deltaValue < 126) {
					deltaValue += 2;
				}
			} else {
				if (deltaValue > 1) {
					deltaValue -= 2;
				}
			}
			shiftBit <<= 1;
			if (shiftBit > 128) {
				shiftBit = 0;
			}
		}
		return ret;
	}

	void stepOutput() {
		doOutput(lengthCounter > 0, timerCount);
	}

	protected void doOutput(boolean keyOnFlag, int timer) {

	}
}
