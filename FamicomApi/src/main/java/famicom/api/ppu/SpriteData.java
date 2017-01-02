package famicom.api.ppu;

/**
 * Created by hkoba on 2017/01/01.
 */
public class SpriteData {
    protected int y;
    protected int pattern;
    protected int color;
    protected boolean behindBg;
    protected boolean flipX;
    protected boolean flipY;
    protected int x;

    public int getY() {
        return y;
    }

    public SpriteData setY(int y) {
        this.y = y;
        return this;
    }

    public int getPattern() {
        return pattern;
    }

    public SpriteData setPattern(int pattern) {
        this.pattern = pattern;
        return this;
    }

    public int getColor() {
        return color;
    }

    public SpriteData setColor(int color) {
        this.color = color & 3;
        return this;
    }

    public boolean isBehindBg() {
        return behindBg;
    }

    public SpriteData setBehindBg(boolean behindBg) {
        this.behindBg = behindBg;
        return this;
    }

    public boolean isFlipX() {
        return flipX;
    }

    public SpriteData setFlipX(boolean flipX) {
        this.flipX = flipX;
        return this;
    }

    public boolean isFlipY() {
        return flipY;
    }

    public SpriteData setFlipY(boolean flipY) {
        this.flipY = flipY;
        return this;
    }

    public int getX() {
        return x;
    }

    public SpriteData setX(int x) {
        this.x = x;
        return this;
    }
}
