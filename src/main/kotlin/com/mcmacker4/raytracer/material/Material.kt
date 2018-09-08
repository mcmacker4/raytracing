package com.mcmacker4.raytracer.material

import com.mcmacker4.raytracer.tracing.HitInfo
import com.mcmacker4.raytracer.tracing.Ray
import com.mcmacker4.raytracer.util.*
import org.joml.Vector3f
import org.joml.Vector3fc
import java.lang.Math.pow

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
        val reflected = (ray.dir.reflect(hit.normal, Vector3f()) + randRoughnessVec()).normalize()
        
        return if(reflected.dot(hit.normal) > 0)
            MatResult(reflected, albedo)
        else null
    }

}

class Dielectric(val albedo: Vector3fc, val roughness: Float, val refIndex: Float) : Material {
    
    fun shlick(cos: Float, refIndex: Float): Float {
        var r0 = (1 - refIndex) / (1 + refIndex)
        r0 *= r0
        return r0 + (1 - r0) * pow((1.0 - cos), 5.0).toFloat()
    }
    
    override fun scatter(ray: Ray, hit: HitInfo): MatResult? {
        
        val reflected = Vector3f(ray.dir).reflect(hit.normal)
        val attenuation = albedo
        
        val outNormal: Vector3f
        val niOverNt: Float
        val cosine: Float
        
        if(ray.dir.dot(hit.normal) > 0) {
            outNormal = -hit.normal
            niOverNt = refIndex
            cosine = refIndex * ray.dir.dot(hit.normal)
        } else {
            outNormal = Vector3f(hit.normal)
            niOverNt = 1 / refIndex
            cosine = -ray.dir.dot(hit.normal)
        }
        
        val reflectProb: Float
        
        val refracted = ray.dir.refract(outNormal, niOverNt)
        reflectProb = if(refracted != null) {
            shlick(cosine, refIndex)
        } else {
            1f
        }
        
        return if(randf() < reflectProb) {
            MatResult((reflected + randInUnitSphere() * roughness).normalize(), attenuation)
        } else {
            MatResult((refracted!! + randInUnitSphere() * roughness).normalize(), attenuation)
        }
        
    }

}