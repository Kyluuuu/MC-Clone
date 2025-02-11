package javamc;

import java.util.Random;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;


public class World {

    private static World instance = null;

    public static synchronized World getInstance() {
        if (instance == null) {
            instance = new World();
        }
        return instance;
    }

    private int renderDistance = 3;
    private int constantRenderDistance = 80; // 1 block around the player, so 3x3, if 2 then 5x5
    private Random rand = new Random();
    private long seed;
    private ConcurrentHashMap<String, Chunk> chunks = new ConcurrentHashMap<>();
    private static final double SCALE = 0.01;
    private Player player;
    private Chunk currentChunk;
    private int[] currentChunkPos;
    private ExecutorService updatePosThenChunkThreadQueue;
    private ExecutorService chunkGenerationThreadPool;

    private World() {
        currentChunkPos = new int[2];
        seed = rand.nextInt(10000);
        player = new Player();
        updatePosThenChunkThreadQueue = Executors.newSingleThreadExecutor();
        chunkGenerationThreadPool =
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2);
    }

    public void shutdown() {
        // gracefully kill the system later
        System.exit(0);
    }

    public void isReady() {
        initWorld();
    }

    private void generateChunk(int x, int z) {
        if (chunks.containsKey(x + "," + z))
            return;

        short[][] heightMap = new short[Consts.CHUNKSIZE][Consts.CHUNKSIZE];
        short highest = 0;
        short lowest = Consts.WORLDHEIGHT;

        for (int xMap = 0; xMap < Consts.CHUNKSIZE; xMap++) {
            for (int zMap = 0; zMap < Consts.CHUNKSIZE; zMap++) {
                double nx = (x + xMap) * SCALE;
                double nz = (z + zMap) * SCALE;
                int height = getHeight(nx, nz);
                heightMap[xMap][zMap] = (short) height;
                if (height > highest) {
                    highest = (short) height;
                }
                if (height < lowest) {
                    lowest = (short) height;
                }
            }
        }
        chunks.put(x + "," + z, new Chunk(heightMap, x, z, highest, lowest));
    }

    private int getHeight(double nx, double nz) {
        double noise = OpenSimplex2.noise2(seed, nx, nz);
        // noise = Math.pow(Math.max(noise, 0), 5);
        return (int) (noise * 50 + 110);
    }

    private void initWorld() {
        constantRenderDistance++;
        int latchCount = 1 + constantRenderDistance * 2;
        CountDownLatch latch = new CountDownLatch(latchCount * latchCount);

        // creates the chunks around the player
        for (int xU = -Consts.CHUNKSIZE * constantRenderDistance; xU < Consts.CHUNKSIZE
                + Consts.CHUNKSIZE * constantRenderDistance; xU += Consts.CHUNKSIZE) {
            for (int zU = -Consts.CHUNKSIZE * constantRenderDistance; zU < Consts.CHUNKSIZE
                    + Consts.CHUNKSIZE * constantRenderDistance; zU += Consts.CHUNKSIZE) {
                int xUU = xU;
                int zUU = zU;
                chunkGenerationThreadPool.submit(() -> {
                    try {
                        generateChunk(xUU, zUU);
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }


        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.out
                    .println("Failed to load chunks fast enough before meshing in initialisation");
        }

        // generates the chunk meshes of chunks in a certain radius of player
        constantRenderDistance--;
        for (int xU = -Consts.CHUNKSIZE * constantRenderDistance; xU < Consts.CHUNKSIZE
                + Consts.CHUNKSIZE * constantRenderDistance; xU += Consts.CHUNKSIZE) {
            for (int zU = -Consts.CHUNKSIZE * constantRenderDistance; zU < Consts.CHUNKSIZE
                    + Consts.CHUNKSIZE * constantRenderDistance; zU += Consts.CHUNKSIZE) {
                int xUU = xU;
                int zUU = zU;
                chunkGenerationThreadPool.submit(() -> {
                    Chunk curChunk = chunks.get(xUU + "," + zUU);
                    curChunk.generateMesh(getAdjChunks(xUU, zUU, Consts.DIRECTIONALL), Math.max(
                            Math.abs(xUU) / Consts.CHUNKSIZE, Math.abs(zUU) / Consts.CHUNKSIZE));
                    renderChunk(curChunk.getPreGeneratedGeometry());
                });
            }
        }

        currentChunkPos[0] = 0;
        currentChunkPos[1] = 0;
        currentChunk = chunks.get("0,0");

        Renderer.getInstance()
                .setCameraInit(getHeight(Consts.SPAWNPOINTXZ * SCALE, Consts.SPAWNPOINTXZ * SCALE));
    }

    public void updatePlayerPosition(Vector3f pos) {
        updatePosThenChunkThreadQueue.submit(() -> {

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

            int adjChunkDirection = Consts.DIRECTIONALL;

            // left
            if (player.getX() < currentChunk.getX() - Consts.CHUNKSIZE / 4) {
                currentChunk = chunks
                        .get((currentChunkPos[0] - Consts.CHUNKSIZE) + "," + currentChunkPos[1]);
                xRenderChunk = -constantRenderDistance * Consts.CHUNKSIZE;
                zRenderChunk = -constantRenderDistance * Consts.CHUNKSIZE;
                xGenChunk = xRenderChunk - Consts.CHUNKSIZE;
                zGenChunk = zRenderChunk - Consts.CHUNKSIZE;
                xUnrenderChunk += constantRenderDistance * Consts.CHUNKSIZE;
                zUnrenderChunk -= constantRenderDistance * Consts.CHUNKSIZE;
                zDirection = 1;
                adjChunkDirection = Consts.LEFT;
            }
            // right
            else if (player.getX() >= currentChunk.getX() + Consts.CHUNKSIZE * 5 / 4) {
                currentChunk = chunks
                        .get((currentChunkPos[0] + Consts.CHUNKSIZE) + "," + currentChunkPos[1]);
                xRenderChunk = constantRenderDistance * Consts.CHUNKSIZE;
                zRenderChunk = -constantRenderDistance * Consts.CHUNKSIZE;
                xGenChunk = xRenderChunk + Consts.CHUNKSIZE;
                zGenChunk = zRenderChunk - Consts.CHUNKSIZE;
                xUnrenderChunk -= constantRenderDistance * Consts.CHUNKSIZE;
                zUnrenderChunk -= constantRenderDistance * Consts.CHUNKSIZE;
                zDirection = 1;
                adjChunkDirection = Consts.RIGHT;
            }
            // up
            else if (player.getZ() < currentChunk.getZ() - Consts.CHUNKSIZE / 4) {
                currentChunk = chunks
                        .get(currentChunkPos[0] + "," + (currentChunkPos[1] - Consts.CHUNKSIZE));
                xRenderChunk = -constantRenderDistance * Consts.CHUNKSIZE;
                zRenderChunk = -constantRenderDistance * Consts.CHUNKSIZE;
                xGenChunk = xRenderChunk - Consts.CHUNKSIZE;
                zGenChunk = zRenderChunk - Consts.CHUNKSIZE;
                xUnrenderChunk -= constantRenderDistance * Consts.CHUNKSIZE;
                zUnrenderChunk += constantRenderDistance * Consts.CHUNKSIZE;
                xDirection = 1;
                adjChunkDirection = Consts.UP;
            }
            // down
            else if (player.getZ() >= currentChunk.getZ() + Consts.CHUNKSIZE * 5 / 4) {
                currentChunk = chunks
                        .get(currentChunkPos[0] + "," + (currentChunkPos[1] + Consts.CHUNKSIZE));
                xRenderChunk = -constantRenderDistance * Consts.CHUNKSIZE;
                zRenderChunk = constantRenderDistance * Consts.CHUNKSIZE;
                xGenChunk = xRenderChunk - Consts.CHUNKSIZE;
                zGenChunk = zRenderChunk + Consts.CHUNKSIZE;
                xUnrenderChunk -= constantRenderDistance * Consts.CHUNKSIZE;
                zUnrenderChunk -= constantRenderDistance * Consts.CHUNKSIZE;
                xDirection = 1;
                adjChunkDirection = Consts.DOWN;
            } else {
                return;
            }

            currentChunkPos = new int[] {currentChunk.getX(), currentChunk.getZ()};

            xRenderChunk += currentChunkPos[0];
            zRenderChunk += currentChunkPos[1];

            xGenChunk += currentChunkPos[0];
            zGenChunk += currentChunkPos[1];


            CountDownLatch latch = new CountDownLatch(1 + (constantRenderDistance + 1) * 2);

            // generate new chunks in direction
            for (int i = 0; i < 1 + (constantRenderDistance + 1) * 2; i++) {
                int xUL = xGenChunk + i * Consts.CHUNKSIZE * xDirection;
                int zUL = zGenChunk + i * Consts.CHUNKSIZE * zDirection;
                chunkGenerationThreadPool.submit(() -> {
                    try {
                        generateChunk(xUL, zUL);
                    } finally {
                        latch.countDown();
                    }
                });

            }

            // wait until new chunks are done to start doin meshes and unrendering (both have to
            // wait)
            try {
                latch.await(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                System.out.println(
                        "Failed to load chunks fast enough before meshing in initialisation");
            }

            // get meshes
            for (int i = 0; i < 1 + constantRenderDistance * 2; i++) {
                int xUL = xRenderChunk + i * Consts.CHUNKSIZE * xDirection;
                int zUL = zRenderChunk + i * Consts.CHUNKSIZE * zDirection;
                int finalAdjChunkDirection = adjChunkDirection;
                chunkGenerationThreadPool.submit(() -> {
                    String chunkID = xUL + "," + zUL;
                    Chunk chunk = chunks.get(chunkID);
                    if (!chunk.hasGeometry()) {
                        chunk.generateMesh(getAdjChunks(chunk.getX(), chunk.getZ(), finalAdjChunkDirection),
                                Math.max(Math.abs(xUL) / Consts.CHUNKSIZE,
                                        Math.abs(zUL) / Consts.CHUNKSIZE));
                    }
                    renderChunk(chunk.getPreGeneratedGeometry());
                });
            }

            // get unrendering chunk meshes
            for (int i = 0; i < 1 + constantRenderDistance * 2; i++) {
                int xUL = xUnrenderChunk + i * Consts.CHUNKSIZE * xDirection;
                int zUL = zUnrenderChunk + i * Consts.CHUNKSIZE * zDirection;
                String chunkID = xUL + "," + zUL;
                chunkGenerationThreadPool.submit(() -> {
                    if (chunks.containsKey(chunkID)) {
                        Chunk curChunk = chunks.get(chunkID);
                        if (curChunk.hasGeometry()) {
                            unrenderChunk(curChunk.getPreGeneratedGeometry());
                            curChunk.clearGeometry();
                        }

                    }
                });
            }
        });
    }

    public Chunk[] getAdjChunks(int x, int z, int direction) {
        Chunk[] adjChunks = new Chunk[4];
        if (direction == Consts.DIRECTIONALL) {
            adjChunks[Consts.LEFT] = chunks.get((x - Consts.CHUNKSIZE) + "," + z); // left chunk
            adjChunks[Consts.UP] = chunks.get(x + "," + (z - Consts.CHUNKSIZE)); // top chunk
            adjChunks[Consts.RIGHT] = chunks.get((x + Consts.CHUNKSIZE) + "," + z); // right chunk
            adjChunks[Consts.DOWN] = chunks.get(x + "," + (z + Consts.CHUNKSIZE)); // bottom chunk
        }
        else if (direction == Consts.LEFT) {
            adjChunks[Consts.LEFT] = chunks.get((x - Consts.CHUNKSIZE) + "," + z); // left chunk
        }
        else if (direction == Consts.UP) {
            adjChunks[Consts.UP] = chunks.get(x + "," + (z - Consts.CHUNKSIZE)); // top chunk
        }
        else if (direction == Consts.RIGHT) {
            adjChunks[Consts.RIGHT] = chunks.get((x + Consts.CHUNKSIZE) + "," + z); // right chunk
        }
        else {
            adjChunks[Consts.DOWN] = chunks.get(x + "," + (z + Consts.CHUNKSIZE)); // bottom chunk
        }
        return adjChunks;
    }

    private void renderChunk(Geometry renderChunk) {
        Renderer.getInstance().renderChunk(renderChunk);
    }

    private void unrenderChunk(Geometry unrenderChunk) {
        Renderer.getInstance().unrenderChunk(unrenderChunk);
    }
}

//raycasting for occlusion culling
//add all chunks to the scene, before rendering chunk meshes 
//3x3 around player will always render, 
//get all chunks within the players frustum
//then for all these chunks check if the rays collide with their rayVolume, if so render those chuinks else dont




