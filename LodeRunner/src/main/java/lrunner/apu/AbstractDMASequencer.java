package lrunner.apu;

import famicom.api.apu.FamicomAPU;
import famicom.api.apu.DeltaSound;

public abstract class AbstractDMASequencer implements SoundSequencer {

	protected AbstractDMASequencer() {
	}

	@Override
	public ChannelType[] getChannels() {
		return new ChannelType[] { ChannelType.DMA };
	}

	@Override
	public boolean soundStep(FamicomAPU apu, ChannelType channel, int frame) {
		//return soundStep(apu.getDMA(), frame);
		return false;
	}

	@Override
	public void start(FamicomAPU apu, ChannelType channel, int frame) {
		//start(apu.getDMA(), frame);
	}

	@Override
	public void pause(FamicomAPU apu, ChannelType channel, int frame) {
		//pause(apu.getDMA(), frame);
	}

	protected void start(DeltaSound sound, int frame) {
	}

	protected void pause(DeltaSound sound, int frame) {
	}

	abstract protected boolean soundStep(DeltaSound sound, int frame);
}
