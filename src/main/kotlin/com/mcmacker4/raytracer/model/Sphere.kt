package com.mcmacker4.raytracer.model

import com.mcmacker4.raytracer.util.minus
import com.mcmacker4.raytracer.tracing.HitInfo
import com.mcmacker4.raytracer.tracing.Ray
import org.joml.Vector3f
import kotlin.math.sqrt


class Sphere(val pos: Vector3f, val radius: Float, override val color: Vector3f) : Solid {
    
    override fun hit(ray: Ray, hits: ArrayList<HitInfo>) {

        /**
         * Being:
         *      A: ray origin
         *      B: ray direction
         *      C: sphere position
         *      r: Sphere radius
         *      
         * Resolve for t:
         *      (t^2)(B^2) + 2tB(A-C) + (A-C)^2 - r^2 = 0
         */
        
        val oc = ray.origin - pos
        val a = ray.dir.dot(ray.dir)
        val b = 2f * oc.dot(ray.dir)
        val c = oc.dot(oc) - radius * radius
        val disc = b*b - 4*a*c
        
        val t1 = (-b - sqrt(disc)) / (2f*a)
        val t2 = (-b + sqrt(disc)) / (2f*a)
        
        for(t in arrayOf(t1, t2)) {
            if(t > ray.tMin && t < ray.tMax) {
                val hitPos = ray.pointAt(t)
                //Calculate normal
                val normal = ((hitPos - pos) / radius).normalize()
                //return hitinfo
                hits.add(HitInfo(t, hitPos, normal, this))
            }
        }

    }
    
}