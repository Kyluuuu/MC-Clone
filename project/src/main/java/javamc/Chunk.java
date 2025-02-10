package javamc;

import java.util.ArrayList;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import java.util.List;

public class Chunk {
    private int[] blocks;

    private int x;
    private int z;
    private Geometry chunkGeometry;

    // block array of just the tops the chunk
    private int[][] blockTops = new int[Consts.CHUNKSIZE][Consts.CHUNKSIZE];

    private float[] vertices;
    private float[] uvs;
    private short[] indices;

    private int vIndex = 0;
    private int uvIndex = 0;
    private int inIndex = 0;
    private int indiCounter = 0;

    private int currentBlock = 0;
    private int currentLength = 0;
    private int maxLength = 0;

    public Chunk(int[][] blockTops, int x, int z, short highest) {
        this.blockTops = blockTops;
        this.x = x;
        this.z = z;
        generateBlockPlacement(highest);
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public boolean isBlockAir(int x, int y, int z) {
        return blocks[XYZposToBlockArrayPos(x, y, z)] == Consts.BlockName.Air.Value;
    }

    public Geometry getPreGeneratedGeometry() {
        return chunkGeometry;
    }

    public void clearGeometry() {
        chunkGeometry = null;
    }

    public boolean hasGeometry() {
        return chunkGeometry != null;
    }

    private void generateBlockPlacement(short highest) {
        List<Integer> tempBlocks = new ArrayList<>();
        currentBlock = Consts.BlockName.Stone_Block.Value;

        for (int yV = 0; yV <= highest; yV++) {
            for (int zV = 0; zV < Consts.CHUNKSIZE; zV++) {
                for (int xV = 0; xV < Consts.CHUNKSIZE; xV++) {
                    if (yV > blockTops[xV][zV]) {
                        addRLEBlock(tempBlocks, Consts.BlockName.Air.Value);
                    } else if (yV < blockTops[xV][zV] - Consts.DIRTLAYER) {
                        addRLEBlock(tempBlocks, Consts.BlockName.Stone_Block.Value);
                    } else if (yV == blockTops[xV][zV]) {
                        addRLEBlock(tempBlocks, Consts.BlockName.Grass_Block.Value);
                    } else {
                        addRLEBlock(tempBlocks, Consts.BlockName.Dirt_Block.Value);
                    }

                }
            }
        }
        tempBlocks.add(currentBlock);
        tempBlocks.add(maxLength + currentLength);

        blocks = new int[tempBlocks.size() + 1];
        for (int i = 0; i < blocks.length - 1; i++) {
            blocks[i] = tempBlocks.get(i).intValue();
        }
        blocks[blocks.length - 1] = 0;
    }

    private void addRLEBlock(List<Integer> tempBlocks, int newBlock) {
        if (currentBlock == newBlock) {
            currentLength++;
        } else {
            maxLength += currentLength;
            tempBlocks.add(currentBlock);
            tempBlocks.add(maxLength);
            currentBlock = newBlock;
            currentLength = 1;
        }
    }

    private int XYZposToBlockArrayPos(int x, int y, int z) {
        int oldIndex = Consts.CHUNKSIZE * Consts.CHUNKSIZE * y + z * Consts.CHUNKSIZE + x;
        for (int i = 1; i < blocks.length; i += 2) {
            if (oldIndex < blocks[i]) {
                return i - 1;
            }
        }
        return blocks.length - 1;
    }

    public void generateMesh(Chunk[] adjChunks, int distance) {
        int[] bufferLengths = calculateBufferLengths(adjChunks);
        // System.out.println(distance);w

        vertices = new float[bufferLengths[0]];
        uvs = new float[bufferLengths[1]];
        indices = new short[bufferLengths[2]];

        generateBlocksGeometry(adjChunks);

        Mesh chunkMesh = new Mesh();
        // Set mesh mode to Triangles
        chunkMesh.setMode(Mesh.Mode.Triangles);
        // set vertices
        chunkMesh.setBuffer(VertexBuffer.Type.Position, 3, vertices);
        // set uv coords
        chunkMesh.setBuffer(VertexBuffer.Type.TexCoord, 2, uvs);
        // Set indices
        chunkMesh.setBuffer(VertexBuffer.Type.Index, 3, indices);
        // update the bounding box of the mesh
        chunkMesh.updateBound();

        // create the geomoetry using the mesh
        Geometry geometry = new Geometry("CustomMesh", chunkMesh);

        // move the geometry to its respective chunk coordinates
        geometry.move(x, 0, z);

        chunkGeometry = geometry;
        vertices = null;
        uvs = null;
        indices = null;
        indiCounter = 0;
        vIndex = 0;
        uvIndex = 0;
        inIndex = 0;


    }

    //calculates vertices length needed for matrices for the mesh generation
    private int[] calculateBufferLengths(Chunk[] adjChunks) {
        int vertexBufferLength = 0;
        int uvBufferLength = 0;
        int indiceBufferLength = 0;

        for (int xV = 0; xV < Consts.CHUNKSIZE; xV++) {
            for (int zV = 0; zV < Consts.CHUNKSIZE; zV++) {
                for (int yV = 0; yV <= blockTops[xV][zV]; yV++) {
                    if (isBlockAir(xV, yV, zV))
                        continue;

                    int faces = 0;
                    if (isBlockAir(xV, yV + 1, zV)) { // top face
                        faces++;
                    }
                    if (yV > 0 && isBlockAir(xV, yV - 1, zV)) { // bottom face
                        faces++;
                    }
                    if (xV != Consts.CHUNKSIZE - 1 && isBlockAir(xV + 1, yV, zV)) { // rightface
                        faces++;
                    }
                    if (zV != Consts.CHUNKSIZE - 1 && isBlockAir(xV, yV, zV + 1)) { // frontface
                        faces++;
                    }
                    if (zV > 0 && isBlockAir(xV, yV, zV - 1)) { // backface
                        faces++;
                    }
                    if (xV > 0 && isBlockAir(xV - 1, yV, zV)) { // left face
                        faces++;
                    }
                    vertexBufferLength += faces * 12;
                    uvBufferLength += faces * 8;
                    indiceBufferLength += faces * 6;
                }
            }
        }

        if (adjChunks == null)
            return new int[] {vertexBufferLength, uvBufferLength, indiceBufferLength};

        int faces = 0;
        // left face
        if (adjChunks[0] != null) {
            int xVLR = 0;
            for (int zVLR = 0; zVLR < Consts.CHUNKSIZE; zVLR++) {
                for (int yVLR = 0; yVLR <= blockTops[xVLR][zVLR]; yVLR++) {
                    if (adjChunks[0].isBlockAir(Consts.CHUNKSIZE - 1, yVLR, zVLR)) {
                        faces++;
                    }
                }
            }
        }

        // right face
        if (adjChunks[2] != null) {
            int xVLR = Consts.CHUNKSIZE - 1;
            for (int zVLR = 0; zVLR < Consts.CHUNKSIZE; zVLR++) {
                for (int yVLR = 0; yVLR <= blockTops[xVLR][zVLR]; yVLR++) {
                    if (adjChunks[2].isBlockAir(0, yVLR, zVLR)) {
                        faces++;
                    }
                }
            }
        }

        // backface
        if (adjChunks[1] != null) {
            int zVFB = 0;
            for (int xVFB = 0; xVFB < Consts.CHUNKSIZE; xVFB++) {
                for (int yVFB = 0; yVFB <= blockTops[xVFB][zVFB]; yVFB++) {
                    if (adjChunks[1].isBlockAir(xVFB, yVFB, Consts.CHUNKSIZE - 1)) {
                        faces++;
                    }
                }
            }
        }
        // frontface
        if (adjChunks[3] != null) {
            int zVFB = Consts.CHUNKSIZE - 1;
            for (int xVFB = 0; xVFB < Consts.CHUNKSIZE; xVFB++) {
                for (int yVFB = 0; yVFB <= blockTops[xVFB][zVFB]; yVFB++) {
                    if (adjChunks[3].isBlockAir(xVFB, yVFB, 0)) {
                        faces++;
                    }
                }
            }
        }

        vertexBufferLength += faces * 12;
        uvBufferLength += faces * 8;
        indiceBufferLength += faces * 6;

        return new int[] {vertexBufferLength, uvBufferLength, indiceBufferLength};
    }

    

    private void generateBlocksGeometry(Chunk[] adjChunks) {
        for (int xV = 0; xV < Consts.CHUNKSIZE; xV++) {
            for (int zV = 0; zV < Consts.CHUNKSIZE; zV++) {
                for (int yV = 0; yV <= blockTops[xV][zV]; yV++) {
                    if (isBlockAir(xV, yV, zV))
                        continue;
                    processInnerBlockFace(xV, yV, zV);
                }
            }
        }
        processChunkBorderFace(adjChunks);
    }

    private void processInnerBlockFace(int xV, int yV, int zV) {
        if (isBlockAir(xV, yV + 1, zV)) { // top face
            createFace(xV, yV, zV, 2);
        }
        if (yV > 0 && isBlockAir(xV, yV - 1, zV)) { // bottom face
            createFace(xV, yV, zV, 3);
        }
        if (xV != Consts.CHUNKSIZE - 1 && isBlockAir(xV + 1, yV, zV)) { // rightface
            createFace(xV, yV, zV, 1);
        }
        if (zV != Consts.CHUNKSIZE - 1 && isBlockAir(xV, yV, zV + 1)) { // frontface
            createFace(xV, yV, zV, 5);
        }
        if (zV > 0 && isBlockAir(xV, yV, zV - 1)) { // backface
            createFace(xV, yV, zV, 4);
        }
        if (xV > 0 && isBlockAir(xV - 1, yV, zV)) { // left face
            createFace(xV, yV, zV, 0);
        }
    }

    private void processChunkBorderFace(Chunk[] adjChunks) {
        if (adjChunks == null)
            return;
        // left face
        if (adjChunks[0] != null) {
            int xVLR = 0;
            for (int zVLR = 0; zVLR < Consts.CHUNKSIZE; zVLR++) {
                for (int yVLR = 0; yVLR <= blockTops[xVLR][zVLR]; yVLR++) {
                    if (adjChunks[0].isBlockAir(Consts.CHUNKSIZE - 1, yVLR, zVLR)) {
                        createFace(xVLR, yVLR, zVLR, 0);
                    }
                }
            }
        }

        // right face
        if (adjChunks[2] != null) {
            int xVLR = Consts.CHUNKSIZE - 1;
            for (int zVLR = 0; zVLR < Consts.CHUNKSIZE; zVLR++) {
                for (int yVLR = 0; yVLR <= blockTops[xVLR][zVLR]; yVLR++) {
                    if (adjChunks[2].isBlockAir(0, yVLR, zVLR)) {
                        createFace(xVLR, yVLR, zVLR, 1);
                    }
                }
            }
        }

        // backface
        if (adjChunks[1] != null) {
            int zVFB = 0;
            for (int xVFB = 0; xVFB < Consts.CHUNKSIZE; xVFB++) {
                for (int yVFB = 0; yVFB <= blockTops[xVFB][zVFB]; yVFB++) {
                    if (adjChunks[1].isBlockAir(xVFB, yVFB, Consts.CHUNKSIZE - 1)) {
                        createFace(xVFB, yVFB, zVFB, 4);
                    }
                }
            }
        }
        // frontface
        if (adjChunks[3] != null) {
            int zVFB = Consts.CHUNKSIZE - 1;
            for (int xVFB = 0; xVFB < Consts.CHUNKSIZE; xVFB++) {
                for (int yVFB = 0; yVFB <= blockTops[xVFB][zVFB]; yVFB++) {
                    if (adjChunks[3].isBlockAir(xVFB, yVFB, 0)) {
                        createFace(xVFB, yVFB, zVFB, 5);
                    }
                }
            }
        }
    }

    private void createFace(int xV, int yV, int zV, int face) {
        int[] faceVerts = Block.getFaceVerts(face);
        for (int i = 0; i < 4; i++) {
            vertices[vIndex++] = (xV + faceVerts[i * 3]);
            vertices[vIndex++] = (yV + faceVerts[i * 3 + 1]);
            vertices[vIndex++] = (zV + faceVerts[i * 3 + 2]);
        }
        generateIndices();
        generateBlockUVCoordinates(blocks[XYZposToBlockArrayPos(xV, yV, zV)],
                Block.getFaceNumber(face));
    }


    private void generateBlockUVCoordinates(int block, int face) {
        float[] texturePos = Block.getTexturePos(block, face);
        texturePos[0] += 1 / 4200f;
        texturePos[1] += 1 / 4096f;

        uvs[uvIndex++] = texturePos[0];
        uvs[uvIndex++] = texturePos[1];

        uvs[uvIndex++] = texturePos[0];
        uvs[uvIndex++] = (texturePos[1] + 1 / 64f - 1 / 4096f);

        uvs[uvIndex++] = (texturePos[0] + 1 / 64f - 1 / 4096f);
        uvs[uvIndex++] = (texturePos[1] + 1 / 64f - 1 / 4096f);

        uvs[uvIndex++] = (texturePos[0] + 1 / 64f - 1 / 4096f);
        uvs[uvIndex++] = texturePos[1];
    }

    private void generateIndices() {
        indices[inIndex++] = (short) indiCounter;
        indices[inIndex++] = ((short) (indiCounter + 1));
        indices[inIndex++] = ((short) (indiCounter + 2));

        indices[inIndex++] = ((short) (indiCounter + 2));
        indices[inIndex++] = ((short) (indiCounter + 3));
        indices[inIndex++] = (short) indiCounter;

        indiCounter += 4;
    }
}


