package com.mcmacker4.raytracer.scene

import com.mcmacker4.raytracer.util.CustomAcos
import com.mcmacker4.raytracer.util.angle2
import com.mcmacker4.raytracer.util.asVector
import org.joml.Vector3f
import org.joml.Vector3fc
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.*

class EnvironmentMap(file: File) {
    
    private var image: BufferedImage = ImageIO.read(file)
    
    private val front = Vector3f(0f, 0f, -1f)
    
    fun getColor(dir: Vector3fc): Vector3fc {
        
        val u = 0.5 + atan2(dir.z(), dir.x()) / (2*PI)
        val v = dir.y() * 0.5 + 0.5
        
        val iu = floor(u.coerceIn(0.0, 0.99999) * image.width).toInt()
        val iv = floor(v.coerceIn(0.0, 0.99999) * image.height).toInt()
        
        return asVector(image.getRGB(iu, image.height - iv - 1))
        
    }
    
}
