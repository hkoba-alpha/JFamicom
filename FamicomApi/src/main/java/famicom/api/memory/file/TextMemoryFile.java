package famicom.api.memory.file;

import famicom.api.memory.AbstractMemoryFile;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * #開始はコメント
 * .addr $0000
 * .addr +$30
 * .chr { "00: 0123456" 10:A 11:B 13:C } 中かっこは、閉じかっこまでがデータとみなす
 * .pat " 123" クォートは、中は全て文字列として扱う
 * .section c0
 * .pattern 8
 * .pattern 16
 * .memory
 * 0102  普通の１６進数はデータ
 * " Test" クォートに囲まれたデータは chr との対応。日本語も１文字
 * ( 1 23   45  6)かっこに囲まれたデータは、patとの対応
 * <p>
 * Created by hkoba on 2017/01/08.
 */
public class TextMemoryFile extends AbstractMemoryFile<TextMemoryFile> {
    private static class TextData {
        private boolean charaFlag;
        private String textData;

        private TextData(String text, char endChar) {
            textData = text;
            charaFlag = ("\"\'".indexOf(endChar) >= 0);
        }

        @Override
        public String toString() {
            return textData;
        }
    }

    private class CommandAnalyzer {
        private byte[] memoryData = new byte[1024];
        private int maxSize;
        private int curAddr;
        private int patternSize;
        private List<String> patternCache = new ArrayList<>();
        private int sectionMinAddr;
        private int sectionMaxAddr;
        private String sectionName;
        private String patternText;
        private Map<Character, Byte> charaMap = new HashMap<>();

        private void analyze(List<TextData> textList) {
            TextData cmd = textList.get(0);
            if (cmd.charaFlag || cmd.textData.charAt(0) != '.') {
                // 普通のデータ
                if (patternSize > 0) {
                    entryPattern(textList);
                } else {
                    entryMemory(textList);
                }
                return;
            }
            flushPattern();
            if (".addr".equals(cmd.textData)) {
                if (textList.size() > 1) {
                    curAddr = parseDigit(textList.get(1).textData);
                }
            } else if (".memory".equals(cmd.textData)) {
                patternSize = 0;
            } else if (".pattern".equals(cmd.textData)) {
                patternSize = 8;
                for (TextData dt : textList.subList(1, textList.size())) {
                    if (dt.charaFlag) {
                        patternText = dt.textData;
                    } else {
                        patternSize = parseDigit(dt.textData);
                    }
                }
            } else if (".section".equals(cmd.textData)) {
                flushSection();
                if (textList.size() > 1) {
                    sectionName = textList.get(1).textData;
                }
                sectionMinAddr = Integer.MAX_VALUE;
                sectionMaxAddr = 0;
            } else if (".chr".equals(cmd.textData)) {
                for (TextData text : textList.subList(1, textList.size())) {
                    int ix = text.textData.indexOf(':');
                    if (ix < 0) {
                        // Error
                        continue;
                    }
                    int ch = parseDigit(text.textData.substring(0, ix));
                    for (int i = ix + 1; i < text.textData.length(); i++) {
                        charaMap.put(text.textData.charAt(i), (byte) ch);
                        ch++;
                    }
                }
            }
        }

        private int parseDigit(String text) {
            if (text.length() == 0) {
                return 0;
            }
            if (text.charAt(0) == '$') {
                return Integer.parseInt(text.substring(1), 16);
            }
            return Integer.parseInt(text);
        }

        private void entryByte(byte data) {
            if (curAddr < sectionMinAddr) {
                sectionMinAddr = curAddr;
            }
            while (curAddr >= memoryData.length) {
                byte[] newData = new byte[memoryData.length + 1024];
                System.arraycopy(memoryData, 0, newData, 0, memoryData.length);
                memoryData = newData;
            }
            memoryData[curAddr] = data;
            curAddr++;
            if (curAddr > maxSize) {
                maxSize = curAddr;
            }
            if (curAddr > sectionMaxAddr) {
                sectionMaxAddr = curAddr;
            }
        }

        private void entryPattern(List<TextData> textList) {
            StringBuilder str = new StringBuilder();
            for (TextData text : textList) {
                str.append(text.textData);
            }
            patternCache.add(str.toString());
            if (patternCache.size() == patternSize) {
                flushPattern();
            }
        }

