package lrunner.apu;

import famicom.api.annotation.Attach;
import famicom.api.annotation.FamicomApplication;
import famicom.api.apu.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

@FamicomApplication
public class SoundManager {
	@Attach
	private FamicomAPU famicomAPU;

	private class SequenceData {
		private int priority;
		private SoundSequencer sequencer;
		private int startFlag;
		private int pauseFlag;
		private int playingFlag;
		private int channelFlag;
		private int frameIndex;
		private boolean loopFlag;
		/**
		 * 音を停止するフラグ
		 */
		private boolean silentFlag;

		private SequenceData(int pri, SoundSequencer seq, boolean loop) {
			priority = pri;
			sequencer = seq;
			loopFlag = loop;
			setChannelFlag();
		}

		private void setChannelFlag() {
			SoundSequencer.ChannelType[] allList = SoundSequencer.ChannelType
					.values();
			SoundSequencer.ChannelType[] chlst = sequencer.getChannels();
			for (int i = 0; i < allList.length; i++) {
				boolean flg = false;
				for (SoundSequencer.ChannelType ch : chlst) {
					if (ch == allList[i]) {
						flg = true;
						break;
					}
				}
				if (flg) {
					channelFlag |= (1 << i);
				}
			}
		}

		private void pause(FamicomAPU apu) {
			SoundSequencer.ChannelType[] allList = SoundSequencer.ChannelType
					.values();
			for (int i = 0; i < allList.length; i++) {
				if ((pauseFlag & (1 << i)) > 0) {
					sequencer.pause(apu, allList[i], frameIndex);
					playingFlag &= ~(1 << i);
				}
			}
			pauseFlag = 0;
		}

		private boolean play(FamicomAPU apu) {
			SoundSequencer.ChannelType[] allList = SoundSequencer.ChannelType
					.values();
			for (int i = 0; i < allList.length; i++) {
				int chflg = 1 << i;
				if ((startFlag & chflg) > 0) {
					sequencer.start(apu, allList[i], frameIndex);
					if (!sequencer.soundStep(apu, allList[i], frameIndex)) {
						channelFlag &= ~chflg;
					}
					playingFlag |= chflg;
				} else if ((playingFlag & chflg) > 0) {
					if (!sequencer.soundStep(apu, allList[i], frameIndex)) {
						channelFlag &= ~chflg;
						playingFlag &= ~chflg;
					}
				} else if ((channelFlag & (1 << i)) > 0) {
					// スキップ
					if (!sequencer.soundStep(apu, allList[i], frameIndex)) {
						channelFlag &= ~chflg;
					}
				}
			}
			startFlag = 0;
			if (channelFlag == 0) {
				if (loopFlag) {
					setChannelFlag();
					frameIndex = 0;
					return true;
				} else {
					return false;
				}
			}
			frameIndex++;
			return true;
		}
	}

	private ArrayList<SequenceData> soundList;
	private ArrayList<SequenceData> removeList;

	public SoundManager() {
		soundList = new ArrayList<SequenceData>();
		removeList = new ArrayList<SequenceData>();
	}

	public void soundStep(FamicomAPU apu, boolean irqFlag) {
		for (SequenceData dt : removeList) {
			dt.pauseFlag = dt.playingFlag;
			dt.pause(apu);
		}
		removeList.clear();
		// フラグの設定
		SoundSequencer.ChannelType[] allList = SoundSequencer.ChannelType
				.values();
		for (int i = 0; i < allList.length; i++) {
			int flg = (1 << i);
			boolean play = false;
			for (SequenceData dt : soundList) {
				if ((dt.channelFlag & flg) > 0) {
					// 対象
					if (play || dt.silentFlag) {
						// すでに出力済み、あるいは休止中
						if ((dt.playingFlag & flg) > 0) {
							// 停止する
							dt.pauseFlag |= flg;
						}
					} else {
						// これから出力する
						if ((dt.playingFlag & flg) == 0) {
							// 再生開始
							dt.startFlag |= flg;
						}
						play = true;
					}
				}
			}
		}
		// play
		for (SequenceData dt : soundList) {
			dt.pause(apu);
		}
		Iterator<SequenceData> it = soundList.iterator();
		while (it.hasNext()) {
			SequenceData dt = it.next();
			if (!dt.play(apu)) {
				// 終了した
				it.remove();
				System.out.println("Remove play:" + soundList.size());
			}
		}
	}

	public SoundManager addSequencer(int priority, SoundSequencer seq, boolean loopFlag) {
		soundList.add(new SequenceData(priority, seq, loopFlag));
		sortSoundList();
		System.out.println("Add Play:" + soundList.size());
		return this;
	}
	public SoundManager setLoopFlag(SoundSequencer seq, boolean loopFlag) {
		for (SequenceData dt: soundList) {
			if (dt.sequencer == seq) {
				dt.loopFlag = loopFlag;
				break;
			}
		}
		return this;
	}
	public boolean isPlaying(SoundSequencer seq) {
		for (SequenceData dt: soundList) {
			if (dt.sequencer == seq) {
				return true;
			}
		}
		return false;
	}

	public SoundManager removeSequencer(int priority) {
		Iterator<SequenceData> it = soundList.iterator();
		while (it.hasNext()) {
			SequenceData dt = it.next();
			if (dt.priority == priority) {
				it.remove();
				removeList.add(dt);
			}
		}
		sortSoundList();
		return this;
	}

	public SoundManager removeSequencer(SoundSequencer seq) {
		Iterator<SequenceData> it = soundList.iterator();
		while (it.hasNext()) {
			SequenceData dt = it.next();
			if (dt.sequencer == seq) {
				it.remove();
				removeList.add(dt);
			}
		}
		sortSoundList();
		return this;
	}

	public SoundManager pauseSequencer(int priority) {
		for (SequenceData dt: soundList) {
			if (dt.priority == priority) {
				dt.silentFlag = true;
			}
		}
		return this;
	}
	public SoundManager pauseSequencer(SoundSequencer seq) {
		for (SequenceData dt: soundList) {
			if (dt.sequencer == seq) {
				dt.silentFlag = true;
			}
		}
		return this;
	}
	public SoundManager resumeSequencer(int priority) {
		for (SequenceData dt: soundList) {
			if (dt.priority == priority) {
				dt.silentFlag = false;
			}
		}
		return this;
	}
	public SoundManager resumeSequencer(SoundSequencer seq) {
		for (SequenceData dt: soundList) {
			if (dt.sequencer == seq) {
				dt.silentFlag = false;
			}
		}
		return this;
	}

	private void sortSoundList() {
		Collections.sort(soundList, new Comparator<SequenceData>() {
			@Override
			public int compare(SequenceData o1, SequenceData o2) {
				if (o1.priority > o2.priority) {
					return -1;
				} else if (o1.priority < o2.priority) {
					return 1;
				}
				return 0;
			}
		});
	}
}
