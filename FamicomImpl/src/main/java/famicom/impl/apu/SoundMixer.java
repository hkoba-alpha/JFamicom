package famicom.impl.apu;

public class SoundMixer {
	private byte[] squareTable;

	private byte[] tndTable;

	private int volumeMax;

	public SoundMixer() {
		volumeMax = 64;
		squareTable = new byte[31];
		for (int i = 1; i < squareTable.length; i++) {
			squareTable[i] = (byte)(volumeMax * 95.88 / ((8128.0 / i) + 100));
		}
		tndTable = new byte[3 * 15 + 2 * 15 + 128];
		for (int i = 1; i < tndTable.length; i++) {
			tndTable[i] = (byte)(volumeMax * 163.67 / (24329.0 / i + 100));
		}
	}

	public void mixData(byte[] sq1, byte[] sq2, byte[] tri, byte[] noise, byte[] dmc, byte[] output) {
		for (int i = 0; i < sq1.length; i++) {
			output[i] = (byte)(squareTable[sq1[i] + sq2[i]] + tndTable[tri[i] * 3 + noise[i] * 2 + dmc[i]]);
		}
	}
}
