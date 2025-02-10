package javamc;

import com.jme3.math.Vector3f;


public class Player {
    private Vector3f pos; 
    private int[] inventory;

    public Player() {
        pos = new Vector3f(0, 0, 0);
    }

    public void updatePlayerPosition(Vector3f pos) {
        this.pos = pos;
    }

    public Vector3f getPos() {
        return this.pos;
    }

    public int getX() {
        return (int) pos.getX();
    }

    public int getZ() {
        return (int) pos.getZ();
    }

    public int getY() {
        return (int) pos.getY();
    }
}
