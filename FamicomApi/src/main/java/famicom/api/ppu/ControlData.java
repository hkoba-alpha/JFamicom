package famicom.api.ppu;

/**
 * Created by hkoba on 2017/01/01.
 */
public abstract class ControlData {
    protected int nameTableAddress;
    protected int spritePatternAddress;
    protected int screenPatternAddress;
    protected int spriteSize;
    protected boolean screenMask;
    protected boolean spriteMask;
    protected boolean screenEnabled;
    protected boolean spriteEnabled;
    protected int scrollX;
    protected int scrollY;
    protected boolean spriteHit;
    protected boolean vBlank;

    public ControlData setNameTableAddress(int nameTableAddress) {
        this.nameTableAddress = nameTableAddress & 3;
        return this;
    }

    public ControlData setSpritePatternAddress(int spritePatternAddress) {
        this.spritePatternAddress = spritePatternAddress & 1;
        return this;
    }

    public ControlData setScreenPatternAddress(int screenPatternAddress) {
        this.screenPatternAddress = screenPatternAddress & 1;
        return this;
    }

    public ControlData setSpriteSize(int spriteSize) {
        this.spriteSize = spriteSize & 1;
        return this;
    }

    public ControlData setScreenMask(boolean screenMask) {
        this.screenMask = screenMask;
        return this;
    }

    public ControlData setSpriteMask(boolean spriteMask) {
        this.spriteMask = spriteMask;
        return this;
    }

    public ControlData setScreenEnabled(boolean screenEnabled) {
        this.screenEnabled = screenEnabled;
        return this;
    }

    public ControlData setSpriteEnabled(boolean spriteEnabled) {
        this.spriteEnabled = spriteEnabled;
        return this;
    }

    public ControlData setScroll(int scrollX, int scrollY) {
        this.scrollX = scrollX & 255;
        if (scrollY >= 0 && scrollY < 240) {
            this.scrollY = scrollY & 255;
        }
        return this;
    }

    public int getNameTableAddress() {
        return nameTableAddress;
    }

    public int getSpritePatternAddress() {
        return spritePatternAddress;
    }

    public int getScreenPatternAddress() {
        return screenPatternAddress;
    }

    public int getSpriteSize() {
        return spriteSize;
    }

    public boolean isScreenMask() {
        return screenMask;
    }

    public boolean isSpriteMask() {
        return spriteMask;
    }

    public boolean isScreenEnabled() {
        return screenEnabled;
    }

    public boolean isSpriteEnabled() {
        return spriteEnabled;
    }

    public boolean isSpriteHit() {
        boolean ret = spriteHit;
        spriteHit = false;
        return ret;
    }

    public boolean isVBlank() {
        boolean ret = vBlank;
        vBlank = false;
        return ret;
    }
}
