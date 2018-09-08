package com.mcmacker4.raytracer.renderer.cla

import org.lwjgl.opencl.CL12.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.MemoryUtil.NULL


data class CLContext(val id: Long, val device: CLDevice) {
    
    fun release() {
        clReleaseContext(id)
    }
    
    companion object {
        
        fun createContext(device: CLDevice) : CLContext {
            
            MemoryStack.stackPush().use { stack ->
                
                val propsBuff = stack.mallocPointer(3)
                propsBuff.put(CL_CONTEXT_PLATFORM.toLong())
                propsBuff.put(device.platform.id)
                propsBuff.put(0)
                propsBuff.flip()
                
                
                val contextCB: (err: Long, priv: Long, cb: Long, usrData: Long) -> Unit = { err, _, _, _ ->
                    val ascii = MemoryUtil.memASCII(err)
                    println("CLContext error: $ascii")
                }

                val errBuff = stack.mallocInt(1)
                
                val id = clCreateContext(propsBuff, device.id, contextCB, NULL, errBuff)
                
                val errnum = errBuff.get()
                if(errnum != CL_SUCCESS) {
                    error("Something wenr wrong creating context (err: $errnum)")
                }
                
                return CLContext(id, device)
                
            }
            
        }
        
    }
    
}