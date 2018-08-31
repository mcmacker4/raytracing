package com.mcmacker4.raytracer.material

import com.mcmacker4.raytracer.tracing.HitInfo
import com.mcmacker4.raytracer.tracing.Ray
import com.mcmacker4.raytracer.util.*
import org.joml.Vector3f
import org.joml.Vector3fc

data class MatResult(val scatter: Vector3fc, val attenuation: Vector3fc)

interface Material {
    
    fun scatter(ray: Ray, hit: HitInfo) : MatResult?
    
}

private fun randInUnitSphere() : Vector3fc {
    var p: Vector3fc
    do {
        p = Vector3f(randf(), randf(), randf()) * 2f - 1f
    } while(p.dot(p) >= 1f)
    return p
}

class Lambertian(val albedo: Vector3fc) : Material {

    
    
    override fun scatter(ray: Ray, hit: HitInfo): MatResult? {
        val scattered = (hit.pos + hit.normal + randInUnitSphere()).normalize()
        val attenuation = albedo
        return MatResult(scattered, attenuation)
    }

}

class Metallic(val albedo: Vector3fc, val roughness: Float) : Material {
    
    private fun randRoughnessVec() : Vector3f {
        return if(roughness > 0f)
            randInUnitSphere() * roughness
        else
            Vector3f(0f)
    }

    override fun scatter(ray: Ray, hit: HitInfo): MatResult? {
        val reflected = ray.dir.reflect(hit.normal, Vector3f()).normalize() + randRoughnessVec()
        
        return if(reflected.dot(hit.normal) > 0)
            MatResult(reflected, albedo)
        else null
    }

}