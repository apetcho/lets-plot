/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.livemap.mapengine

import org.jetbrains.letsPlot.commons.geometry.DoubleVector
import org.jetbrains.letsPlot.commons.intern.typedGeometry.MultiPolygon
import org.jetbrains.letsPlot.commons.intern.typedGeometry.Rect
import org.jetbrains.letsPlot.commons.intern.typedGeometry.Vec
import org.jetbrains.letsPlot.core.canvas.Context2d
import org.jetbrains.letsPlot.livemap.Client
import org.jetbrains.letsPlot.livemap.ClientPoint

fun Context2d.scale(scale: DoubleVector) = scale(scale.x, scale.y)
fun Context2d.scale(scale: org.jetbrains.letsPlot.livemap.ClientPoint) = scale(scale.x, scale.y)

fun <T> Context2d.moveTo(p: Vec<T>) = moveTo(p.x, p.y)
fun <T> Context2d.lineTo(p: Vec<T>) = lineTo(p.x, p.y)
fun <T> Context2d.translate(p: Vec<T>) = translate(p.x, p.y)
fun Context2d.fillRect(r: Rect<org.jetbrains.letsPlot.livemap.Client>) = fillRect(r.origin.x, r.origin.y, r.dimension.x, r.dimension.y)
fun Context2d.strokeRect(r: Rect<org.jetbrains.letsPlot.livemap.Client>) = strokeRect(r.origin.x, r.origin.y, r.dimension.x, r.dimension.y)

fun <T> Context2d.drawMultiPolygon(geometry: MultiPolygon<T>, afterPolygon: (Context2d) -> Unit) {
    for (polygon in geometry) {
        for (ring in polygon) {
            ring[0].let(::moveTo)
            ring.drop(1).forEach(::lineTo)
        }
    }
    afterPolygon(this)
}
