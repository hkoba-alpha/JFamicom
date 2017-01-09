package lrunner.apu;

import famicom.api.apu.FamicomAPU;
import famicom.api.core.ComponentManager;
import famicom.api.memory.FamicomMemory;

import java.io.*;
import java.util.HashMap;

public class PsgSoundData implements SoundSequencer {
	private static class LineData {
		int waitNum;
		int flag;
		byte[] data;
		int addrIndex;
		LineData nextData;

		private FamicomMemory famicomRAM;

		LineData(String[] dt, int ix, int wait) {
			data = new byte[4];
			waitNum = Integer.parseInt(dt[0]) + wait;
			addrIndex = ix;
			for (int i = 0; i < 4; i++) {
				if (!"--".equals(dt[ix + i + 1])) {
					data[i] = (byte) Integer.parseInt(dt[ix + i + 1], 16);
					flag |= (1 << i);
				}
			}
			famicomRAM = ComponentManager.getInstance().getObject(FamicomMemory.class);
		}

		void writeData(FamicomAPU sound) {
			for (int i = 0; i < 4; i++) {
				if ((flag & (1 << i)) > 0) {
					famicomRAM.write(0x4000 + addrIndex + i, data[i] & 255);
				}
			}
		}
	}

	private HashMap<ChannelType, LineData> soundMap;

	private HashMap<ChannelType, LineData> curMap;

	public PsgSoundData(String lnstr) {
		this(new ByteArrayInputStream(lnstr.getBytes()));
	}

	public PsgSoundData(InputStream is) {
		soundMap = new HashMap<ChannelType, LineData>();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		try {
			String lnstr;
			HashMap<ChannelType, LineData> map = new HashMap<ChannelType, LineData>();
			final ChannelType[] typeList = new ChannelType[] {
					ChannelType.Square1, ChannelType.Square2,
					ChannelType.Triangle, ChannelType.Noise, ChannelType.DMA };
			int[] waitList = new int[typeList.length];
			boolean addFlag = false;
			while ((lnstr = rd.readLine()) != null) {
				String[] dt = lnstr.trim().split("[ :|*]+");

				addFlag = false;
				for (int i = 0; i < dt.length - 3; i += 4) {
					int index = i / 4;
					if (index >= typeList.length) {
						break;
					}
					LineData ln = new LineData(dt, i, waitList[index]);
					waitList[index] = ln.waitNum;
					if (ln.flag != 0) {
						// 追加する
						LineData last = map.get(typeList[index]);
						if (last != null) {
							last.nextData = ln;
						} else {
							soundMap.put(typeList[index], ln);
						}
						map.put(typeList[index], ln);
						addFlag = true;
					}
				}
			}
			if (!addFlag && map.size() > 0) {
				for (int i = 0; i < waitList.length; i++) {
					LineData last = map.get(typeList[i]);
					if (last != null) {
						String[] strlst = new String[waitList.length + 1];
						strlst[0] = "0";
						for (int j = 1; j < strlst.length; j++) {
							strlst[j] = "--";
						}
						last.nextData = new LineData(strlst, i * 4, waitList[i]);
						break;
					}
				}
			}
			rd.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public ChannelType[] getChannels() {
		return soundMap.keySet().toArray(new ChannelType[0]);
	}

	@Override
	public boolean soundStep(FamicomAPU apu, ChannelType channel, int frame) {
		if (curMap == null) {
			curMap = new HashMap<ChannelType, LineData>(soundMap);
		}
		LineData dt = curMap.get(channel);
		if (dt != null) {
			if (dt.waitNum <= frame) {
				// play
				dt.writeData(apu);
				dt = dt.nextData;
				if (dt != null) {
					curMap.put(channel, dt);
				} else {
					curMap.remove(channel);
				}
				if (curMap.size() == 0) {
					// すべて終了
					curMap = null;
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public void start(FamicomAPU apu, ChannelType channel, int frame) {
		if (frame == 0) {
			curMap = new HashMap<ChannelType, LineData>(soundMap);
		}
	}

	@Override
	public void pause(FamicomAPU apu, ChannelType channel, int frame) {
		switch (channel) {
		case Square1:
			apu.getSquare(0).setEnabled(false);
			break;
		case Square2:
			apu.getSquare(1).setEnabled(false);
			break;
		case Triangle:
			apu.getTriangle().setEnabled(false);
			break;
		case Noise:
			apu.getNoise().setEnabled(false);
			break;
		case DMA:
			break;
		}
	}
}
