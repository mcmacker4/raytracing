package com.mcmacker4.raytracer.tracing

import com.mcmacker4.raytracer.scene.Scene
import com.mcmacker4.raytracer.util.*
import com.mcmacker4.raytracer.model.Solid
import org.joml.Vector3f

data class HitInfo(
        val t: Float,
        val pos: Vector3f,
        val normal: Vector3f,
        val solid: Solid)

class Ray(
        val origin: Vector3f,
        val dir: Vector3f,
        val tMin: Float = 0.0001f,
        val tMax: Float = Float.MAX_VALUE) {
    
    init {
        dir.normalize()
    }

    fun pointAt(t: Float) =
            origin + dir * t

}

object RayTracer {
    
    private fun randInUnitSphere() : Vector3f {
        var p: Vector3f
        do {
            p = Vector3f(randf(), randf(), randf()) * 2f - 1f
        } while(p.dot(p) >= 1f)
        return p
    }
    
    fun shoot(scene: Scene, ray: Ray): Vector3f {
        
        val hits = arrayListOf<HitInfo>()
        for (solid in scene.elements) {
            solid.hit(ray, hits)
        }
        if (hits.size > 0) {
            val hit = hits.minBy { it.t }
            if (hit != null) {
                val target = hit.normal + randInUnitSphere()
                return shoot(scene, Ray(hit.pos, target)) * hit.solid.color
            }
        }

        return if(scene.environmentMap != null) {
            scene.environmentMap.getColor(ray.dir)
        } else {
            val t = ray.dir.y * 0.5f + 1
            Vector3f(1f, 1f, 1f) * (1 - t) + Vector3f(0.5f, 0.7f, 1.0f) * t
        }
        
    }
    
}
