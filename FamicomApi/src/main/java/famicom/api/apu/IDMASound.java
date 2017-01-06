package famicom.api.apu;

public interface IDMASound {
	IDMASound setPeriod(boolean loopFlag, int periodIndex);

	IDMASound setDelta(int delta);

	IDMASound setSample(byte[] data, int length);
}
