package lrunner.apu;

import famicom.api.apu.FamicomAPU;
import famicom.api.apu.TriangleSound;

public abstract class AbstractTriangleSequencer implements SoundSequencer {

	protected AbstractTriangleSequencer() {
	}

	@Override
	public ChannelType[] getChannels() {
		return new ChannelType[] { ChannelType.Triangle };
	}

	@Override
	public boolean soundStep(FamicomAPU apu, ChannelType channel, int frame) {
		return soundStep(apu.getTriangle(), frame);
	}

	@Override
	public void start(FamicomAPU apu, ChannelType channel, int frame) {
		start(apu.getTriangle(), frame);
	}

	@Override
	public void pause(FamicomAPU apu, ChannelType channel, int frame) {
		pause(apu.getTriangle(), frame);
	}

	protected void start(TriangleSound sound, int frame) {
	}

	protected void pause(TriangleSound sound, int frame) {
		sound.setEnabled(false);
	}

	abstract protected boolean soundStep(TriangleSound sound, int frame);
}
