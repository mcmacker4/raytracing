package com.mcmacker4.raytracer.renderer.cla

import org.lwjgl.PointerBuffer
import org.lwjgl.opencl.CL10.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer


data class CLProgram(val id: Long, val context: CLContext) {
    
    fun release() {
        clReleaseProgram(id)
    }
    
    companion object {

        fun createProgram(context: CLContext, source: String) : CLProgram {

            MemoryStack.stackPush().use { stack ->

                //Create program
                val errBuff = stack.mallocInt(1)
                val program = clCreateProgramWithSource(context.id, source, errBuff)

                //Check for errors
                val errnum = errBuff.get()
                if(errnum != CL_SUCCESS) {
                    error("CLProgram creation failed (err: $errnum)")
                }

                //Build program
                val status = clBuildProgram(program, null as PointerBuffer?, "", null, MemoryUtil.NULL)
                if(status != CL_SUCCESS) {
                    val lenBuff = stack.mallocPointer(1)
                    clGetProgramBuildInfo(program, context.device.id, CL_PROGRAM_BUILD_LOG, null as ByteBuffer?, lenBuff)

                    val len = lenBuff.get().toInt()
                    val msgBuff = stack.malloc(len)
                    clGetProgramBuildInfo(program, context.device.id, CL_PROGRAM_BUILD_LOG, msgBuff, null)

                    val msgArray = ByteArray(len - 1)
                    msgBuff.get(msgArray)
                    val msg = String(msgArray)

                    error("Build failed ($status, $msg)")
                }

                return CLProgram(program, context)
            }

        }
        
    }
    
}