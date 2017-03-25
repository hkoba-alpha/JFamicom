package famicom.api.memory.file;

import famicom.api.annotation.FamicomRom;
import famicom.api.memory.AbstractMemoryFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.Map;

/**
 * Created by hkoba on 2017/01/08.
 */
public class NesRomFile extends AbstractMemoryFile<NesRomFile> {
    protected byte[] memoryData;

    protected int mapperType;

    protected FamicomRom.MirrorMode mirrorMode;

    protected boolean backupEnabled;

    @Override
    protected void readFile(InputStream inputStream) {
        byte[] header = new byte[16];
        try {
            inputStream.read(header);
            if (header[0] != 'N' || header[1] != 'E' || header[2] != 'S' || header[3] != 0x1a) {
                // format error
                return;
            }
            int trainer = 0;
            int prgSize = header[4];
            int chrSize = header[5];
            int inst = 0;
            int prom = 0;
            int mirror = header[6] & 15;
            if ((mirror & 8) > 0) {
                mirrorMode = ((mirror & 1) > 0 ? FamicomRom.MirrorMode.FOUR_VERTICAL: FamicomRom.MirrorMode.FOUR_HORIZONTAL);
            } else if ((mirror & 1) > 0) {
                mirrorMode = FamicomRom.MirrorMode.VERTICAL;
            } else {
                mirrorMode = FamicomRom.MirrorMode.HORIZONTAL;
            }
            mapperType = ((header[6] >> 4) & 15) | (header[7] & 0xf0);
            if ((header[6] & 4) > 0) {
                trainer = 512;
            }
            memoryData = new byte[trainer + prgSize * 0x4000 + chrSize * 0x2000 + inst + prom];
            inputStream.read(memoryData);
            int addr = trainer;
            for (int i = 0; i < prgSize; i++) {
                entryName("PRG" + i, addr, 0x4000);
                addr += 0x4000;
            }
            for (int i = 0; i < chrSize; i++) {
                entryName("CHR" + i, addr, 0x2000);
                addr += 0x2000;
            }
            backupEnabled = (header[6] & 2) > 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] getData() {
        return memoryData;
    }

    public FamicomRom.MirrorMode getMirrorMode() {
        return mirrorMode;
    }

    public void printText(PrintStream stream, String patternText, int patternSize) {
        final int horSize = 4;
        super.nameMap.entrySet().stream().sorted(new Comparator<Map.Entry<String, int[]>>() {
            @Override
            public int compare(Map.Entry<String, int[]> o1, Map.Entry<String, int[]> o2) {
                if (o1.getValue()[0] < o2.getValue()[0]) {
                    return -1;
                } else if (o1.getValue()[0] > o2.getValue()[0]) {
                    return 1;
                }
                return 0;
            }
        }).forEach(v -> {
            int addr = v.getValue()[0];
            int size = v.getValue()[1];
            stream.println(".section " + v.getKey());
            stream.println(".addr $" + Integer.toString(addr, 16));
            if (v.getKey().startsWith("CHR")) {
                // キャラクタ
                stream.println(".pattern " + patternSize + ",\"" + patternText + "\"");
                int offset = 0;
                while (offset < size) {
                    for (int y = 0; y < patternSize; y++) {
                        StringBuilder str = new StringBuilder("\"");
                        for (int x = 0; x < horSize; x++) {
                            int ch1 = memoryData[addr + offset + x * patternSize * 2];
                            int ch2 = memoryData[addr + offset + x * patternSize * 2 + 8];
                            for (int b = 7; b >= 0; b--) {
                                int ix = ((ch1 >> b) & 1) | (((ch2 >> b) & 1) << 1);
                                str.append(patternText.charAt(ix));
                            }
                        }
                        str.append('\"');
                        stream.println(str);
                        offset++;
                        if ((y & 7) == 7) {
                            offset += 8;
                        }
                    }
                    offset += (horSize - 1) * patternSize * 2;
                }
            } else {
                stream.println(".memory");
            }
        });
    }

    public int getMapperType() {
        return mapperType;
    }

    public boolean isBackupEnabled() {
        return backupEnabled;
    }
}
