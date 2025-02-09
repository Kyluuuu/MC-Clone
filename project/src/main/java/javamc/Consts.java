package javamc;

public class Consts {
    public static final int WORLDHEIGHT = 250;
    public static final int CHUNKSIZE = 32;
    public static final int BOTTOMOFWORLD = 0;
    public static final float TEXTUREATLASUNIT = 1 / 64f;
    public static final int TEXTUREPIXELSIZE = 16;
    public static final int TEXTUREATLASSIZE = 1024;
    public static final int TEXTUREATLASROW = TEXTUREATLASSIZE / TEXTUREPIXELSIZE;
    public static final int SPAWNPOINTXZ = Consts.CHUNKSIZE - (Consts.CHUNKSIZE / 2);
    public static final int SPAWNHEIGHTOFFSET = 10;

    private Consts() {}
}
