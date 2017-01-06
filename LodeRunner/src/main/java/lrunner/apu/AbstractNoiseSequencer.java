package lrunner.apu;

import famicom.api.apu.FamicomAPU;
import famicom.api.apu.NoiseSound;

public abstract class AbstractNoiseSequencer implements SoundSequencer {

	protected AbstractNoiseSequencer() {
	}

	@Override
	public ChannelType[] getChannels() {
		return new ChannelType[] { ChannelType.Noise };
	}

	@Override
	public boolean soundStep(FamicomAPU apu, ChannelType channel, int frame) {
		return soundStep(apu.getNoise(), frame);
	}

	@Override
	public void start(FamicomAPU apu, ChannelType channel, int frame) {
		start(apu.getNoise(), frame);
	}

	@Override
	public void pause(FamicomAPU apu, ChannelType channel, int frame) {
		pause(apu.getNoise(), frame);
	}

	protected void start(NoiseSound sound, int frame) {
	}

	protected void pause(NoiseSound sound, int frame) {
		sound.setEnabled(false);
	}

	abstract protected boolean soundStep(NoiseSound sound, int frame);
}
