package com.mcmacker4.raytracer.renderer.cla

import org.lwjgl.opencl.CL10.clGetPlatformIDs
import org.lwjgl.system.MemoryStack
import java.nio.IntBuffer


data class CLPlatform(val id: Long) {
    
    companion object {
        
        fun getPlatforms() : List<CLPlatform> {
            
            val platforms = arrayListOf<CLPlatform>()
            
            MemoryStack.stackPush().use { stack ->
                //Get number of platforms
                val numBuff = stack.mallocInt(1)
                clGetPlatformIDs(null, numBuff)
                val num = numBuff.get()

                if(num > 0) {
                    //Get platforms
                    val platformsBuff = stack.mallocPointer(num)
                    clGetPlatformIDs(platformsBuff, null as IntBuffer?)

                    while (platformsBuff.hasRemaining()) {
                        val id = platformsBuff.get()
                        platforms.add(CLPlatform(id))
                    }
                }
            }
            
            return platforms
            
        } 
        
    }
    
}