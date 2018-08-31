package com.mcmacker4.raytracer

import com.mcmacker4.raytracer.util.*
import com.mcmacker4.raytracer.scene.Camera
import com.mcmacker4.raytracer.scene.Scene
import com.mcmacker4.raytracer.tracing.RayTracer
import org.joml.Vector3f
import java.awt.image.BufferedImage
import kotlin.math.sqrt


object Renderer {
    
    fun render(scene: Scene, camera: Camera, image: BufferedImage) : BufferedImage {
        
        val width = image.width
        val height = image.height
        
        val raysPerPixel = 400
        
        for(j in 0 until height) {
            if(j % 10 == 0) println(j)
            for (i in 0 until width) {
                var color = Vector3f(0f)
                for(n in 0 until raysPerPixel) {
                    val u = (i.toFloat() + Math.random().toFloat()) / width
                    val v = (j.toFloat() + Math.random().toFloat()) / height
                    color += RayTracer.shoot(scene, camera.getRay(u, v))
                }
                color /= raysPerPixel.toFloat()
                color.set(sqrt(color.x), sqrt(color.y), sqrt(color.z))
                val pixel = asPixel(color)
                image.setRGB(i, height - j - 1, pixel)
            }
        }

        return image

    }
    
//    private fun interpolate(valA: Vector3f, valB: Vector3f, valC: Vector3f, posA: Vector3f, posB: Vector3f, posC: Vector3f, pos: Vector3f) : Vector3f {
//        val wA = ((posB.y - posC.y) * (pos.x - posC.x) + (posC.x - posB.x) * (pos.y - posC.y)) /
//                ((posB.y - posC.y) * (posA.x - posC.x) + (posC.x - posB.x) * (posA.y - posC.y))
//        val wB = ((posC.y - posA.y) * (pos.x - posC.x) + (posA.x - posC.x) * (pos.y - posC.y)) /
//                ((posB.y - posC.y) * (posA.x - posC.x) + (posC.x - posB.x) * (posA.y - posC.y))
//        val wC = 1 - wA - wB
//        return valA * wA + valB * wB + valC * wC
//    }
    
}