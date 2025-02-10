package javamc;

public class Start {
    public static void main(String[] args) {
        Renderer.getInstance();
        World.getInstance();
    }
}

// things to do in Minecraft

// multithread chunk generation
// save a world
// if chunks are far away completely unload them and store them in storage

// gpu optimizations

// - occlusion culling, cull chunks that are blocked using raycasting, problem with glass
// make glass serperate mesh
// - batch/LOD of chunks, chunks that are further away can combine block meshes of the same type
// need batch and occlusion culling to reduce gpu vertex memory in actively loaded chunks and to
// improve performance

// gui
// raycasting for showing which block the player is currently looking at, only need 1 ray

//optimise chunk memory by using short instead of int, block amounts that exceed short can just be repeated again
// might be half of 32000 for negatives

//untouched chunks can be made static and therefore can be fully forgotten when unrendering
//as these chunks can be regenerated from the noise maps, however im not sure this is good or not
//however modified chunks cannot be forgotten

//need to save chunks to save memory 
