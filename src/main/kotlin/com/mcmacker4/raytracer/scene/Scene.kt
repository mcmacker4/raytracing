package com.mcmacker4.raytracer.scene

import com.mcmacker4.raytracer.model.Solid


data class Scene(val elements: List<Solid>, val environmentMap: EnvironmentMap? = null)