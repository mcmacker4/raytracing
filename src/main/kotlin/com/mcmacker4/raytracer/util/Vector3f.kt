package com.mcmacker4.raytracer.util

import org.joml.Vector3f


operator fun Vector3f.unaryMinus() =
        Vector3f(-x, -y, -z)

operator fun Vector3f.plus(other: Vector3f) =
        Vector3f(x + other.x, y + other.y, z + other.z)

operator fun Vector3f.plus(scalar: Float) =
        Vector3f(x + scalar, y + scalar, z + scalar)

operator fun Vector3f.minus(other: Vector3f) =
        Vector3f(x - other.x, y - other.y, z - other.z)

operator fun Vector3f.minus(scalar: Float) =
        Vector3f(x - scalar, y - scalar, z - scalar)

operator fun Vector3f.times(other: Vector3f) =
        Vector3f(x * other.x, y * other.y, z * other.z)

operator fun Vector3f.times(scale: Float) =
        Vector3f(x * scale, y * scale, z * scale)


fun randf() = Math.random().toFloat()