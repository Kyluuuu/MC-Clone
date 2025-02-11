package javamc;

import java.util.ArrayList;
import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import java.util.List;

public class Chunk {
    private short[][] blockTops = new short[Consts.CHUNKSIZE][Consts.CHUNKSIZE];
    private byte[] blocks = new byte[Consts.CHUNKSIZE * Consts.WORLDHEIGHT * Consts.CHUNKSIZE];

    private int x, z;
    private Geometry chunkGeometry;

    private float[] vertices;
    private float[] uvs;
    private short[] indices;
    private BoundingBox rayCastVolume;

    private int vIndex, uvIndex, inIndex, indiCounter;

    public Chunk(short[][] blockTops, int x, int z, short highest, short lowest) {
        this.blockTops = blockTops;
        this.x = x;
        this.z = z;
        generateBlockPlacement(highest, lowest);
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

    public BoundingBox getRayCastVolume() {
        return rayCastVolume;
    }

    private void generateBlockPlacement(short highest, short lowest) {
        for (int yV = 0; yV <= highest; yV++) {
            for (int zV = 0; zV < Consts.CHUNKSIZE; zV++) {
                for (int xV = 0; xV < Consts.CHUNKSIZE; xV++) {
                    int index = XYZposToBlockArrayPos(xV, yV, zV);
                    if (yV > blockTops[xV][zV]) {
                        blocks[index] = Consts.BlockName.Air.Value;
                    } else if (yV >= Consts.SNOWLAYER) {
                        blocks[index] = Consts.BlockName.Snow_Block.Value;
                    } else if (yV >= Consts.STONELAYER
                            || yV < blockTops[xV][zV] - Consts.DIRTLAYER) {
                        blocks[index] = Consts.BlockName.Stone_Block.Value;
                    } else if (yV == blockTops[xV][zV]) {
                        blocks[index] = Consts.BlockName.Grass_Block.Value;
                    } else {
                        blocks[index] = Consts.BlockName.Dirt_Block.Value;
                    }
                }
            }
        }

        rayCastVolume = new BoundingBox(new Vector3f(x, lowest, z),
                new Vector3f(x + Consts.CHUNKSIZE - 1, highest, z + Consts.CHUNKSIZE - 1));
    }

    private int XYZposToBlockArrayPos(int x, int y, int z) {
        return Consts.CHUNKSIZE * Consts.CHUNKSIZE * y + z * Consts.CHUNKSIZE + x;
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
        chunkGeometry = new Geometry("CustomMesh", chunkMesh);

        // move the geometry to its respective chunk coordinates
        chunkGeometry.move(x, 0, z);

        vertices = null;
        uvs = null;
        indices = null;
        indiCounter = 0;
        vIndex = 0;
        uvIndex = 0;
        inIndex = 0;
    }

    // calculates vertices length needed for matrices for the mesh generation
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
        if (adjChunks[Consts.LEFT] != null) {
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
        if (adjChunks[Consts.RIGHT] != null) {
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
        if (adjChunks[Consts.UP] != null) {
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
        if (adjChunks[Consts.DOWN] != null) {
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
        texturePos[0] += Consts.TEXTUREBLEEDOFFSET;
        texturePos[1] += Consts.TEXTUREBLEEDOFFSET;

        uvs[uvIndex++] = texturePos[0];
        uvs[uvIndex++] = texturePos[1];

        uvs[uvIndex++] = texturePos[0];
        uvs[uvIndex++] = texturePos[1] + Consts.TEXTUREBLEEDOFFSETX;

        uvs[uvIndex++] = texturePos[0] + Consts.TEXTUREBLEEDOFFSETX;
        uvs[uvIndex++] = texturePos[1] + Consts.TEXTUREBLEEDOFFSETZ;

        uvs[uvIndex++] = texturePos[0] + Consts.TEXTUREBLEEDOFFSETZ;
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


