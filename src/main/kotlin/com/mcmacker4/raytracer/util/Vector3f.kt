package com.mcmacker4.raytracer.util

import org.joml.Vector3f
import org.joml.Vector3fc
import kotlin.math.sqrt


operator fun Vector3fc.unaryMinus() =
        Vector3f(-x(), -y(), -z())

operator fun Vector3fc.plus(other: Vector3fc) =
        Vector3f(x() + other.x(), y() + other.y(), z() + other.z())

operator fun Vector3fc.plus(scalar: Float) =
        Vector3f(x() + scalar, y() + scalar, z() + scalar)

operator fun Vector3fc.minus(other: Vector3fc) =
        Vector3f(x() - other.x(), y() - other.y(), z() - other.z())

operator fun Vector3fc.minus(scalar: Float) =
        Vector3f(x() - scalar, y() - scalar, z() - scalar)

operator fun Vector3fc.times(other: Vector3fc) =
        Vector3f(x() * other.x(), y() * other.y(), z() * other.z())

operator fun Vector3fc.times(scale: Float) =
        Vector3f(x() * scale, y() * scale, z() * scale)

fun Vector3fc.refract(normal: Vector3fc, niOverNt: Float) : Vector3fc? {
    val dt = dot(normal)
    val discr = 1.0f - niOverNt*niOverNt*(1-dt*dt)
    return if(discr > 0) {
        Vector3f((this - normal * dt) * niOverNt - normal * sqrt(discr))
    } else null
}

