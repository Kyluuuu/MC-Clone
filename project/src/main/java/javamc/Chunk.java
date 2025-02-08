package javamc;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.util.BufferUtils;
import java.nio.*;
import com.jme3.scene.VertexBuffer;


public class Chunk {
    private byte[] blocks = new byte[Consts.CHUNKSIZE * Consts.WORLDHEIGHT * Consts.CHUNKSIZE];

    private int x;
    private int z;
    private Geometry chunkGeometry;
    private Mesh chunkMesh;

    // block array of just the tops the chunk
    private short[][] blockTops = new short[Consts.CHUNKSIZE][Consts.CHUNKSIZE];
    private int indiCounter = 0;

    float[] vertices;
    float[] uvs;
    short[] indices;

    int vIndex = 0;
    int uvIndex = 0;
    int inIndex = 0;

    Vector3f v = new Vector3f();


    public Chunk(short[][] blockTops, int x, int z) {
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

    private void generateBlockPlacement() {
        for (int xV = 0; xV < Consts.CHUNKSIZE; xV++) {
            for (int zV = 0; zV < Consts.CHUNKSIZE; zV++) {
                for (int yV = 0; yV < blockTops[xV][zV] - 1; yV++) {
                    blocks[XYZposToBlockArrayPos(xV, yV, zV)] = 5;
                }
                blocks[XYZposToBlockArrayPos(xV, blockTops[xV][zV] - 1, zV)] = 1;
            }
        }
    }

    private int XYZposToBlockArrayPos(int x, int y, int z) {
        return Consts.CHUNKSIZE * Consts.CHUNKSIZE * y + z * Consts.CHUNKSIZE + x;
    }

    private int[] BlockArrayPosToXYZpos(int index) {
        int zOut = index % Consts.CHUNKSIZE * Consts.CHUNKSIZE;
        int xOut = zOut % Consts.CHUNKSIZE;
        int yOut = (index - zOut) / Consts.CHUNKSIZE * Consts.CHUNKSIZE;
        return new int[] {xOut, yOut, zOut};
    }

    public Geometry generateMesh() {
        int[] bufferLengths = calculateBufferLengths();

        vertices = new float[bufferLengths[0]];
        uvs = new float[bufferLengths[1]];
        indices = new short[bufferLengths[2]];

        generateBlocks();
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
        blockTops = null;
        indiCounter = 0;
        vIndex = 0;
        uvIndex = 0;
        inIndex = 0;

        return geometry;
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

        return new int[] {vertexBufferLength + 1, uvBufferLength + 1, indiceBufferLength + 1};
    }

    private void generateBlocks() {
        for (int xV = 0; xV < Consts.CHUNKSIZE; xV++) {
            for (int zV = 0; zV < Consts.CHUNKSIZE; zV++) {
                for (int yV = 0; yV <= blockTops[xV][zV]; yV++) {
                    processBlock(xV, yV, zV);
                }
            }
        }
    }

    private void processBlock(int xV, int yV, int zV) {
        if (blocks[XYZposToBlockArrayPos(xV, yV, zV)] == 0)
            return;
        if (yV > 0 && blocks[XYZposToBlockArrayPos(xV, yV - 1, zV)] == 0) { // bottom face
            createFace(xV, yV, zV, 3);
        }
        if (blocks[XYZposToBlockArrayPos(xV, yV + 1, zV)] == 0) { // top face
            createFace(xV, yV, zV, 2);
        }
        if (xV == 0 || xV > 0 && blocks[XYZposToBlockArrayPos(xV - 1, yV, zV)] == 0) { // left face
            createFace(xV, yV, zV, 0);
        }
        if (xV == Consts.CHUNKSIZE - 1 || xV < Consts.CHUNKSIZE - 1
                && blocks[XYZposToBlockArrayPos(xV + 1, yV, zV)] == 0) { // rightface
            createFace(xV, yV, zV, 1);
        }
        if (zV == Consts.CHUNKSIZE - 1 || zV < Consts.CHUNKSIZE - 1
                && blocks[XYZposToBlockArrayPos(xV, yV, zV + 1)] == 0) { // front
            // face
            createFace(xV, yV, zV, 5);
        }
        if (zV == 0 || zV > 0 && blocks[XYZposToBlockArrayPos(xV, yV, zV - 1)] == 0) { // backface
            createFace(xV, yV, zV, 4);
        }
    }

    private void createFace(int xV, int yV, int zV, int face) {
        int[] faceVerts = Block.getFaceVerts(face);
        for (int i = 0; i < 3; i++) {
            vertices[vIndex++] = (byte) (xV + faceVerts[i * 3]);
            vertices[vIndex++] = (byte) (yV + faceVerts[i * 3 + 1]);
            vertices[vIndex++] = (byte) (zV + faceVerts[i * 3 + 2]);
        }
        generateIndices();
        generateBlockUVCoordinates(blocks[XYZposToBlockArrayPos(xV, yV, zV)], 2);
    }



    private void generateBlockUVCoordinates(int block, int face) {
        float[] texturePos = Block.getTexturePos(block, face);
        texturePos[0] += 1 / 4200f;
        texturePos[1] += 1 / 4096f;

        uvs[uvIndex++] = (byte) texturePos[0];
        uvs[uvIndex++] = (byte) texturePos[1];

        uvs[uvIndex++] = (byte) texturePos[0];
        uvs[uvIndex++] = ((byte) (texturePos[1] + 1 / 64f - 1 / 4096f));

        uvs[uvIndex++] = ((byte) (texturePos[0] + 1 / 64f - 1 / 4096f));
        uvs[uvIndex++] = ((byte) (texturePos[1] + 1 / 64f - 1 / 4096f));

        uvs[uvIndex++] = ((byte) (texturePos[0] + 1 / 64f - 1 / 4096f));
        uvs[uvIndex++] = (byte) texturePos[1];
    }

    private void generateIndices() {
        indices[indiCounter++] = (short) indiCounter;
        indices[indiCounter++] = ((short) (indiCounter + 1));
        indices[indiCounter++] = ((short) (indiCounter + 2));

        indices[indiCounter++] = ((short) (indiCounter + 2));
        indices[indiCounter++] = ((short) (indiCounter + 3));
        indices[indiCounter++] = (short) indiCounter;
    }

    public void clearMeshBuffer() {
        chunkMesh.clearBuffer(VertexBuffer.Type.Position);
        chunkMesh.clearBuffer(VertexBuffer.Type.TexCoord);
        chunkMesh.clearBuffer(VertexBuffer.Type.Index);
        chunkMesh = null;
    }
}


