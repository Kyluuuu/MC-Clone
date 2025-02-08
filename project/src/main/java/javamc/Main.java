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

public class Main extends SimpleApplication {

    World world;

    public Main() {
        world = new World();
        start();

    }

    @Override
    public void simpleInitApp() {
        setDisplayStatView(false);
        flyCam.setMoveSpeed(60f);
        cam.setFrustumFar(500f);

        Texture hdrTexture = assetManager.loadTexture("skyHdr.hdr");
        hdrTexture.getImage().setColorSpace(null);
        Spatial sky =
                SkyFactory.createSky(assetManager, hdrTexture, SkyFactory.EnvMapType.EquirectMap);
        rootNode.attachChild(sky);

        // coordinates for jmonkey, x, y, z, where y is vertical

        // Grid grid = new Grid(10, 10, 1.0f); // (Columns, Rows, Spacing)
        // Geometry gridGeo = new Geometry("Grid", grid);

        // Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        // mat1.setColor("Color", ColorRGBA.White);
        // gridGeo.setMaterial(mat1);

        // rootNode.attachChild(gridGeo);

        for (Chunk chunk : world.getChunks()) {
            Geometry chunkGeometry = chunk.generateMesh();
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            Texture texture = assetManager.loadTexture("McTextureAtlas.png");
            texture.setMagFilter(Texture.MagFilter.Nearest);
            texture.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
            mat.setTexture("ColorMap", texture);
            chunkGeometry.setMaterial(mat);
            rootNode.attachChild(chunkGeometry);
            // chunk.clearMeshBuffer();
        }
    }

    @Override
    public void simpleUpdate(float tpf) {

    }

    public void attachMesh(Geometry mesh) {
        rootNode.attachChild(mesh);
    }

    public void detachMesh(Geometry mesh) {
        rootNode.detachChild(mesh);
    }
}


