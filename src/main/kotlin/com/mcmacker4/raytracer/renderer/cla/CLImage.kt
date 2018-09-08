package com.mcmacker4.raytracer.renderer.cla

import org.lwjgl.opencl.CL10
import org.lwjgl.opencl.CL12.*
import org.lwjgl.opencl.CLImageDesc
import org.lwjgl.opencl.CLImageFormat
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import java.nio.IntBuffer


class CLImage(val id: Long, val context: CLContext) {
    
    fun release() {
        clReleaseMemObject(id)
    }
    
    companion object {
        
        fun nil(context: CLContext) = CLImage(0, context)
        
        private fun createImage(width: Int, height: Int, depth: Int, type: Int, flags: Int, format: Int, dataType: Int, context: CLContext) : CLImage {
            MemoryStack.stackPush().use { stack ->

                val formatStruct = CLImageFormat.malloc()
                        .set(format, dataType)

                val descStruct = CLImageDesc.malloc()
                        .set(type, width.toLong(), height.toLong(), depth.toLong(), 0, 0, 0, 0, 0, 0)

                val errBuff = stack.mallocInt(1)
                val image = clCreateImage(context.id, flags.toLong(), formatStruct, descStruct, null as ByteBuffer?, errBuff)
                
                val errnum = errBuff.get()
                if(errnum != CL_SUCCESS) {
                    error("Failed to create Image object (err: $errnum)")
                }
                
                formatStruct.free()
                descStruct.free()
                
                return CLImage(image, context)

            }
            
        }
        
        fun createImage2D(width: Int, height: Int, flags: Int, context: CLContext) : CLImage {
            return createImage(width, height, 1, CL_MEM_OBJECT_IMAGE2D, flags, CL_RGBA, CL_UNSIGNED_INT8, context)
        }
        
        fun createImage3D(width: Int, height: Int, depth: Int, flags: Int, context: CLContext) : CLImage {
            return createImage(width, height, depth, CL_MEM_OBJECT_IMAGE3D, flags, CL_RGBA, CL_UNSIGNED_INT8, context)
        }
        
        fun createFromTexture(image: BufferedImage, flags: Int, context: CLContext): CLImage {
            MemoryStack.stackPush().use { stack ->
                
                val formatStruct = CLImageFormat.malloc().set(CL_RGBA, CL_UNORM_INT8)
                
                val imageArray = image.getRGB(0, 0, image.width, image.height, null, 0, image.width)
                val imageBuff = MemoryUtil.memAllocInt(image.width * image.height).put(imageArray).flip() as IntBuffer
                
                val errBuff = stack.mallocInt(1)
                val imageId = clCreateImage2D(context.id, flags.toLong(), formatStruct, image.width.toLong(), image.height.toLong(), 0, imageBuff, errBuff)
                
                val errnum = errBuff.get()
                if(errnum != CL10.CL_SUCCESS) {
                    error("Error creating image (err: $errnum)")
                }
                
                formatStruct.free()
                MemoryUtil.memFree(imageBuff)
                
                return CLImage(imageId, context)
                
            }
        }
        
    }
    
}