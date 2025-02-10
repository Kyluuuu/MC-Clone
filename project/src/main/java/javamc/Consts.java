package javamc;

public class Consts {
    public static final int WORLDHEIGHT = 250;
    public static final int CHUNKSIZE = 15; //32
    public static final int BOTTOMOFWORLD = 0;
    public static final float TEXTUREATLASUNIT = 1 / 64f;
    public static final int TEXTUREPIXELSIZE = 16;
    public static final int TEXTUREATLASSIZE = 1024;
    public static final int TEXTUREATLASROW = TEXTUREATLASSIZE / TEXTUREPIXELSIZE;
    public static final int SPAWNPOINTXZ = Consts.CHUNKSIZE - (Consts.CHUNKSIZE / 2);
    public static final int SPAWNHEIGHTOFFSET = 10;
    public static final int CHUNKMAXBLOCKS = CHUNKSIZE * CHUNKSIZE * WORLDHEIGHT;
    public static final int DIRTLAYER = 3;

    public enum BlockName {
        Air(0),
        Grass_Block(1),
        //Grass top
        //Grass bottom
        Glass_Block(4),
        Stone_Block(5),
        Dirt_Block(6);

        final int Value;

        BlockName(int Value) {
            this.Value = Value;
        }
    }

    private Consts() {}
}
