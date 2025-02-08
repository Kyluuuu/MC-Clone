package javamc;

import com.jme3.math.Vector3f;


public class Player {
    private int x;
    private int z;
    private int y;
    private int[] inventory;

    public Player() {
        x = 0;
        y = 0;
    }

    public void updatePlayerPosition(Vector3f pos) {
        x = (int) pos.getX();
        y = (int) pos.getY();
        z = (int) pos.getZ();
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
