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

// untouched chunks can be made static and therefore can be fully forgotten when unrendering
// as these chunks can be regenerated from the noise maps, however im not sure this is good or not
// however modified chunks cannot be forgotten

// need to save chunks to save memory
// gracefully end the program later like saving the chunks



// ray casting
// would first have to get all chunks currently loaded in the frustum each frame, then raycast the
// scene with a lot of rays
// get the return list of chunks and only render those

// could only use 8 points for generating the bounding box of a chunk for raycasting

// need geometries within the frustum, however to get this i need to basically do frustum cullign
// which is already done by the engine
// however the engine does not give a list of geometries within the frustum
// so what i can do is disable the frustum culling
// and do it myself with the benefit of getting the list of geometries that are in the frustum
// then in the update frame method, call a new method in the world class that would do the raycast
// generation and then that would call a
// occlusion derender method in the renderer which would be queued, and only be applied to currently
// loaded chunks with geometry being rendered

//for raycasting can do for a layer of chunks, but the chunks behind that need to be reraycast?


//greedymeshing
//can greedy mesh render every chunk
//however normal render a 3x3 chunk radius


