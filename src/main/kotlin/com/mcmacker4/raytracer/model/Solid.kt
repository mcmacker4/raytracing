package com.mcmacker4.raytracer.model

import com.mcmacker4.raytracer.material.Material
import com.mcmacker4.raytracer.tracing.HitInfo
import com.mcmacker4.raytracer.tracing.Ray
import org.joml.Vector3f
import java.util.*


interface Solid {
    
    val material: Material

    fun hit(ray: Ray, hits: ArrayList<HitInfo>)
    
}