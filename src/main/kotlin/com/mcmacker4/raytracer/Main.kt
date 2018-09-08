package com.mcmacker4.raytracer

import com.mcmacker4.raytracer.material.Dielectric
import com.mcmacker4.raytracer.material.Lambertian
import com.mcmacker4.raytracer.material.Metallic
import com.mcmacker4.raytracer.model.Sphere
import com.mcmacker4.raytracer.renderer.OpenCLRenderer
import com.mcmacker4.raytracer.renderer.ParallelRenderer
import com.mcmacker4.raytracer.scene.Camera
import com.mcmacker4.raytracer.scene.EnvironmentMap
import com.mcmacker4.raytracer.scene.Scene
import org.joml.Vector3f
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.system.measureTimeMillis


fun main(args: Array<String>) {

    val width = 1920
    val height = 1080
    val aspect = width.toFloat() / height

    val environ = EnvironmentMap(File("environ.png"))
    val scene = Scene(arrayListOf(
            Sphere(Vector3f(0.55f, -0.433f, -1.5f), 0.5f, Lambertian(Vector3f(0.2f, 0.2f, 0.9f))),
            Sphere(Vector3f(-0.55f, -0.433f, -1.5f), 0.5f, Metallic(Vector3f(0.8f, 0.6f, 0.2f), 0.3f)),
            Sphere(Vector3f(0f, 0.433f, -1.5f), 0.5f, Dielectric(Vector3f(0.9f, 0.2f, 0.2f), 0.3f, 0f)),
            Sphere(Vector3f(0f, 0f, -1f), 0.3f, Dielectric(Vector3f(1f), 0f, 1.5f))
    ), environ)
    
//    val spheres = arrayListOf<Sphere>()
//    for(i in 0 until 25) {
//        spheres.add(Sphere(Vector3f((i % 5).toFloat() - 2f, (i / 5).toFloat() - 2f, -3f), 0.4f, Metallic(Vector3f(1f), i.toFloat() / 24)))
//    }
//    val scene = Scene(spheres, environ)
    
    val camera = Camera(Vector3f(0f), Vector3f(0f, 0f, -1f), aspect)
    
    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    
    OpenCLRenderer
    val time = measureTimeMillis {
        OpenCLRenderer.render(scene, camera, image)
        OpenCLRenderer.cleanUp()
    }

    println("Time: $time")

    val file = File("render.png")
    ImageIO.write(image, "png", file)
    
}