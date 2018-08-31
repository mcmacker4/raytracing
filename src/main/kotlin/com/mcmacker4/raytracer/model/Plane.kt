package com.mcmacker4.raytracer.model

import com.mcmacker4.raytracer.material.Material
import com.mcmacker4.raytracer.tracing.HitInfo
import com.mcmacker4.raytracer.tracing.Ray
import org.joml.Vector3f

class Plane(val pos: Vector3f, val normal: Vector3f, override val material: Material) : Solid {

    override fun hit(ray: Ray, hits: ArrayList<HitInfo>) {

        val nb = normal.dot(ray.dir)
        if(nb != 0f) {
            val na = normal.dot(ray.origin)
            val nc = normal.dot(pos)
            val t = (nc - na) / nb

            if(t > ray.tMin && t < ray.tMax) {
                val hitPos = ray.pointAt(t)
                hits.add(HitInfo(t, hitPos, Vector3f(normal), this))
            }
        }
        
    }
    
}