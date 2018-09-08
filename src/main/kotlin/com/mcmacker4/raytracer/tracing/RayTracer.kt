package com.mcmacker4.raytracer.tracing

import com.mcmacker4.raytracer.scene.Scene
import com.mcmacker4.raytracer.util.*
import com.mcmacker4.raytracer.model.Solid
import org.joml.Vector3f
import org.joml.Vector3fc

data class HitInfo(
        val t: Float,
        val pos: Vector3fc,
        val normal: Vector3fc,
        val solid: Solid)

class Ray(
        val origin: Vector3fc,
        val dir: Vector3fc,
        val tMin: Float = 0.0001f,
        val tMax: Float = Float.MAX_VALUE) {
    
    fun pointAt(t: Float) =
            origin + dir * t

}

object RayTracer {
    
    const val MAX_BOUNCES = 50
    
    fun shoot(scene: Scene, ray: Ray, bounce: Int = 0): Vector3fc {
        
        val hits = arrayListOf<HitInfo>()
        for (solid in scene.elements) {
            solid.hit(ray, hits)
        }
        if (hits.size > 0) {
            if(bounce < MAX_BOUNCES) {
                val hit = hits.minBy { it.t }
                if (hit != null) {
                    val matRes = hit.solid.material.scatter(ray, hit)
                    return if(matRes != null) {
                        matRes.attenuation * shoot(scene, Ray(hit.pos, matRes.scatter), bounce + 1)
                    } else Vector3f(0f)
                }
            } else {
                return Vector3f(0f)
            }
        }

        return if(scene.environmentMap != null) {
            scene.environmentMap.getColor(ray.dir)
        } else {
            val t = ray.dir.y() * 0.5f + 0.5f
            Vector3f(1f, 1f, 1f) * (1 - t) + Vector3f(0.5f, 0.7f, 1.0f) * t
        }
        
    }
    
}
