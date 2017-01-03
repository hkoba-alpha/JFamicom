package famicom.impl.pad;

import famicom.api.annotation.FamicomLibrary;
import famicom.api.annotation.Initialize;
import famicom.api.pad.IFamicomPad;
import famicom.api.pad.PadData;
import org.newdawn.slick.Input;
import org.newdawn.slick.command.Command;
import org.newdawn.slick.command.Control;
import org.newdawn.slick.command.InputProvider;
import org.newdawn.slick.command.InputProviderListener;

/**
 * Created by hkoba on 2017/01/03.
 */
@FamicomLibrary(priority = 0)
public class FamicomPadImpl implements IFamicomPad, InputProviderListener {
    private class PadDataImpl extends PadData {
        void set(ButtonType type) {
            super.buttonFlag |= type.getButtonFlag();
            super.readFlag |= type.getButtonFlag();
        }

        void clear(ButtonType type) {
            super.buttonFlag &= ~type.getButtonFlag();
        }

        @Override
        public boolean isDown(ButtonType type) {
            return super.isDown(type);
        }

        @Override
        public int getButtonFlag() {
            return super.getButtonFlag();
        }

        @Override
        public int getButton() {
            return super.getButton();
        }

        @Override
        public int getStick() {
            return super.getStick();
        }
    }

    private class PadCommand implements Command {
        private PadDataImpl padData;
        private PadData.ButtonType buttonType;

        private PadCommand(int ix, PadData.ButtonType type) {
            padData = (PadDataImpl) getPad(ix);
            buttonType = type;
        }

        private void press() {
            padData.set(buttonType);
        }

        private void release() {
            padData.clear(buttonType);
        }
    }

    private PadDataImpl[] padDatas = new PadDataImpl[2];

    @Initialize
    private void init() {
        for (int i = 0; i < padDatas.length; i++) {
            padDatas[i] = new PadDataImpl();
        }
    }

    public void initPad(Input input, Control[][] keyConfig) {
        input.clearControlPressedRecord();
        input.clearKeyPressedRecord();
        input.removeAllListeners();
        InputProvider provider = new InputProvider(input);
        provider.addListener(this);
        for (int index = 0; index < keyConfig.length; index++) {
            int pad = 0;
            for (PadData.ButtonType type: PadData.ButtonType.values()) {
                if (pad >= keyConfig[index].length) {
                    break;
                }
                if (keyConfig[index][pad] != null) {
                    provider.bindCommand(keyConfig[index][pad], new PadCommand(index, type));
                }
                pad++;
            }
        }
    }

    @Override
    public PadData getPad(int index) {
        return padDatas[index & 1];
    }

    @Override
    public void controlPressed(Command command) {
        ((PadCommand) command).press();
    }

    @Override
    public void controlReleased(Command command) {
        ((PadCommand) command).release();
    }
}
