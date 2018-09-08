package com.mcmacker4.raytracer.renderer

import com.mcmacker4.raytracer.scene.Camera
import com.mcmacker4.raytracer.scene.Scene
import com.mcmacker4.raytracer.tracing.RayTracer
import com.mcmacker4.raytracer.util.asPixel
import com.mcmacker4.raytracer.util.plus
import org.joml.Vector3f
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import kotlin.math.sqrt
import kotlin.streams.toList


object ParallelRenderer : Renderer {

    private data class Sector(val x: Int, val y: Int, val w: Int, val h: Int)

    private data class RenderedSector(val sector: Sector, val image: BufferedImage)

    const val SECTOR_W: Int = 30
    const val SECTOR_H: Int = 30
    
    const val RAYS_PER_PIXEL = 1000
    
    override fun render(scene: Scene, camera: Camera, result: BufferedImage) {
        
        val width = result.width
        val height = result.height
        
        val fullSector = Sector(0, 0, width, height)

        var nW = width / SECTOR_W
        var nH = height / SECTOR_H

        if(width % SECTOR_W > 0) nW += 1
        if(height % SECTOR_H > 0) nH += 1
        
        
        //Divide image in 20x20 sectorsarr
        val sectors = arrayListOf<Sector>()

        for(j in 0 until nH) {
            for (i in 0 until nW) {
                
                val secW = if(i == nW - 1 && width % SECTOR_W > 0) width % SECTOR_W else SECTOR_W
                val secH = if(j == nH - 1 && height % SECTOR_H > 0) height % SECTOR_H else SECTOR_H
                
                sectors.add(Sector(i * SECTOR_W, j * SECTOR_H, secW, secH))
                
            }
        }
        
        println("${sectors.size} sectors.")

        /**
         * Parallel render of 30x30 sectors
         */
        val renderedSectors = sectors.parallelStream().map {
            println("Rendering sector at: (${it.x}, ${it.y})")
            renderSector(it, scene, camera, fullSector)
        }.toList()
        
        for(renderedSec in renderedSectors) {
            
            val sec = renderedSec.sector
            val array = IntArray(sec.w * sec.h) 
            renderedSec.image.getRGB(0, 0, sec.w, sec.h, array, 0, sec.w)
            result.setRGB(sec.x, sec.y, sec.w, sec.h, array, 0, sec.w)
            
        }
        
    }
    
    private fun renderSector(sector: Sector, scene: Scene, camera: Camera, result: Sector) : RenderedSector {
        
        val image = BufferedImage(sector.w, sector.h, TYPE_INT_ARGB)

        for(j in 0 until sector.h) {
            for (i in 0 until sector.w) {
                var color = Vector3f(0f)
                for (n in 0 until RAYS_PER_PIXEL) {
                    val u = ((i + sector.x).toFloat() + Math.random().toFloat()) / result.w
                    val v = 1 - (((sector.h - j - 1) + sector.y).toFloat() + Math.random().toFloat()) / result.h
                    color += RayTracer.shoot(scene, camera.getRay(u, v))
                }
                color /= RAYS_PER_PIXEL.toFloat()
                color.set(sqrt(color.x), sqrt(color.y), sqrt(color.z))
                val pixel = asPixel(color)
                image.setRGB(i, sector.h - j - 1, pixel)
            }
        }
        
        return RenderedSector(sector, image)
        
    }
    
}