package lrunner.play;

import famicom.api.ppu.IFamicomPPU;
import famicom.api.ppu.NameTable;
import lrunner.data.StageConfig;
import lrunner.data.StageData;

public class ChampTitlePlay extends TitlePlayBase {

	public ChampTitlePlay(IFamicomPPU ppu) {
		super(ppu);
		drawTitle(ppu, 4, 12);
		drawMark(ppu, 13, 4);
		byte[] champ = new byte[15 * 2];
		for (int i = 0; i < champ.length; i++) {
			champ[i] = (byte) ((i < 15 ? i + 0xc0 : i - 15 + 0x21) & 255);
		}
		NameTable table = ppu.getNameTable(0);
		table.print(8, 8, champ, 15, 2, 0, 15);
		StageData.drawString(ppu, 2, 24, "COPYRIGHT 1984  DOUG SMITH");
		StageData.drawString(ppu, 3, 25, "PUBLISHED BY HUDSON SOFT");
		StageData.drawString(ppu, 6, 26, "UNDER LICENSE FROM");
		StageData.drawString(ppu, 3, 27, "BRODERBUND SOFTWARE INC");
		drawCommand(ppu, 7, 17, new String[] { "PRESS START BUTTON", "EDIT MODE",
				"LODE RUNNER" });
	}

	@Override
	protected PlayBase commandSelected(IFamicomPPU ppu, int sel) {
		if (sel == 0) {
			ppu.getControlData().setSpriteEnabled(true).setScreenEnabled(true).setSpriteMask(false).setScreenMask(false);
			//ppu.controlPPU2(0, true, true, false, false);
			StageConfig config = new StageConfig(
					ChampTitlePlay.class.getResource("/stage/champ/"));
			StageData stage = config.loadStage(1);
			return new StartPlay(stage);
		} else if (sel == 2) {
			return new NormalTitlePlay(ppu);
		}
		return this;
	}

}