        private void entryMemory(List<TextData> textList) {
            for (TextData text : textList) {
                if (text.charaFlag) {
                    for (int i = 0; i < text.textData.length(); i++) {
                        char ch = text.textData.charAt(i);
                        Byte data = charaMap.get(ch);
                        if (data == null) {
                            data = (byte) (ch & 255);
                        }
                        entryByte(data);
                    }
                } else {
                    for (int i = 0; i < text.textData.length() - 1; i += 2) {
                        entryByte((byte) Integer.parseInt(text.textData.substring(i, i + 2), 16));
                    }
                }
            }
        }

        private void flushPattern() {
            int maxsz = 0;
            for (String str : patternCache) {
                if (str.length() > maxsz) {
                    maxsz = str.length();
                }
            }
            for (int x = 0; x < maxsz; x += 8) {
                for (int y = 0; y < patternCache.size(); y += 8) {
                    for (int dy = 0; dy < 8; dy++) {
                        int ch1 = 0;
                        int ch2 = 0;
                        if (y + dy < patternCache.size()) {
                            String dt = patternCache.get(y + dy);
                            for (int dx = 0; dx < 8; dx++) {
                                ch1 <<= 1;
                                ch2 <<= 1;
                                if (x + dx < dt.length()) {
                                    int cx = patternText.indexOf(dt.charAt(x + dx));
                                    if (cx > 0) {
                                        ch1 |= (cx & 1);
                                        ch2 |= ((cx >> 1) & 1);
                                    }
                                }
                            }
                        }
                        // 書き込む
                        entryByte((byte) ch1);
                        curAddr += 7;
                        entryByte((byte) ch2);
                        curAddr -= 8;
                    }
                    curAddr += 8;
                }
            }
            patternCache.clear();
        }

        private void flushSection() {
            if (sectionName != null && sectionMinAddr < sectionMaxAddr) {
                entryName(sectionName, sectionMinAddr, sectionMaxAddr - sectionMinAddr);
            }
            sectionName = null;
        }
    }

    private byte[] memoryData;

    @Override
    protected void readFile(InputStream inputStream) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            CommandAnalyzer analyzer = new CommandAnalyzer();
            while (true) {
                List<TextData> textList = readLine(bufferedReader);
                if (textList.size() == 0) {
                    break;
                }
                analyzer.analyze(textList);
            }
            analyzer.flushPattern();
            analyzer.flushSection();
            memoryData = new byte[analyzer.maxSize];
            System.arraycopy(analyzer.memoryData, 0, memoryData, 0, analyzer.maxSize);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] getData() {
        return memoryData;
    }

    private List<TextData> readLine(BufferedReader bufferedReader) throws IOException {
        List<TextData> retList = new ArrayList<>();
        String lnstr;
        while ((lnstr = bufferedReader.readLine()) != null) {
            StringBuilder text = new StringBuilder(lnstr.trim());
            if (text.length() == 0 || text.charAt(0) == '#') {
                continue;
            }
            parseText(retList, text, bufferedReader, (char) 0);
            if (retList.size() > 0) {
                break;
            }
        }
        return retList;
    }

    private void parseText(List<TextData> textList, StringBuilder text, BufferedReader bufferedReader, char endChar) throws IOException {
        int ex = 0;
        while (ex <= text.length()) {
            if (ex == text.length()) {
                // 終わり
                if (endChar != 0) {
                    // 追加で読み込み
                    String lnstr = bufferedReader.readLine();
                    if (lnstr != null) {
                        text.append(lnstr);
                        continue;
                    }
                }
                // 終了
                if (ex > 0) {
                    textList.add(new TextData(text.substring(0, ex), endChar));
                }
                text.delete(0, ex);
                break;
            }
            char ch = text.charAt(ex);
            if (endChar != 0) {
                if (ch == endChar) {
                    // 終了
                    if (ex > 0) {
                        textList.add(new TextData(text.substring(0, ex), endChar));
                    }
                    text.delete(0, ex + 1);
                    break;
                }
                if (endChar != '}') {
                    ex++;
                    continue;
                }
            }
            if (ch == '\"' || ch == '\'' || ch == '{') {
                // quotの始まり
                if (ex > 0) {
                    textList.add(new TextData(text.substring(0, ex), endChar));
                }
                text.delete(0, ex + 1);
                ex = 0;
                parseText(textList, text, bufferedReader, ch == '{' ? '}' : ch);
                continue;
            }
            if (" \t,\r\n".indexOf(ch) >= 0) {
                // trim
                if (ex > 0) {
                    textList.add(new TextData(text.substring(0, ex), endChar));
                }
                text.delete(0, ex + 1);
                ex = 0;
                continue;
            }
            ex++;
        }
    }
}
