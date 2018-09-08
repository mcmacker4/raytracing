package com.mcmacker4.raytracer.renderer

import com.mcmacker4.raytracer.util.*
import com.mcmacker4.raytracer.scene.Camera
import com.mcmacker4.raytracer.scene.Scene
import com.mcmacker4.raytracer.tracing.RayTracer
import org.joml.Vector3f
import java.awt.image.BufferedImage
import kotlin.math.sqrt


object SequentialRenderer : Renderer {
    
    override fun render(scene: Scene, camera: Camera, result: BufferedImage) {
        
        val width = result.width
        val height = result.height
        
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
                result.setRGB(i, height - j - 1, pixel)
            }
        }

    }
    
}