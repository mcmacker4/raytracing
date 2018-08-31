package com.mcmacker4.raytracer

import com.mcmacker4.raytracer.model.Plane
import com.mcmacker4.raytracer.model.Sphere
import com.mcmacker4.raytracer.scene.Camera
import com.mcmacker4.raytracer.scene.EnvironmentMap
import com.mcmacker4.raytracer.scene.Scene
import org.joml.Vector3f
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.system.measureTimeMillis


fun main(args: Array<String>) {

    val width = 400
    val height = 260
    val aspect = width.toFloat() / height

    val environ = EnvironmentMap(File("environ.jpg"))
    val scene = Scene(arrayListOf(
            Sphere(Vector3f(0.55f, 0f, -1f), 0.5f, Vector3f(0.2f, 0.2f, 0.9f)),
            Sphere(Vector3f(-0.55f, 0f, -1f), 0.5f, Vector3f(0.9f, 0.2f, 0.2f)),
            Plane(Vector3f(0f, -0.4f, 0f), Vector3f(0f, 1f, 0f), Vector3f(0.8f, 0.8f, 0.8f))
    ), environ)

    val camera = Camera(Vector3f(0f), Vector3f(0f, 0f, -1f), aspect)

    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val time = measureTimeMillis {
        ParallelRenderer.render(scene, camera, image)
    }

    println("Time: $time")

    val file = File("render.png")
    ImageIO.write(image, "png", file)
    
}