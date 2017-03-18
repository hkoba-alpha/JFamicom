package famicom.api.memory.file;

import famicom.api.memory.AbstractMemoryFile;
import famicom.api.pad.PadData;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hkoba on 2017/03/11.
 */
public class NesAsmFile extends NesRomFile {
    @Override
    protected void readFile(InputStream inputStream) {
        BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
        iNesPrg = 1;
        iNesChr = 1;
        iNesMir = 0;
        iNesMap = 0;
    }

    protected int iNesPrg;
    protected int iNesChr;
    protected int iNesMir;
    protected int iNesMap;

    protected void readAsm(BufferedReader rd) {

    }

    protected void readBank(BufferedReader rd, int bank) {

    }

    private static class ParseData {
        /**
         * 0: opなし
         * 1: 実際の値
         * 2: メモリ
         * 3: ($44,X)特殊
         * 4: X修飾
         * 8: Y修飾
         * -1:コメント
         */
        private int type;

        private ParseData(String lnstr) {
            lnstr = lnstr.trim();
            int sx = -1;
            int ix = 0;
            char quot = 0;
            List<String> cmdList = new ArrayList<>();
            while (ix < lnstr.length()) {
                char ch = lnstr.charAt(ix);
                if (quot != 0) {
                    if (quot == '(') {
                        if (ch == ')') {
                            cmdList.add(lnstr.substring(sx, ix + 1));
                            sx = -1;
                            quot = 0;
                        }
                    } else if (quot == ch) {
                        cmdList.add(lnstr.substring(sx, ix + 1));
                        sx = -1;
                        quot = 0;
                    }
                    ix++;
                    continue;
                } else if (ch == ' ' || ch == ',') {
                    if (sx >= 0) {
                        cmdList.add(lnstr.substring(sx, ix));
                        sx = -1;
                    }
                    ix++;
                    continue;
                } else if ("\"\'(".indexOf(ch) >= 0) {
                    quot = ch;
                }
                if (sx < 0) {
                    sx = ix;
                }
                ix++;
            }
            if (sx >= 0) {
                cmdList.add(lnstr.substring(sx));
            }
        }
    }
}
