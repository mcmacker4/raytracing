package com.mcmacker4.raytracer.renderer

import com.mcmacker4.raytracer.scene.Camera
import com.mcmacker4.raytracer.scene.Scene
import java.awt.image.BufferedImage


interface Renderer {

    fun render(scene: Scene, camera: Camera, result: BufferedImage)
    
}