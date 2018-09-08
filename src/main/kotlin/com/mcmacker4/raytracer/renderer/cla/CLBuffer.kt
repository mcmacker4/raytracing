package com.mcmacker4.raytracer.renderer.cla

import org.lwjgl.opencl.CL12.*
import org.lwjgl.system.MemoryStack


data class CLBuffer(val id: Long, private val size: Long, val context: CLContext) {
    
    fun release() {
        clReleaseMemObject(id)
    }
    
    companion object {
        
        fun createBuffer(flags: Int, size: Long, context: CLContext): CLBuffer {
            
            MemoryStack.stackPush().use { stack ->
                
                val errBuff = stack.mallocInt(1)
                val buffer = clCreateBuffer(context.id, flags.toLong(), size, errBuff)
                
                val errnum = errBuff.get()
                if(errnum != CL_SUCCESS) {
                    error("Error creating OpenCL memory buffer (err: $errnum)")
                }
                
                return CLBuffer(buffer, size, context)
                
            }
            
        }
        
    }
    
}