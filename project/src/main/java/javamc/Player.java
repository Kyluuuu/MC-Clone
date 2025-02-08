package javamc;

//player is two blocks tall

public class Player {
    private int x;
    private int z;
    private int y;
    private int[] inventory;

    public Player() {
        x = 0;
        y = 0;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public int getY() {
        return y;
    }
}
