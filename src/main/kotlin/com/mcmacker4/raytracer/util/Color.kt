package com.mcmacker4.raytracer.util

import org.joml.Vector3f


fun asPixel(color: Vector3f) : Int {
    val alpha = 0xFF shl 24
    val red = (color.x * 255.99).toInt() and 0xFF shl 16
    val green = (color.y * 255.99).toInt() and 0xFF shl 8
    val blue = (color.z * 255.99).toInt() and 0xFF
    return alpha or red or green or blue
}

fun asVector(color: Int) : Vector3f {
    val red = (color and (0xFF shl 16)) shr (16)
    val green = (color and (0xFF shl 8)) shr (8)
    val blue = color and 0xFF
    return Vector3f(red / 256f, green / 256f, blue / 256f)
}