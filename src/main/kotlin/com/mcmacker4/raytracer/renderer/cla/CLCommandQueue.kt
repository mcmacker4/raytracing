package com.mcmacker4.raytracer.renderer.cla

import org.lwjgl.PointerBuffer
import org.lwjgl.opencl.CL10.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.awt.image.BufferedImage
import java.nio.FloatBuffer
import java.nio.IntBuffer


data class CLCommandQueue(val id: Long, val context: CLContext) {
    
    fun enqueueWriteBuffer(buffer: CLBuffer, data: IntBuffer) {
        clEnqueueWriteBuffer(id, buffer.id, true, 0, data, null, null)
    }

    fun enqueueNDRangeKernel(kernel: CLKernel, dimensions: Int, globalWorkSizes: LongArray, workGroupSizes: LongArray?) {
        MemoryStack.stackPush().use { stack ->
            val globalWorkSizeBuff = stack.mallocPointer(globalWorkSizes.size)
            globalWorkSizeBuff.put(globalWorkSizes)
            globalWorkSizeBuff.flip()
            
            val workGroupSizesBuff: PointerBuffer? = if(workGroupSizes != null) {
                val workGroupSizesBuff = stack.mallocPointer(workGroupSizes.size)
                workGroupSizesBuff.put(workGroupSizes)
                workGroupSizesBuff.flip()
            } else null
            val status = clEnqueueNDRangeKernel(id, kernel.id, dimensions, null, globalWorkSizeBuff, workGroupSizesBuff, null, null)
            if(status != CL_SUCCESS) {
                error("Error enqueuing ND range kernel (err: $status)")
            }
        }
    }

    fun enqueueReadBuffer(buffer: CLBuffer, ptr: IntBuffer) {
        clEnqueueReadBuffer(id, buffer.id, true, 0, ptr, null, null)
    }

    fun enqueueReadBuffer(buffer: CLBuffer, ptr: FloatBuffer) {
        clEnqueueReadBuffer(id, buffer.id, true, 0, ptr, null, null)
    }
    
    fun enqueueReadImage(image: CLImage, buffer: IntBuffer, startX: Int, startY: Int, startZ: Int, width: Int, height: Int, depth: Int, rowPitch: Int, slicePitch: Int) {
        MemoryStack.stackPush().use { stack ->
            val originBuff = stack.mallocPointer(3)
            originBuff.put(longArrayOf(startX.toLong(), startY.toLong(), startZ.toLong()))
            originBuff.flip()
            
            val regionBuff = stack.mallocPointer(3)
            regionBuff.put(longArrayOf(width.toLong(), height.toLong(), depth.toLong()))
            regionBuff.flip()
            
            clEnqueueReadImage(id, image.id, true, originBuff, regionBuff, rowPitch.toLong(), slicePitch.toLong(), buffer, null, null)
        }
    }
    
    fun release() {
        clReleaseCommandQueue(id)
    }

    fun enqueueWriteImage(imageBuffer: CLImage, image: BufferedImage) {

        MemoryStack.stackPush().use { stack ->

            val originBuff = stack.mallocPointer(3).put(longArrayOf(0, 0, 0)).flip() as PointerBuffer

            val regionBuff = stack.mallocPointer(3)
                    .put(longArrayOf(image.width.toLong(), image.height.toLong(), 1)).flip() as PointerBuffer

            val imageArray = IntArray(image.width * image.height)
            image.getRGB(0, 0, image.width, image.height, imageArray, 0, image.width)
            val imageBuff = MemoryUtil.memAllocInt(image.width * image.height).put(imageArray).flip() as IntBuffer
            
            val errno = clEnqueueWriteImage(id, imageBuffer.id, true, originBuff, regionBuff, 0, 0, imageBuff, null, null)
            if(errno != CL_SUCCESS) {
                error("Error writing image to buffer (err: $errno)")
            }

        }
        
    }

    companion object {
        
        fun createCommandQueue(props: Int, context: CLContext) : CLCommandQueue {
            
            MemoryStack.stackPush().use { stack ->
                
                val errBuff = stack.mallocInt(1)
                val queue = clCreateCommandQueue(context.id, context.device.id, props.toLong(), errBuff)
                
                val errnum = errBuff.get()
                if(errnum != CL_SUCCESS) {
                    error("Error creating command queue (err: $errnum)")
                }
                
                return CLCommandQueue(queue, context)
                
            }
            
        }
        
    }
    
}