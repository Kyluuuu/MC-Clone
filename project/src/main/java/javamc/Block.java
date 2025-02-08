package javamc;

import java.util.HashMap;
import java.awt.Point;

public class Block {
    private static HashMap<Integer, String> blocks;

    private static int[] leftFaceVerts = {
        0, 0, 1,
        0, 1, 1,
        0, 1, 0,
        0, 0, 0
    };

    private static int[] rightFaceVerts = {
        1, 0, 0,
        1, 1, 0,
        1, 1, 1,
        1, 0, 1
    };

    private static int[] topFaceVerts = {
        0, 1, 0,
        0, 1, 1,
        1, 1, 1,
        1, 1, 0
    };

    private static int[] bottomFaceVerts = {
        0, 0, 0,
        1, 0, 0,
        1, 0, 1,
        0, 0, 1
    };

    private static int[] backFaceVerts = {
        0, 0, 0,
        0, 1, 0,
        1, 1, 0,
        1, 0, 0
    };

    private static int[] frontFaceVerts = {
        1, 0, 1,
        1, 1, 1,
        0, 1, 1,
        0, 0, 1
    };

    static {
       blocks = new HashMap<>();

       blocks.put(0, "Air");
       blocks.put(1, "Grass BlockSide");
       blocks.put(2, "Grass BlockTop");
       blocks.put(3, "Grass BlockBottom");
       blocks.put(4, "Glass Block");
       blocks.put(5, "Stone Block");
    }

    public static float[] getTexturePos(int block, int face) {
        float[] result = new float[2];

        if (face != 0) {
            String temp = blocks.get(block).split(" ")[0];

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
