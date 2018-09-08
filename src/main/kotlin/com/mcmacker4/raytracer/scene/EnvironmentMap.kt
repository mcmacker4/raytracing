package com.mcmacker4.raytracer.scene

import com.mcmacker4.raytracer.util.asVector
import org.joml.Vector3fc
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.*

class EnvironmentMap(file: File) {
    
    val image: BufferedImage = ImageIO.read(file)
    
    fun getColor(dir: Vector3fc): Vector3fc {
        
        val u = 0.5 + atan2(dir.z(), dir.x()) / (2*PI)
        val v = dir.y() * 0.5 + 0.5
        
        val iu = floor(u.coerceIn(0.0, 0.99999) * image.width).toInt()
        val iv = floor(v.coerceIn(0.0, 0.99999) * image.height).toInt()
        
        return asVector(image.getRGB(iu, image.height - iv - 1))
        
    }
    
}
