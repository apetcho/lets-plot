/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.commons.intern.spatial

import org.jetbrains.letsPlot.commons.intern.spatial.LongitudeSegment.Companion.splitSegment
import org.jetbrains.letsPlot.commons.intern.typedGeometry.*
import org.jetbrains.letsPlot.commons.interval.DoubleSpan
import kotlin.math.max
import kotlin.math.min

// Segment have direction, i.e. `start` can be less than `end` for the case
// of the antimeridian intersection.
// That's why we can't use ClosedRange class with lower <= upper invariant
typealias Segment = Pair<Double, Double>

val org.jetbrains.letsPlot.commons.intern.spatial.Segment.start get() = first
val org.jetbrains.letsPlot.commons.intern.spatial.Segment.end get() = second

class GeoBoundingBoxCalculator<TypeT>(
    private val myMapRect: Rect<TypeT>,
    private val myLoopX: Boolean,
    private val myLoopY: Boolean
) {
    fun calculateBoundingBox(xSegments: Sequence<org.jetbrains.letsPlot.commons.intern.spatial.Segment>, ySegments: Sequence<org.jetbrains.letsPlot.commons.intern.spatial.Segment>): Rect<TypeT> {
        val xRange = calculateBoundingRange(xSegments, myMapRect.xRange(), myLoopX)
        val yRange = calculateBoundingRange(ySegments, myMapRect.yRange(), myLoopY)
        return Rect.XYWH(xRange.lowerEnd, yRange.lowerEnd, xRange.length(), yRange.length())
    }

    private fun calculateBoundingRange(
        segments: Sequence<org.jetbrains.letsPlot.commons.intern.spatial.Segment>,
        mapRange: DoubleSpan,
        loop: Boolean
    ): DoubleSpan {
        return if (loop) {
            org.jetbrains.letsPlot.commons.intern.spatial.GeoBoundingBoxCalculator.Companion.calculateLoopLimitRange(
                segments,
                mapRange
            )
        } else {
            DoubleSpan(
                segments.map(org.jetbrains.letsPlot.commons.intern.spatial.Segment::start).minOrNull()!!,
                segments.map(org.jetbrains.letsPlot.commons.intern.spatial.Segment::end).maxOrNull()!!
            )
        }
    }

    companion object {
        internal fun calculateLoopLimitRange(segments: Sequence<org.jetbrains.letsPlot.commons.intern.spatial.Segment>, mapRange: DoubleSpan): DoubleSpan {
            return segments
                .map { splitSegment(it.start, it.end, mapRange.lowerEnd, mapRange.upperEnd) }
                .flatten()
                .run {
                    org.jetbrains.letsPlot.commons.intern.spatial.GeoBoundingBoxCalculator.Companion.findMaxGapBetweenRanges(
                        this,
                        mapRange.length()
                    )
                }
                .run {
                    org.jetbrains.letsPlot.commons.intern.spatial.GeoBoundingBoxCalculator.Companion.invertRange(
                        this,
                        mapRange.length()
                    )
                }
                .run {
                    org.jetbrains.letsPlot.commons.intern.spatial.GeoBoundingBoxCalculator.Companion.normalizeCenter(
                        this,
                        mapRange
                    )
                }
        }

        private fun normalizeCenter(range: DoubleSpan, mapRange: DoubleSpan): DoubleSpan {
            return if (mapRange.contains((range.upperEnd + range.lowerEnd) / 2)) {
                range
            } else {
                DoubleSpan(
                    range.lowerEnd - mapRange.length(),
                    range.upperEnd - mapRange.length()
                )
            }
        }

        private fun findMaxGapBetweenRanges(ranges: Sequence<DoubleSpan>, width: Double): DoubleSpan {
            val sortedRanges = ranges.sortedBy(DoubleSpan::lowerEnd)
            var prevUpper = sortedRanges.maxByOrNull(DoubleSpan::upperEnd)!!.upperEnd
            var nextLower = sortedRanges.first().lowerEnd
            val gapRight = max(width + nextLower, prevUpper)
            var maxGapRange = DoubleSpan(prevUpper, gapRight)

            val it = sortedRanges.iterator()
            prevUpper = it.next().upperEnd

            while (it.hasNext()) {
                val range = it.next()

                nextLower = range.lowerEnd
                if (nextLower > prevUpper && nextLower - prevUpper > maxGapRange.length()) {
                    maxGapRange = DoubleSpan(prevUpper, nextLower)
                }
                prevUpper = max(prevUpper, range.upperEnd)
            }
            return maxGapRange
        }

        private fun invertRange(range: DoubleSpan, width: Double): DoubleSpan {
            // Fix for rounding error for invertRange introduced by math with width.
            fun safeRange(first: Double, second: Double) = DoubleSpan(min(first, second), max(first, second))

            return when {
                range.length() > width -> DoubleSpan(range.lowerEnd, range.lowerEnd)
                range.upperEnd > width -> safeRange(range.upperEnd - width, range.lowerEnd)
                else -> safeRange(range.upperEnd, width + range.lowerEnd)
            }
        }

        private fun DoubleSpan.length(): Double = upperEnd - lowerEnd
    }
}

fun makeSegments(start: (Int) -> Double, end: (Int) -> Double, size: Int): Sequence<org.jetbrains.letsPlot.commons.intern.spatial.Segment> {
    return (0 until size).asSequence().map { org.jetbrains.letsPlot.commons.intern.spatial.Segment(start(it), end(it)) }
}

fun <T> org.jetbrains.letsPlot.commons.intern.spatial.GeoBoundingBoxCalculator<T>.geoRectsBBox(rectangles: List<org.jetbrains.letsPlot.commons.intern.spatial.GeoRectangle>): Rect<T>? {
    if (rectangles.isEmpty()) return null

    return calculateBoundingBox(
        org.jetbrains.letsPlot.commons.intern.spatial.makeSegments(
            { rectangles[it].startLongitude() },
            { rectangles[it].endLongitude() },
            rectangles.size
        ),
        org.jetbrains.letsPlot.commons.intern.spatial.makeSegments(
            { rectangles[it].minLatitude() },
            { rectangles[it].maxLatitude() },
            rectangles.size
        )
    )
}

fun <T> org.jetbrains.letsPlot.commons.intern.spatial.GeoBoundingBoxCalculator<T>.pointsBBox(xyCoords: List<Double>): Rect<T> {
    require(xyCoords.size % 2 == 0) { "Longitude-Latitude list is not even-numbered." }
    val x: (Int) -> Double = { index -> xyCoords[2 * index] }
    val y: (Int) -> Double = { index -> xyCoords[2 * index + 1] }

    val i = xyCoords.size / 2
    return calculateBoundingBox(
        org.jetbrains.letsPlot.commons.intern.spatial.makeSegments(x, x, i),
        org.jetbrains.letsPlot.commons.intern.spatial.makeSegments(y, y, i)
    )
}

fun <T> org.jetbrains.letsPlot.commons.intern.spatial.GeoBoundingBoxCalculator<T>.union(rectangles: List<Rect<T>>): Rect<T> {
    return calculateBoundingBox(
        org.jetbrains.letsPlot.commons.intern.spatial.makeSegments(
            { rectangles[it].left },
            { rectangles[it].right },
            rectangles.size
        ),
        org.jetbrains.letsPlot.commons.intern.spatial.makeSegments(
            { rectangles[it].top },
            { rectangles[it].bottom },
            rectangles.size
        )
    )
}
