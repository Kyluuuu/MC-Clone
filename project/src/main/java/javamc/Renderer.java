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

public class Renderer extends SimpleApplication {

    private static Renderer instance = null;

    public static synchronized Renderer getInstance() {
        if (instance == null) {
            instance = new Renderer();
        }
        return instance;
    }

    private Material mat;

    private Renderer() {
        start();
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

        mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture texture = assetManager.loadTexture("McTextureAtlas.png");
        texture.setMagFilter(Texture.MagFilter.Nearest);
        texture.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
        mat.setTexture("ColorMap", texture);

        World.getInstance().isReady();
    }


    public void renderChunks(List<Geometry> chunks, List<Geometry> unrenderChunks) {
        if (chunks != null) {
            for (Geometry geometry : chunks) {
                geometry.setMaterial(mat);
                rootNode.attachChild(geometry);
            }
        }
        if (unrenderChunks != null) {
            for (Geometry geometry : unrenderChunks) {
                rootNode.detachChild(geometry);
            }
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        World.getInstance().updatePlayerPosition(cam.getLocation());
    }
}


