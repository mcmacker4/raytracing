package com.mcmacker4.raytracer.scene

import com.mcmacker4.raytracer.model.Solid


class Scene(val elements: ArrayList<Solid>, val environmentMap: EnvironmentMap? = null) {
    
    fun addElement(solid: Solid) {
        elements.add(solid)
    }
    
}