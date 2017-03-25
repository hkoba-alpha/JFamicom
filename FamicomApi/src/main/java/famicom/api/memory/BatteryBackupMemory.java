package famicom.api.memory;

import famicom.api.annotation.FamicomLibrary;
import famicom.api.annotation.PostReset;
import famicom.api.annotation.PreReset;
import famicom.api.annotation.VBlank;
import famicom.api.core.ExecuteManager;
import famicom.api.state.ScanState;
import famicom.api.state.StateFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * バッテリーバックアップ
 * Created by hkoba on 2017/03/20.
 */
@FamicomLibrary(priority = 0)
public class BatteryBackupMemory extends MemoryAccessor<BatteryBackupMemory> {
    protected boolean enabled;
    protected boolean updateFlag;
    protected int updateCount;

    /**
     * 2の階乗で作成すること
     *
     */
    public BatteryBackupMemory() {
        super(0x2000);
    }

    @VBlank
    public void vBlank(ScanState state, StateFile stateFile) {
        if (updateFlag) {
            updateCount++;
            if (updateCount > 10) {
                preReset(stateFile);
            }
        }
    }

    @PreReset
    public void preReset(StateFile stateFile) {
        if (updateFlag) {
            try (ObjectOutputStream outputStream = stateFile.getOutput("Battery")) {
                outputStream.writeObject(super.memoryData);
                updateFlag = false;
                updateCount = 0;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @PostReset
    public void reset(StateFile stateFile) {
        enabled = ExecuteManager.getInstance().getFamicomRom().backup();
        updateFlag = false;
        ObjectInputStream inputStream = stateFile.getInput("Battery");
        if (inputStream != null) {
            try {
                super.memoryData = (byte[])inputStream.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public BatteryBackupMemory write(int startAddr, int endAddr, byte[] buf, int srcIndex) {
        if (enabled) {
            return super.write(startAddr, endAddr, buf, srcIndex);
        }
        return this;
    }

    @Override
    public BatteryBackupMemory write(int startAddr, int endAddr, byte[] buf) {
        if (enabled) {
            return super.write(startAddr, endAddr, buf);
        }
        return this;
    }

    @Override
    public BatteryBackupMemory write(int addr, int val) {
        if (enabled) {
            return super.write(addr, val);
        }
        return this;
    }

    @Override
    protected void updateMemory(int startAddr, int endAddr) {
        updateFlag = true;
        updateCount = 0;
    }

    public BatteryBackupMemory setEnabled(boolean flag) {
        enabled = flag;
        return this;
    }
}
