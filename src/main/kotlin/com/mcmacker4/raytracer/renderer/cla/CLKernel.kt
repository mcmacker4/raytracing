package com.mcmacker4.raytracer.renderer.cla

import org.lwjgl.opencl.CL10.*
import org.lwjgl.system.MemoryStack
import java.nio.IntBuffer


data class CLKernel(val id: Long, val program: CLProgram) {

    fun setArg(index: Int, buffer: CLBuffer) {
        MemoryStack.stackPush().use { stack ->
            val ptr = stack.mallocPointer(1).put(buffer.id).flip()
            clSetKernelArg(id, index, ptr)
        }
    }
    
    fun setArg(index: Int, image: CLImage) {
        MemoryStack.stackPush().use { stack ->
            val ptr = stack.mallocPointer(1).put(image.id).flip()
            clSetKernelArg(id, index, ptr)
        }
    }
    
    fun setArg(index: Int, value: Int) {
        MemoryStack.stackPush().use { stack ->
            val buff = stack.mallocInt(1).put(value).flip() as IntBuffer
            clSetKernelArg(id, index, buff)
        }
    }
    
    fun setArg(index: Int, value: Boolean) {
        MemoryStack.stackPush().use { stack ->
            val buff = stack.mallocInt(1).put(if(value) 1 else 0).flip() as IntBuffer
            clSetKernelArg(id, index, buff)
        }
    }
    
    fun release() {
        clReleaseKernel(id)
    }

    fun setArgInt(index: Int, value: Int) {
        clSetKernelArg(id, index, intArrayOf(value))
    }

    companion object {
        
        fun createKernel(name: String, program: CLProgram) : CLKernel {
            
            MemoryStack.stackPush().use { stack ->
                
                val errBuff = stack.mallocInt(1)
                val kernel = clCreateKernel(program.id, name, errBuff)
                
                
                val errnum = errBuff.get()
                if(errnum != CL_SUCCESS) {
                    error("Error creating kernel (err: $errnum)")
                }
                
                return CLKernel(kernel, program)
                
            }
            
        }
        
    }
    
}