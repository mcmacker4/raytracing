package com.mcmacker4.raytracer.scene

import com.mcmacker4.raytracer.util.*
import com.mcmacker4.raytracer.tracing.Ray
import org.joml.Vector3f


class Camera(val pos: Vector3f, val lookAt: Vector3f, aspect: Float) {

    val corner = Vector3f(-1 * aspect, -1f, -1f)
    val hor = Vector3f(2f * aspect, 0f, 0f)
    val ver = Vector3f(0f, 2f, 0f)
    
    fun getRay(u: Float, v: Float) : Ray {
        return Ray(pos, corner + hor * u + ver * v)
    }
    
}