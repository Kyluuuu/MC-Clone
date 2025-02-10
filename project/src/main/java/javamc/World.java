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
    private int constantRenderDistance = 10; // 1 block around the player, so 3x3, if 2 then 5x5
    private Random rand = new Random();
    private long seed;
    private HashMap<String, Chunk> chunks = new HashMap<>();
    private static final double SCALE = 0.007;
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
        if (chunks.containsKey(x + "," + z))
            return;

        int[][] heightMap = new int[Consts.CHUNKSIZE][Consts.CHUNKSIZE];
        short highest = 0;

        for (int xMap = 0; xMap < Consts.CHUNKSIZE; xMap++) {
            for (int zMap = 0; zMap < Consts.CHUNKSIZE; zMap++) {
                double nx = (x + xMap) * SCALE;
                double nz = (z + zMap) * SCALE;
                int height = getHeight(nx, nz);
                heightMap[xMap][zMap] = height;
                if (height > highest) {
                    highest = (short) height;
                }
            }
        }
        chunks.put(x + "," + z, new Chunk(heightMap, x, z, highest));
    }

    private int getHeight(double nx, double nz) {
        double noise = OpenSimplex2.noise2(seed, nx, nz);
        // noise = Math.pow(Math.max(noise, 0), 5);
        return (int) (noise * 50 + 110);
    }

    private void initWorld() {

        // creates the chunks around the player
        constantRenderDistance++;
        for (int xU = -Consts.CHUNKSIZE * constantRenderDistance; xU < Consts.CHUNKSIZE
                + Consts.CHUNKSIZE * constantRenderDistance; xU += Consts.CHUNKSIZE) {
            for (int zU = -Consts.CHUNKSIZE * constantRenderDistance; zU < Consts.CHUNKSIZE
                    + Consts.CHUNKSIZE * constantRenderDistance; zU += Consts.CHUNKSIZE) {
                generateChunk(xU, zU);
            }
        }

        // generates the chunk meshes of chunks in a certain radius of player
        constantRenderDistance--;
        for (int xU = -Consts.CHUNKSIZE * constantRenderDistance; xU < Consts.CHUNKSIZE
                + Consts.CHUNKSIZE * constantRenderDistance; xU += Consts.CHUNKSIZE) {
            for (int zU = -Consts.CHUNKSIZE * constantRenderDistance; zU < Consts.CHUNKSIZE
                    + Consts.CHUNKSIZE * constantRenderDistance; zU += Consts.CHUNKSIZE) {
                Chunk curChunk = chunks.get(xU + "," + zU);
                curChunk.generateMesh(getAdjChunks(xU, zU));
            }
        }

        // generateChunk(0, 0);
        // chunks.get(0 + "," + 0).generateMesh(null);


        currentChunkPos[0] = 0;
        currentChunkPos[1] = 0;
        currentChunk = chunks.get("0,0");

        Renderer.getInstance()
                .setCameraInit(getHeight(Consts.SPAWNPOINTXZ * SCALE, Consts.SPAWNPOINTXZ * SCALE));

        renderChunks(getInitChunkGeometry(), new ArrayList<>());
    }

    public void updatePlayerPosition(Vector3f pos) {
        Vector3f oldPos = player.getPos();
        player.updatePlayerPosition(pos);
        if (oldPos.getX() == player.getX() && oldPos.getZ() == player.getZ()) {
            return;
        }

        int xRenderChunk = 0;
        int zRenderChunk = 0;

        int xGenChunk = 0;
        int zGenChunk = 0;

        int xUnrenderChunk = currentChunkPos[0];
        int zUnrenderChunk = currentChunkPos[1];

        int xDirection = 0;
        int zDirection = 0;

        // left
        if (player.getX() < currentChunk.getX() - Consts.CHUNKSIZE / 4) {
            currentChunk =
                    chunks.get((currentChunkPos[0] - Consts.CHUNKSIZE) + "," + currentChunkPos[1]);
            xRenderChunk = -constantRenderDistance * Consts.CHUNKSIZE;
            zRenderChunk = -constantRenderDistance * Consts.CHUNKSIZE;
            xGenChunk = xRenderChunk - Consts.CHUNKSIZE;
            zGenChunk = zRenderChunk - Consts.CHUNKSIZE;
            xUnrenderChunk += constantRenderDistance * Consts.CHUNKSIZE;
            zUnrenderChunk -= constantRenderDistance * Consts.CHUNKSIZE;
            zDirection = 1;
        }
        // right
        else if (player.getX() >= currentChunk.getX() + Consts.CHUNKSIZE * 5 / 4) {
            currentChunk =
                    chunks.get((currentChunkPos[0] + Consts.CHUNKSIZE) + "," + currentChunkPos[1]);
            xRenderChunk = constantRenderDistance * Consts.CHUNKSIZE;
            zRenderChunk = -constantRenderDistance * Consts.CHUNKSIZE;
            xGenChunk = xRenderChunk + Consts.CHUNKSIZE;
            zGenChunk = zRenderChunk - Consts.CHUNKSIZE;
            xUnrenderChunk -= constantRenderDistance * Consts.CHUNKSIZE;
            zUnrenderChunk -= constantRenderDistance * Consts.CHUNKSIZE;
            zDirection = 1;
        }
        // up
        else if (player.getZ() < currentChunk.getZ() - Consts.CHUNKSIZE / 4) {
            currentChunk =
                    chunks.get(currentChunkPos[0] + "," + (currentChunkPos[1] - Consts.CHUNKSIZE));
            xRenderChunk = -constantRenderDistance * Consts.CHUNKSIZE;
            zRenderChunk = -constantRenderDistance * Consts.CHUNKSIZE;
            xGenChunk = xRenderChunk - Consts.CHUNKSIZE;
            zGenChunk = zRenderChunk - Consts.CHUNKSIZE;
            xUnrenderChunk -= constantRenderDistance * Consts.CHUNKSIZE;
            zUnrenderChunk += constantRenderDistance * Consts.CHUNKSIZE;
            xDirection = 1;
        }
        // down
        else if (player.getZ() >= currentChunk.getZ() + Consts.CHUNKSIZE * 5 / 4) {
            currentChunk =
                    chunks.get(currentChunkPos[0] + "," + (currentChunkPos[1] + Consts.CHUNKSIZE));
            xRenderChunk = -constantRenderDistance * Consts.CHUNKSIZE;
            zRenderChunk = constantRenderDistance * Consts.CHUNKSIZE;
            xGenChunk = xRenderChunk - Consts.CHUNKSIZE;
            zGenChunk = zRenderChunk + Consts.CHUNKSIZE;
            xUnrenderChunk -= constantRenderDistance * Consts.CHUNKSIZE;
            zUnrenderChunk -= constantRenderDistance * Consts.CHUNKSIZE;
            xDirection = 1;
        } else {
            return;
        }

        currentChunkPos = new int[] {currentChunk.getX(), currentChunk.getZ()};

        xRenderChunk += currentChunkPos[0];
        zRenderChunk += currentChunkPos[1];

        xGenChunk += currentChunkPos[0];
        zGenChunk += currentChunkPos[1];

        List<Geometry> renderChunkGeometies = new ArrayList<>();
        List<Geometry> unrenderChunkGeometies = new ArrayList<>();


        // generate new chunks in direction
        for (int i = 0; i < 1 + (constantRenderDistance + 1) * 2; i++) {
            int xUL = xGenChunk + i * Consts.CHUNKSIZE * xDirection;
            int zUL = zGenChunk + i * Consts.CHUNKSIZE * zDirection;
            generateChunk(xUL, zUL);
        }

        // get meshes
        for (int i = 0; i < 1 + constantRenderDistance * 2; i++) {
            int xUL = xRenderChunk + i * Consts.CHUNKSIZE * xDirection;
            int zUL = zRenderChunk + i * Consts.CHUNKSIZE * zDirection;
            String chunkID = xUL + "," + zUL;
            Chunk chunk = chunks.get(chunkID);
            if (!chunk.hasGeometry()) {
                chunk.generateMesh(getAdjChunks(chunk.getX(), chunk.getZ()));
            }
            renderChunkGeometies.add(chunk.getPreGeneratedGeometry());
        }

        // get geometries of chunks that needed to be unloaded
        for (int i = 0; i < 1 + constantRenderDistance * 2; i++) {
            int xUL = xUnrenderChunk + i * Consts.CHUNKSIZE * xDirection;
            int zUL = zUnrenderChunk + i * Consts.CHUNKSIZE * zDirection;
            String chunkID = xUL + "," + zUL;
            if (chunks.containsKey(chunkID)) {
                Chunk curChunk = chunks.get(chunkID);
                if (curChunk.hasGeometry()) {
                    unrenderChunkGeometies.add(curChunk.getPreGeneratedGeometry());
                    curChunk.clearGeometry();
                }
            }
        }

        renderChunks(renderChunkGeometies, unrenderChunkGeometies);
    }

    public Chunk[] getAdjChunks(int x, int z) {
        Chunk[] adjChunks = new Chunk[4];
        adjChunks[0] = chunks.get((x - Consts.CHUNKSIZE) + "," + z); // left chunk
        adjChunks[1] = chunks.get(x + "," + (z - Consts.CHUNKSIZE)); // top chunk
        adjChunks[2] = chunks.get((x + Consts.CHUNKSIZE) + "," + z); // right chunk
        adjChunks[3] = chunks.get(x + "," + (z + Consts.CHUNKSIZE)); // bottom chunk
        return adjChunks;
    }

    private void renderChunks(List<Geometry> renderChunks, List<Geometry> unrenderChunks) {
        System.out.println("world rendering chunks");
        Renderer.getInstance().renderChunks(renderChunks, unrenderChunks);
    }

    private List<Geometry> getInitChunkGeometry() {
        return chunks.values().stream().filter(Chunk::hasGeometry)
                .map(Chunk::getPreGeneratedGeometry).toList();
    }
}


