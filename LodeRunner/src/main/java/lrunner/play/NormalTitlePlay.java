package lrunner.play;

import famicom.api.ppu.IFamicomPPU;
import famicom.api.ppu.NameTable;
import lrunner.data.StageConfig;
import lrunner.data.StageData;

import java.io.InputStream;

public class NormalTitlePlay extends TitlePlayBase {

	public NormalTitlePlay(IFamicomPPU ppu) {
		super(ppu);
		drawTitle(ppu, 8, 5);
		drawMark(ppu, 2, 5);
		NameTable table = ppu.getNameTable(0);
		for (int y = 11; y < 22; y++) {
			for (int x = 8; x < 24; x++) {
				if (y < 13 || y >= 20 || x < 10 || x >= 22) {
					table.putChar(x, y, 0x64 + ((x + y) & 1));
					table.setColor(x, y, 1);
				}
			}
		}
		StageData.drawString(ppu, 2, 24, "COPYRIGHT @ 1984 HUDSON SOFT");
		StageData.drawString(ppu, 7, 25, "WITH PERMISSION OF");
		StageData.drawString(ppu, 4, 26, "BRODERBUND SOFTWARE INC");
		StageData.drawString(ppu, 7, 27, "ALL RIGHTS RESERVED");
		drawCommand(ppu, 12, 14, new String[] { "1 PLAYER", "EDIT MODE",
				"CHAMPION" });

		/*
		InputStream is = NormalTitlePlay.class.getResourceAsStream("/sound2.txt");
		SampleROM.soundManager.removeSequencer(0);
		SampleROM.soundManager.addSequencer(0, new PsgSoundData(is), true);
		*/
	}

	@Override
	protected PlayBase commandSelected(IFamicomPPU ppu, int sel) {
		if (sel == 0) {
			ppu.getControlData().setSpriteEnabled(true).setScreenEnabled(true).setSpriteMask(false).setScreenMask(false);
			//ppu.controlPPU2(0, true, true, false, false);
			StageConfig config = new StageConfig(
					NormalTitlePlay.class.getResource("/stage/loderun/"));
			StageData stage = config.loadStage(1);
			return new StartPlay(stage);
		} else if (sel == 2) {
			return new ChampTitlePlay(ppu);
		}
		return this;
	}

}
