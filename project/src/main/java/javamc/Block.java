package javamc;

import java.util.HashMap;
import java.awt.Point;

public class Block {
    private static HashMap<Integer, String> blocks;

    private static int[] leftFaceVerts = {0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 0, 0};

    private static int[] rightFaceVerts = {1, 0, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1};

    private static int[] topFaceVerts = {0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0};

    private static int[] bottomFaceVerts = {0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1};

    private static int[] backFaceVerts = {0, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0};

    private static int[] frontFaceVerts = {1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 0, 1};

    static {
        blocks = new HashMap<>();

        blocks.put((int) Consts.BlockName.Air.Value, "Air");
        blocks.put((int) Consts.BlockName.Grass_Block.Value, "Grass_BlockSide");
        blocks.put(Consts.BlockName.Grass_Block.Value + 1, "Grass_BlockTop");
        blocks.put(Consts.BlockName.Grass_Block.Value + 2, "Grass_BlockBottom");
        blocks.put((int) Consts.BlockName.Glass_Block.Value, "Glass_Block");
        blocks.put((int) Consts.BlockName.Stone_Block.Value, "Stone_Block");
        blocks.put((int) Consts.BlockName.Dirt_Block.Value, "Dirt_Block");
        blocks.put((int) Consts.BlockName.Snow_Block.Value, "Snow_Block");
        blocks.put((int) Consts.BlockName.Log_Block.Value, "Log_Block");
        blocks.put(Consts.BlockName.Log_Block.Value + 1, "Log_BlockTop");
        blocks.put(Consts.BlockName.Log_Block.Value + 2, "Log_BlockBottom"); //???
        blocks.put((int) Consts.BlockName.Leaf_Block.Value, "Leaf_Block");
    }

    public static int getFaceNumber(int face) {
        if (face == 3) {
            return 2;
        } else if (face == 2) {
            return 1;
        }
        return 0;
    }

    // face == 2 is bottom face
    // face == 1 is top face
    public static float[] getTexturePos(int block, int face) {
        float[] result = new float[2];

        if (face != 0) {
            String temp = blocks.get(block).split("_")[0];

            if (!blocks.containsKey(block + face) || !blocks.get(block + face).contains(temp)) {
                face = 0;
            }
        }

        result[0] = ((block + face - 1) % Consts.TEXTUREATLASROW) * Consts.TEXTUREATLASUNIT;
        int ftemp = (block + face) / Consts.TEXTUREATLASROW;
        result[1] = 1f - ((ftemp + 1) * Consts.TEXTUREATLASUNIT);
        return result;
    }

    public static int[] getFaceVerts(int face) {
        switch (face) {
            case 0:
                return leftFaceVerts;
            case 1:
                return rightFaceVerts;
            case 2:
                return topFaceVerts;
            case 3:
                return bottomFaceVerts;
            case 4:
                return backFaceVerts;
            case 5:
                return frontFaceVerts;
            default:
                return leftFaceVerts;
        }
    }
}
