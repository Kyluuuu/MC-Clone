package javamc;

import java.util.Random;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class World {

    private static World instance = null;

    public static synchronized World getInstance() {
        if (instance == null) {
            instance = new World();
        }
        return instance;
    }

    private int renderDistance = 3;
    private int constantRenderDistance = 2; // 1 block around the player, so 3x3, if 2 then 5x5
    private Random rand = new Random();
    private long seed;
    private HashMap<String, Chunk> chunks = new HashMap<>();
    private static final double SCALE = 0.009;
    private Player player;
    private Chunk currentChunk;
    private int[] currentChunkPos;

    private World() {
        currentChunkPos = new int[2];
        seed = rand.nextInt(10000);
        player = new Player();
    }

    public void isReady() {
        initWorld();
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
        Chunk chunk = new Chunk(heightMap, x, z);
        chunks.put(x + "," + z, chunk);
    }

    private void initWorld() {
        // makes a 3x3 chunk grid around the player spawn
        for (int xU = -Consts.CHUNKSIZE * constantRenderDistance; xU < Consts.CHUNKSIZE
                + Consts.CHUNKSIZE * constantRenderDistance; xU += Consts.CHUNKSIZE) {
            for (int zU = -Consts.CHUNKSIZE * constantRenderDistance; zU < Consts.CHUNKSIZE
                    + Consts.CHUNKSIZE * constantRenderDistance; zU += Consts.CHUNKSIZE) {
                generateChunk(xU, zU);
            }
        }
        currentChunkPos[0] = 0;
        currentChunkPos[1] = 0;
        currentChunk = chunks.get("0,0");
        renderChunks(getInitChunkGeometry(), null);
    }

    public void updatePlayerPosition(Vector3f pos) {
        player.updatePlayerPosition(pos);
        int direction = 0;
        // left
        if (player.getX() < currentChunk.getX() - Consts.CHUNKSIZE / 4) {
            currentChunk =
                    chunks.get((currentChunkPos[0] - Consts.CHUNKSIZE) + "," + currentChunkPos[1]);
            direction = 1;
        }
        // right
        // else if (player.getX() >= currentChunk.getX() + Consts.CHUNKSIZE * 5 / 4) {
        //     currentChunk =
        //             chunks.get((currentChunkPos[0] + Consts.CHUNKSIZE) + "," + currentChunkPos[1]);
        //     direction = 2;
        // }
        // // up
        // else if (player.getZ() < currentChunk.getZ() - Consts.CHUNKSIZE / 4) {
        //     currentChunk =
        //             chunks.get(currentChunkPos[0] + "," + (currentChunkPos[1] - Consts.CHUNKSIZE));
        //     direction = 3;
        // }
        // // down
        // else if (player.getZ() >= currentChunk.getZ() + Consts.CHUNKSIZE * 5 / 4) {
        //     currentChunk =
        //             chunks.get(currentChunkPos[0] + "," + (currentChunkPos[1] + Consts.CHUNKSIZE));
        //     direction = 4;
        // }

        if (direction != 0) {
            updateChunks(direction);
        }
    }

    public Chunk[] getAdjChunks(int x, int z) {
        Chunk[] adjChunks = new Chunk[4];
        adjChunks[0] = chunks.get((x - Consts.CHUNKSIZE) + "," + z); // left chunk
        adjChunks[1] = chunks.get(x + "," + (z - Consts.CHUNKSIZE)); // top chunk
        adjChunks[2] = chunks.get((x + Consts.CHUNKSIZE) + "," + z); // right chunk
        adjChunks[3] = chunks.get(x + "," + (z + Consts.CHUNKSIZE)); // bottom chunk
        return adjChunks;
    }

    private void updateChunks(int direction) {
        int xU = currentChunkPos[0];
        int zU = currentChunkPos[1];
        int xDirection = 0;
        int zDirection = 0;
        switch (direction) {
            case 1:
                xU -= Consts.CHUNKSIZE;
                zU -= Consts.CHUNKSIZE;
                zDirection = 1;
                break;
            case 2:
                xU += Consts.CHUNKSIZE;
                zU -= Consts.CHUNKSIZE;
                zDirection = 1;
                break;
            case 3:
                xU -= Consts.CHUNKSIZE;
                zU -= Consts.CHUNKSIZE;
                xDirection = 1;
                break;
            case 4:
                xU -= Consts.CHUNKSIZE;
                zU += Consts.CHUNKSIZE;
                xDirection = 1;
                break;
        }

        List<Geometry> renderChunkGeometies = new ArrayList<>();
        List<Geometry> unrenderChunkGeometies = new ArrayList<>();

        for (int i = 0; i < 1 + constantRenderDistance * 2; i++) {
            String chunkID = (xU + xU * xDirection * i) + "," + (zU + zU * zDirection * i);
            if (!chunks.containsKey(chunkID)) {
                generateChunk(xU + xU * xDirection * i, zU + zU * zDirection * i);
            }
            renderChunkGeometies.add(chunks.get(chunkID).getPreGeneratedGeometry());
        }

        for (int i = 0; i < 1 + constantRenderDistance * 2; i++) {
            String chunkID = (xU + xU * xDirection * i) + "," + (zU + zU * zDirection * i);
            if (chunks.containsKey(chunkID)) {
                unrenderChunkGeometies.add(chunks.get(chunkID).getPreGeneratedGeometry());
            }
        }

        renderChunks(renderChunkGeometies, unrenderChunkGeometies);
    }

    private void renderChunks(List<Geometry> renderChunks, List<Geometry> unrenderChunks) {
        System.out.println("world rendering chunks");
        Renderer.getInstance().renderChunks(renderChunks, unrenderChunks);
    }

    private List<Geometry> getInitChunkGeometry() {
        return chunks.values().stream()
        .map(chunk -> chunk.getPreGeneratedGeometry())
        .toList();
    }

}


