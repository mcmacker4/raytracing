package com.mcmacker4.raytracer.renderer

import com.mcmacker4.raytracer.renderer.cla.*
import com.mcmacker4.raytracer.scene.Camera
import com.mcmacker4.raytracer.scene.Scene
import org.lwjgl.opencl.CL10.*
import org.lwjgl.system.MemoryUtil
import java.awt.image.BufferedImage
import java.util.*


object OpenCLRenderer : Renderer {
    
    private const val SAMPLES = 400
    
    private var device: CLDevice
    private var context: CLContext
    private var program: CLProgram
    private var renderKernel: CLKernel
    private var downsampleKernel: CLKernel
    private var queue: CLCommandQueue
    
    init {
        
        var device: CLDevice? = null
        
        //Find a suitable device (GPU)
        val platforms = CLPlatform.getPlatforms()
        for(platform in platforms) {
            val devices = CLDevice.getDevices(CL_DEVICE_TYPE_GPU, platform)
            if(devices.isNotEmpty()) {
                device = devices[0]
                break
            }
        }
        
        //If GPU is not found, fall back to a default device
        if(device == null) {
            println("No GPU found. Falling back to default device.")
            for(platform in platforms) {
                val devices = CLDevice.getDevices(CL_DEVICE_TYPE_DEFAULT, platform)
                if(devices.isNotEmpty()) {
                    device = devices[0]
                    break
                }
            }
        }

        if(device == null) {
            error("No appropiate OpenCL device found. Please check your video drivers.")
        }
        
        this.device = device
        
        println("Selected device: " + device.getName())

        val workItemSizes = device.getMaxWorkItemSizes()
        if(workItemSizes.size < 3) {
            error("The selected device does not support 2-dimensional work item sizes.")
        }
        
        context = CLContext.createContext(device)
        
        program = CLProgram.createProgram(context, getSource("programs/raytracer.cl"))
        renderKernel = CLKernel.createKernel("render", program)
        downsampleKernel = CLKernel.createKernel("downsample", program)
        
        queue = CLCommandQueue.createCommandQueue(0, context)
        
    }
    
    private fun getSource(path: String) : String {
        val reader = ClassLoader.getSystemClassLoader().getResourceAsStream(path).bufferedReader()
        return reader.lineSequence().joinToString("\n")
    }
    
    private fun renderFullSize(scene: Scene, resultBuffer: CLBuffer, width: Int, height: Int) {
        
        //Send a random seed for each pixel to program
        val seedsBuffer = CLBuffer.createBuffer(CL_MEM_READ_ONLY, width * height * 4L, context)
        val seedsBuff = MemoryUtil.memAllocInt(width * height)
        val rand = Random()
        repeat(seedsBuff.capacity()) { seedsBuff.put(rand.nextInt()) }
        seedsBuff.flip()
        queue.enqueueWriteBuffer(seedsBuffer, seedsBuff)

        //Environment map
        if(scene.environmentMap != null) {
            val imageBuffer = CLImage.createFromTexture(scene.environmentMap.image, CL_MEM_READ_ONLY or CL_MEM_COPY_HOST_PTR, context)
            renderKernel.setArg(0, imageBuffer)
            renderKernel.setArg(1, true)
        } else {
            println("Environment map is null.")
            renderKernel.setArg(0, CLImage.nil(context))
            renderKernel.setArg(1, false)
        }
        
        renderKernel.setArg(2, resultBuffer)
        renderKernel.setArg(3, seedsBuffer)

        val dimensions = 3
        val globalWorkItemSize = longArrayOf(width.toLong(), height.toLong(), SAMPLES.toLong())

        queue.enqueueNDRangeKernel(renderKernel, dimensions, globalWorkItemSize, null)

        seedsBuffer.release()
        MemoryUtil.memFree(seedsBuff)
        
    }
    
    private fun downsample(renderBuffer: CLBuffer, resultBuffer: CLBuffer, width: Int, height: Int) {
        
        val dimensions = 2
        val globalWorkItemSize = longArrayOf(width.toLong(), height.toLong())
        
        downsampleKernel.setArg(0, SAMPLES)
        downsampleKernel.setArg(1, renderBuffer)
        downsampleKernel.setArg(2, resultBuffer)
        
        queue.enqueueNDRangeKernel(downsampleKernel, dimensions, globalWorkItemSize, null)
        
    }
    
    override fun render(scene: Scene, camera: Camera, result: BufferedImage) {
        
        val renderBuffer = CLBuffer.createBuffer(CL_MEM_READ_WRITE, result.width * result.height * SAMPLES * 4L, context)
        val downsampleBuffer = CLBuffer.createBuffer(CL_MEM_WRITE_ONLY, result.width * result.height * 4L, context)
        
        renderFullSize(scene, renderBuffer, result.width, result.height)
        downsample(renderBuffer, downsampleBuffer, result.width, result.height)

        //Read downsampleBuffer
        val downsampleBuff = MemoryUtil.memAllocInt(result.width * result.height)
        queue.enqueueReadBuffer(downsampleBuffer, downsampleBuff)

        val downsampleArray = IntArray(result.width * result.height)
        downsampleBuff.get(downsampleArray)

        result.setRGB(0, 0, result.width, result.height, downsampleArray, 0, result.width)

        MemoryUtil.memFree(downsampleBuff)
        
        renderBuffer.release()
        downsampleBuffer.release()
        
    }
    
    fun cleanUp() {
        queue.release()
        renderKernel.release()
        program.release()
        context.release()
    }
    
}