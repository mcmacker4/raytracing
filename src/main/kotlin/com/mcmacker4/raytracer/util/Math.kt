package com.mcmacker4.raytracer.util

import org.joml.Vector3f
import org.joml.Vector3fc
import java.util.concurrent.ThreadLocalRandom


object CustomAcos {

    private val table = FloatArray(1024 * 1024)

    init {
        repeat(table.size) {
            val input = (it.toFloat() / table.size) * 2f - 1f
            table[it] = Math.acos(input.toDouble()).toFloat()
        }
    }

    fun acos(value: Float): Float = table[toIndex(value)]

    private fun toIndex(pre: Float): Int {
        val value = pre.coerceIn(-1f, 1f)
        val norm = (value + 1f) / 2f
        return (norm * table.size).toInt().coerceIn(0, table.size - 1)
    }
}

fun Vector3fc.angle2(v: Vector3fc): Float {
    var cos = angleCos(v)
    // This is because sometimes cos goes above 1 or below -1 because of lost precision
    cos = if (cos < 1) cos else 1.0f
    cos = if (cos > -1) cos else -1.0f
    return CustomAcos.acos(cos)
}

fun randf() = ThreadLocalRandom.current().nextFloat()