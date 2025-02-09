package javamc;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;


public class Chunk {
    private byte[] blocks = new byte[Consts.CHUNKSIZE * Consts.WORLDHEIGHT * Consts.CHUNKSIZE];

    private int x;
    private int z;
    private Geometry chunkGeometry;
    private Mesh chunkMesh;

    // block array of just the tops the chunk
    private int[][] blockTops = new int[Consts.CHUNKSIZE][Consts.CHUNKSIZE];

    private float[] vertices;
    private float[] uvs;
    private short[] indices;

    private int vIndex = 0;
    private int uvIndex = 0;
    private int inIndex = 0;
    private int indiCounter = 0;

    public Chunk(int[][] blockTops, int x, int z) {
        this.blockTops = blockTops;
        this.x = x;
        this.z = z;
        generateBlockPlacement();
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public boolean isBlockAir(int x, int y, int z) {
        return blocks[XYZposToBlockArrayPos(x, y, z)] == 0;
    }

    public Geometry getPreGeneratedGeometry() {
        return chunkGeometry;
    }

    public boolean hasGeometry() {
        return chunkGeometry != null;
    }

    private void generateBlockPlacement() {
        for (int xV = 0; xV < Consts.CHUNKSIZE; xV++) {
            for (int zV = 0; zV < Consts.CHUNKSIZE; zV++) {
                for (int yV = 0; yV < blockTops[xV][zV]; yV++) {
                    blocks[XYZposToBlockArrayPos(xV, yV, zV)] = 5;
                }
                blocks[XYZposToBlockArrayPos(xV, (blockTops[xV][zV]), zV)] = 1;
            }
        }
    }

    private int XYZposToBlockArrayPos(int x, int y, int z) {
        return Consts.CHUNKSIZE * Consts.CHUNKSIZE * y + z * Consts.CHUNKSIZE + x;
    }

    // private int[] BlockArrayPosToXYZpos(int index) {
    // int zOut = index % Consts.CHUNKSIZE * Consts.CHUNKSIZE;
    // int xOut = zOut % Consts.CHUNKSIZE;
    // int yOut = (index - zOut) / Consts.CHUNKSIZE * Consts.CHUNKSIZE;
    // return new int[] {xOut, yOut, zOut};
    // }

    public void generateMesh(Chunk[] adjChunks) {
        int[] bufferLengths = calculateBufferLengths();

        vertices = new float[bufferLengths[0]];
        uvs = new float[bufferLengths[1]];
        indices = new short[bufferLengths[2]];

        generateBlocks(adjChunks);

        chunkMesh = new Mesh();
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

    private int[] calculateBufferLengths() {
        int vertexBufferLength = 0;
        int uvBufferLength = 0;
        int indiceBufferLength = 0;

        for (int xV = 0; xV < Consts.CHUNKSIZE; xV++) {
            for (int zV = 0; zV < Consts.CHUNKSIZE; zV++) {
                for (int yV = 0; yV <= blockTops[xV][zV]; yV++) {
                    int faces = 0;
                    if (blocks[XYZposToBlockArrayPos(xV, yV, zV)] == 0)
                        continue;
                    if (yV > 0 && blocks[XYZposToBlockArrayPos(xV, yV - 1, zV)] == 0) { // bottom
                        // face
                        faces++;
                    }
                    if (blocks[XYZposToBlockArrayPos(xV, yV + 1, zV)] == 0) { // top face
                        faces++;
                    }
                    if (xV == 0 || xV > 0 && blocks[XYZposToBlockArrayPos(xV - 1, yV, zV)] == 0) { // left
                                                                                                   // face
                        faces++;
                    }
                    if (xV == Consts.CHUNKSIZE - 1 || xV < Consts.CHUNKSIZE - 1
                            && blocks[XYZposToBlockArrayPos(xV + 1, yV, zV)] == 0) { // rightface
                        faces++;
                    }
                    if (zV == Consts.CHUNKSIZE - 1 || zV < Consts.CHUNKSIZE - 1
                            && blocks[XYZposToBlockArrayPos(xV, yV, zV + 1)] == 0) { // front
                        // face
                        faces++;
                    }
                    if (zV == 0 || zV > 0 && blocks[XYZposToBlockArrayPos(xV, yV, zV - 1)] == 0) { // backface
                        faces++;
                    }
                    vertexBufferLength += faces * 12;
                    uvBufferLength += faces * 8;
                    indiceBufferLength += faces * 6;
                }
            }
        }

        return new int[] {vertexBufferLength, uvBufferLength, indiceBufferLength};
    }

    private void generateBlocks(Chunk[] adjChunks) {
        for (int xV = 0; xV < Consts.CHUNKSIZE; xV++) {
            for (int zV = 0; zV < Consts.CHUNKSIZE; zV++) {
                for (int yV = 0; yV <= blockTops[xV][zV]; yV++) {
                    if (blocks[XYZposToBlockArrayPos(xV, yV, zV)] == 0)
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

    public void clearMeshBuffer() {
        chunkGeometry = null;
        if (chunkMesh != null) {
            chunkMesh.clearBuffer(VertexBuffer.Type.Position);
            chunkMesh.clearBuffer(VertexBuffer.Type.TexCoord);
            chunkMesh.clearBuffer(VertexBuffer.Type.Index);
            chunkMesh = null;
        }
    }
}


