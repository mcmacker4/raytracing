package com.mcmacker4.raytracer.renderer.cla

import org.lwjgl.opencl.CL12.*
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer
import java.nio.IntBuffer


data class CLDevice(val id: Long, val platform: CLPlatform) {
    
    private var maxComputeUnits: Int? = null
    
    fun getName() : String {
        MemoryStack.stackPush().use { stack ->
            val lenBuff = stack.mallocPointer(1)
            clGetDeviceInfo(id, CL_DEVICE_NAME, null as ByteBuffer?, lenBuff)
            val len = lenBuff.get().toInt()
            val nameBuff = stack.malloc(len)
            clGetDeviceInfo(id, CL_DEVICE_NAME, nameBuff, null)
            val nameArray = ByteArray(len - 1) //Last is null byte
            nameBuff.get(nameArray)
            return String(nameArray)
        }
    }
    
    fun getMaxComputeUnits() : Int {
        MemoryStack.stackPush().use { stack ->
            val units = stack.mallocInt(1)
            clGetDeviceInfo(id, CL_DEVICE_MAX_COMPUTE_UNITS, units, null)
            return units.get()
        }
    }

    fun getMaxWorkGroupSize() : Int {
        MemoryStack.stackPush().use { stack ->
            val units = stack.mallocInt(1)
            clGetDeviceInfo(id, CL_DEVICE_MAX_WORK_GROUP_SIZE, units, null)
            return units.get()
        }
    }
    
    fun getMaxWorkItemDimensions() : Int {
        MemoryStack.stackPush().use { stack ->
            val units = stack.mallocInt(1)
            clGetDeviceInfo(id, CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS, units, null)
            return units.get()
        }
    }
    
    fun getMaxWorkItemSizes() : LongArray {
        MemoryStack.stackPush().use { stack ->
            val len = getMaxWorkItemDimensions()
            val lenBuff = stack.mallocPointer(1)
            lenBuff.put(len.toLong())
            lenBuff.flip()
            val sizesBuff = stack.mallocLong(len)
            clGetDeviceInfo(id, CL_DEVICE_MAX_WORK_ITEM_SIZES, sizesBuff, lenBuff)
            val sizesArray = LongArray(len)
            sizesBuff.get(sizesArray)
            return sizesArray
        }
    }

    companion object {
        
        fun findDeviceWithFallback(type: Int, platform: CLPlatform): List<CLDevice> {
            val valid = getDevices(type, platform)
            return if(valid.isEmpty())
                getDevices(CL_DEVICE_TYPE_DEFAULT, platform)
            else valid
        }

        fun getDevices(type: Int, platform: CLPlatform) : List<CLDevice> {

            val devices = arrayListOf<CLDevice>()

            MemoryStack.stackPush().use { stack ->
                //Get number of devices
                val numBuff = stack.mallocInt(1)
                clGetDeviceIDs(platform.id, type.toLong(), null, numBuff)
                val num = numBuff.get()

                if(num > 0) {
                    //Get devices
                    val devicesBuff = stack.mallocPointer(num)
                    clGetDeviceIDs(platform.id, type.toLong(), devicesBuff, null as IntBuffer?)

                    while (devicesBuff.hasRemaining()) {
                        val id = devicesBuff.get()
                        devices.add(CLDevice(id, platform))
                    }

                }

            }

            return devices

        }
        
    }
    
}