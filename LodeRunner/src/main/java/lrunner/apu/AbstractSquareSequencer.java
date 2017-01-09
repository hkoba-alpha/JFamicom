package lrunner.apu;

import famicom.api.apu.FamicomAPU;
import famicom.api.apu.SquareSound;

public abstract class AbstractSquareSequencer implements SoundSequencer {

	private ChannelType channelType;

	protected AbstractSquareSequencer(ChannelType channel) {
		channelType = channel;
	}

	@Override
	public ChannelType[] getChannels() {
		return new ChannelType[] { channelType };
	}

	@Override
	public boolean soundStep(FamicomAPU apu, ChannelType channel, int frame) {
		return soundStep(getSquare(apu, channel), frame);
	}

	@Override
	public void start(FamicomAPU apu, ChannelType channel, int frame) {
		start(getSquare(apu, channel), frame);
	}

	@Override
	public void pause(FamicomAPU apu, ChannelType channel, int frame) {
		pause(getSquare(apu, channel), frame);
	}

	protected SquareSound getSquare(FamicomAPU apu, ChannelType type) {
		return type == ChannelType.Square1 ? apu.getSquare(0): apu.getSquare(1);
	}

	protected void start(SquareSound sound, int frame) {
	}

	protected void pause(SquareSound sound, int frame) {
		sound.setEnabled(false);
	}

	abstract protected boolean soundStep(SquareSound sound, int frame);
}
