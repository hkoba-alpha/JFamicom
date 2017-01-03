package lrunner.play;

import famicom.api.pad.IFamicomPad;
import famicom.api.pad.PadData;
import famicom.api.ppu.IFamicomPPU;
import famicom.api.ppu.NameTable;
import lrunner.data.StageData;

import java.awt.*;
import java.util.Arrays;

public abstract class TitlePlayBase implements PlayBase {
	private boolean buttonFlag;
	private int commandSize;
	private int commandIndex;
	private Point cmdPoint;

	public TitlePlayBase(IFamicomPPU ppu) {
		ppu.getControlData().setScroll(0, 0);
		ppu.getControlData().setSpriteEnabled(false).setScreenEnabled(true).setSpriteMask(true).setScreenMask(true);
		//ppu.controlPPU2(0, false, true, true, true);
		byte[] blank = new byte[32 * 32];
		Arrays.fill(blank, 0, 32 * 30, (byte) 0x60);
		for (int i = 0; i < 64; i++) {
			ppu.getSpriteData(i).setY(240);
		}
		NameTable table = ppu.getNameTable(0);
		table.write(0, blank.length, blank);
		buttonFlag = true;
	}

	protected void drawTitle(IFamicomPPU ppu, int x, int y) {
		byte[] title = new byte[22 * 3];
		for (int i = 0; i < title.length; i++) {
			title[i] = (byte) ((i + 0xd0) & 255);
		}
		ppu.getNameTable(0).print(x, y, title, 22,
				3, 0, 22);
	}

	protected void drawMark(IFamicomPPU ppu, int x, int y) {
		byte[] mark = new byte[5 * 3];
		for (int i = 0; i < mark.length; i++) {
			mark[i] = (byte) ((i + 0x12) % 255);
		}
		ppu.getNameTable(0).print(x, y, mark, 5, 3,
				0, 5);
	}

	protected void drawCommand(IFamicomPPU ppu, int x, int y, String[] cmdlst) {
		NameTable table = ppu.getNameTable(0);
		for (int i = 0; i < cmdlst.length; i++) {
			StageData.drawString(ppu, x, y + i * 2, cmdlst[i]);
		}
		StageData.drawString(ppu, x - 1, y, "*");
		commandSize = cmdlst.length;
		commandIndex = 0;
		cmdPoint = new Point(x, y);
	}

	@Override
	public PlayBase stepFrame(IFamicomPad pad, IFamicomPPU ppu) {
		int button = pad.getPad(0).getButton();
		if (buttonFlag) {
			if (button == 0) {
				buttonFlag = false;
			}
			return this;
		}
		if ((button & PadData.ButtonType.START.getButtonFlag()) > 0) {
			return commandSelected(ppu, commandIndex);
		} else if ((button & PadData.ButtonType.SELECT.getButtonFlag()) > 0) {
			buttonFlag = true;
			if (commandSize > 1) {
				StageData.drawString(ppu, cmdPoint.x - 1, cmdPoint.y
						+ commandIndex * 2, " ");
				commandIndex = (commandIndex + 1) % commandSize;
				StageData.drawString(ppu, cmdPoint.x - 1, cmdPoint.y
						+ commandIndex * 2, "*");
			}
		}
		return this;
	}

	abstract protected PlayBase commandSelected(IFamicomPPU ppu, int sel);
}
