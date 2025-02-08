package javamc;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.debug.Grid;
import com.jme3.texture.Texture;
import com.jme3.util.BufferUtils;
import com.jme3.util.SkyFactory;
import com.jme3.scene.shape.Box;
import java.util.List;

public class Main extends SimpleApplication {
    private final Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    private final Texture texture = assetManager.loadTexture("McTextureAtlas.png");

    public Main() {
        start();
        texture.setMagFilter(Texture.MagFilter.Nearest);
        texture.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
        mat.setTexture("ColorMap", texture);
    }

    // coordinates for jmonkey, x, y, z, where y is vertical
    @Override
    public void simpleInitApp() {
        setDisplayStatView(true);
        flyCam.setMoveSpeed(60f);

        cam.setFrustumFar(10000f);

        Texture hdrTexture = assetManager.loadTexture("skyHdr.hdr");
        hdrTexture.getImage().setColorSpace(null);
        Spatial sky =
                SkyFactory.createSky(assetManager, hdrTexture, SkyFactory.EnvMapType.EquirectMap);
        rootNode.attachChild(sky);

    }


    public void renderChunks(List<Chunk> chunks) {
        for (Chunk chunk : chunks) {
            Geometry chunkGeometry = chunk.generateMesh(world.getAdjChunks(chunk.getX(), chunk.getZ()));
            chunkGeometry.setMaterial(mat);
            rootNode.attachChild(chunkGeometry);
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        world.updatePlayerPosition(cam.getLocation());
    }

    public void updateChunkMeshes() {

    }

    public void attachMesh(Geometry mesh) {
        rootNode.attachChild(mesh);
    }

    public void detachMesh(Geometry mesh) {
        rootNode.detachChild(mesh);
    }
}


