package com.mcmacker4.raytracer.scene

import com.mcmacker4.raytracer.util.asVector
import org.joml.Vector3f
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.PI
import kotlin.math.abs

class EnvironmentMap(file: File) {
    
    private var image: BufferedImage = ImageIO.read(file)
    
    private val front = Vector3f(0f, 0f, -1f)
    
    fun getColor(dir: Vector3f): Vector3f {

        var horAngle = Vector3f(dir.x, 0f, dir.z).angle(front)
        if(dir.x < 0f) horAngle *= -1

        var verAngle = Vector3f(0f, dir.y, -abs(dir.z)).angle(front)
        if(dir.y < 0f) verAngle *= -1

        if(horAngle < -PI) horAngle += PI.toFloat()
        if(horAngle >= PI) horAngle -= PI.toFloat()
        if(verAngle < -PI) verAngle += PI.toFloat()
        if(verAngle >= PI) verAngle -= PI.toFloat()

        var x = Math.round(((horAngle / PI.toFloat()) * 0.5 + 0.5) * image.width).toInt()
        var y = Math.round(((verAngle / (PI.toFloat() / 2)) * 0.5 + 0.5) * image.height).toInt()
        
        //println("($x, $y) of (${image.width}, ${image.height})")
        
        if(x >= image.width) x = image.width - 1 
        if(y >= image.height) y = image.height - 1 
        
        return asVector(image.getRGB(x, image.height - y - 1))
        
    }
    
}
