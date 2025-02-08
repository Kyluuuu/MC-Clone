package javamc;

import java.util.Random;
import com.jme3.scene.Geometry;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


public class World {
    private int renderDistance = 15;
    private final Random rand = new Random();
    private long seed;
    private HashMap<int[], Chunk> chunks = new HashMap<>();
    private static final double SCALE = 0.009;
    private Player player;
    private Random random = new Random();

    public World() {
        seed = rand.nextInt(10000);
        initWorld();
        player = new Player();
    }

    private void generateChunk(int x, int z) {
        short[][] heightMap = new short[Consts.CHUNKSIZE][Consts.CHUNKSIZE];

        for (int xMap = 0; xMap < Consts.CHUNKSIZE; xMap++) {
            for (int zMap = 0; zMap < Consts.CHUNKSIZE; zMap++) {
                double nx = (x + xMap) * SCALE;
                double nz = (z + zMap) * SCALE;
                // double height = OpenSimplex2.noise2(seed, nx, nz) * 50 + 120;
                double height = OpenSimplex2.noise2(seed, nx, nz) * 5 + 10;
                heightMap[xMap][zMap] = (short) height;
            }
        }


        System.out.println("Generating a chunk!");

        chunks.put(new int[] {x, z}, new Chunk(heightMap, x, z));
    }

    private void initWorld() {
        System.out.println("Generating world...");



        for (int x = -Consts.CHUNKSIZE * renderDistance; x < Consts.CHUNKSIZE * renderDistance; x +=
                Consts.CHUNKSIZE) {
            for (int z = -Consts.CHUNKSIZE * renderDistance; z < renderDistance
                    * Consts.CHUNKSIZE; z += Consts.CHUNKSIZE) {
                generateChunk(x, z);
            }
        }

        // generateChunk(0, 0);
    }

    public List<Geometry> getChunksGeometry() {
        return chunks.values().stream().map(Chunk::generateMesh).toList();
    }

    public List<Chunk> getChunks() {
        return chunks.values().stream().toList();
    }

    public Chunk getChunkFromPos(int x, int y) {
        return chunks.get(new int[] {x, y});
    }

    public Chunk getChunkFromPlayerPos(int x, int y) {
        return chunks.get(new int[] {x, y});
    }
}


