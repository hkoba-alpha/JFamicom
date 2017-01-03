package lrunner.play;

import famicom.api.pad.IFamicomPad;
import famicom.api.ppu.IFamicomPPU;

public interface PlayBase {
	PlayBase stepFrame(IFamicomPad pad, IFamicomPPU ppu);

}
