package javamc;

import java.util.Random;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import java.util.HashMap;
import java.util.List;


public class World {
    private int renderDistance = 3;
    private Random rand = new Random();
    private long seed;
    private HashMap<String, Chunk> chunks = new HashMap<>();
    private static final double SCALE = 0.009;
    private Player player;
    private Chunk currentChunk;
    private int[] currentChunkPos;
    private Main main;

    public World() {
        currentChunkPos = new int[2];
        seed = rand.nextInt(10000);
        initWorld();
        player = new Player();
        main = new Main();
    }

    private void generateChunk(int x, int z) {
        int[][] heightMap = new int[Consts.CHUNKSIZE][Consts.CHUNKSIZE];

        for (int xMap = 0; xMap < Consts.CHUNKSIZE; xMap++) {
            for (int zMap = 0; zMap < Consts.CHUNKSIZE; zMap++) {
                double nx = (x + xMap) * SCALE;
                double nz = (z + zMap) * SCALE;
                double height = OpenSimplex2.noise2(seed, nx, nz) * 30 + 120;
                heightMap[xMap][zMap] = (int) height;
            }
        }
        chunks.put(x + "," + z, new Chunk(heightMap, x, z));
    }

    private void initWorld() {
        for (int x = -Consts.CHUNKSIZE * renderDistance; x < Consts.CHUNKSIZE * renderDistance; x
            +=
            Consts.CHUNKSIZE) {
            for (int z = -Consts.CHUNKSIZE * renderDistance; z < renderDistance
            * Consts.CHUNKSIZE; z += Consts.CHUNKSIZE) {
                generateChunk(x, z);
            }
        }
        currentChunkPos[0] = 0;
        currentChunkPos[1] = 0;
        currentChunk = chunks.get("0,0");
    }

    private void updateChunks() {
        
    }

    public void updatePlayerPosition(Vector3f pos) {
        player.updatePlayerPosition(pos);
        if (player.getX() < currentChunk.getX()) {
            currentChunk = chunks.get((currentChunkPos[0] - Consts.CHUNKSIZE) + "," + currentChunkPos[1]);
        }
        if (player.getX() >= currentChunk.getX() + Consts.CHUNKSIZE) {
            currentChunk = chunks.get((currentChunkPos[0] + Consts.CHUNKSIZE) + "," + currentChunkPos[1]);
        }
        if (player.getY() < currentChunk.getZ() + Consts.CHUNKSIZE) {
            currentChunk = chunks.get(currentChunkPos[0] + "," + (currentChunkPos[1] - Consts.CHUNKSIZE));
        }
        if (player.getY() >= currentChunk.getZ() + Consts.CHUNKSIZE) {
            currentChunk = chunks.get(currentChunkPos[0] + "," + (currentChunkPos[1] + Consts.CHUNKSIZE));
        }
        updateChunks();
    }

    public Chunk[] getAdjChunks(int x, int z) {
        Chunk[] adjChunks = new Chunk[4];
        adjChunks[0] = chunks.get((x - Consts.CHUNKSIZE) + "," + z); //left chunk
        adjChunks[1] = chunks.get(x + "," + (z - Consts.CHUNKSIZE)); //top chunk
        adjChunks[2] = chunks.get((x + Consts.CHUNKSIZE) + "," + z); //right chunk
        adjChunks[3] = chunks.get(x + "," + (z + Consts.CHUNKSIZE)); //bottom chunk
        return adjChunks;
    }

    public List<Geometry> getChunksGeometry() {
        return chunks.values().stream().map(
            chunk -> chunk.generateMesh(getAdjChunks(chunk.getX(), chunk.getZ()))
        ).toList();
    }

    public List<Chunk> getChunks() {
        return chunks.values().stream().toList();
    }

    public Chunk getChunkFromPos(int x, int y) {
        return chunks.get(x + "," + y);
    }

    public Chunk getChunkFromPlayerPos(int x, int y) {
        return chunks.get(x + "," + y);
    }
}   


