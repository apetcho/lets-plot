/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.core.plot.base.aes

import org.jetbrains.letsPlot.core.commons.typedKey.TypedKeyHashMap
import org.jetbrains.letsPlot.commons.values.Color
import org.jetbrains.letsPlot.core.plot.base.Aes
import org.jetbrains.letsPlot.core.plot.base.GeomKind
import org.jetbrains.letsPlot.core.plot.base.render.point.NamedShape

open class AestheticsDefaults(geomTheme: GeomTheme) {

    private val myDefaults = TypedKeyHashMap().apply {
        for (aes in org.jetbrains.letsPlot.core.plot.base.Aes.values()) {
            // Safe cast because AesInitValue.get(aes) is guaranteed to return correct type.
            @Suppress("UNCHECKED_CAST")
            put(aes as org.jetbrains.letsPlot.core.plot.base.Aes<Any>, AesInitValue[aes])
        }
        // defaults from geom theme:
        put(org.jetbrains.letsPlot.core.plot.base.Aes.COLOR, geomTheme.color())
        put(org.jetbrains.letsPlot.core.plot.base.Aes.FILL, geomTheme.fill())
        put(org.jetbrains.letsPlot.core.plot.base.Aes.ALPHA, geomTheme.alpha())
        put(org.jetbrains.letsPlot.core.plot.base.Aes.SIZE, geomTheme.size())
        put(org.jetbrains.letsPlot.core.plot.base.Aes.LINEWIDTH, geomTheme.lineWidth())
        put(org.jetbrains.letsPlot.core.plot.base.Aes.STROKE, geomTheme.lineWidth())
    }
    private val myDefaultsInLegend = TypedKeyHashMap().apply {
        put(org.jetbrains.letsPlot.core.plot.base.Aes.ALPHA, 1.0)
    }

    private fun <T> update(aes: org.jetbrains.letsPlot.core.plot.base.Aes<T>, defaultValue: T): AestheticsDefaults {
        myDefaults.put(aes, defaultValue)
        return this
    }

    private fun <T> updateInLegend(aes: org.jetbrains.letsPlot.core.plot.base.Aes<T>, defaultValue: T): AestheticsDefaults {
        myDefaultsInLegend.put(aes, defaultValue)
        return this
    }

    fun <T> defaultValue(aes: org.jetbrains.letsPlot.core.plot.base.Aes<T>): T {
        return myDefaults[aes]
    }

    fun <T> defaultValueInLegend(aes: org.jetbrains.letsPlot.core.plot.base.Aes<T>): T {
        return if (myDefaultsInLegend.containsKey(aes)) {
            myDefaultsInLegend[aes]
        } else {
            defaultValue(aes)
        }
    }

    companion object {
        private fun point(geomTheme: GeomTheme): AestheticsDefaults {
            return base(geomTheme)
                .updateInLegend(org.jetbrains.letsPlot.core.plot.base.Aes.SIZE, 5.0)
        }

        private fun bar(geomTheme: GeomTheme): AestheticsDefaults {
            return base(geomTheme)
                .update(org.jetbrains.letsPlot.core.plot.base.Aes.WIDTH, 0.9)
        }

        private fun dotplot(geomTheme: GeomTheme): AestheticsDefaults {
            return base(geomTheme)
                .updateInLegend(org.jetbrains.letsPlot.core.plot.base.Aes.SIZE, 5.0)
        }

        private fun errorBar(geomTheme: GeomTheme): AestheticsDefaults {
            return base(geomTheme)
                .update(org.jetbrains.letsPlot.core.plot.base.Aes.WIDTH, 0.45)
                .update(org.jetbrains.letsPlot.core.plot.base.Aes.HEIGHT, 0.45)
        }

        private fun crossBar(geomTheme: GeomTheme): AestheticsDefaults {
            return base(geomTheme)
                .update(org.jetbrains.letsPlot.core.plot.base.Aes.WIDTH, 0.9)
        }

        private fun boxplot(geomTheme: GeomTheme): AestheticsDefaults {
            return crossBar(geomTheme)
        }

        private fun text(geomTheme: GeomTheme): AestheticsDefaults {
            return base(geomTheme)
                .updateInLegend(org.jetbrains.letsPlot.core.plot.base.Aes.FILL, Color.TRANSPARENT)
        }

        private fun pie(geomTheme: GeomTheme): AestheticsDefaults {
            return base(geomTheme)
                .updateInLegend(org.jetbrains.letsPlot.core.plot.base.Aes.SIZE, 1.0)
                .updateInLegend(org.jetbrains.letsPlot.core.plot.base.Aes.FILL, Color.TRANSPARENT)
                .updateInLegend(org.jetbrains.letsPlot.core.plot.base.Aes.COLOR, Color.TRANSPARENT)
        }

        private fun lollipop(geomTheme: GeomTheme): AestheticsDefaults {
            return point(geomTheme)
                .update(org.jetbrains.letsPlot.core.plot.base.Aes.SHAPE, NamedShape.STICK_CIRCLE)
        }

        private fun base(geomTheme: GeomTheme): AestheticsDefaults {
            return AestheticsDefaults(geomTheme)
        }

        fun create(geomKind: GeomKind, geomTheme: GeomTheme): AestheticsDefaults {
            return when (geomKind) {
                GeomKind.POINT,
                GeomKind.JITTER,
                GeomKind.Q_Q,
                GeomKind.Q_Q_2 -> point(geomTheme)

                GeomKind.BAR -> bar(geomTheme)

                GeomKind.DOT_PLOT,
                GeomKind.Y_DOT_PLOT -> dotplot(geomTheme)

                GeomKind.ERROR_BAR -> errorBar(geomTheme)

                GeomKind.CROSS_BAR -> crossBar(geomTheme)

                GeomKind.BOX_PLOT -> boxplot(geomTheme)

                GeomKind.TEXT,
                GeomKind.LABEL -> text(geomTheme)

                GeomKind.PIE -> pie(geomTheme)

                GeomKind.LOLLIPOP -> lollipop(geomTheme)

                GeomKind.PATH,
                GeomKind.LINE,
                GeomKind.SMOOTH,
                GeomKind.HISTOGRAM,
                GeomKind.TILE,
                GeomKind.BIN_2D,
                GeomKind.MAP,
                GeomKind.LINE_RANGE,
                GeomKind.POINT_RANGE,
                GeomKind.POLYGON,
                GeomKind.AB_LINE,
                GeomKind.H_LINE,
                GeomKind.V_LINE,
                GeomKind.AREA_RIDGES,
                GeomKind.VIOLIN,
                GeomKind.RIBBON,
                GeomKind.AREA,
                GeomKind.DENSITY,
                GeomKind.CONTOUR,
                GeomKind.CONTOURF,
                GeomKind.DENSITY2D,
                GeomKind.DENSITY2DF,
                GeomKind.Q_Q_LINE,
                GeomKind.Q_Q_2_LINE,
                GeomKind.FREQPOLY,
                GeomKind.RECT,
                GeomKind.SEGMENT,
                GeomKind.STEP,
                GeomKind.RASTER,
                GeomKind.IMAGE,
                GeomKind.LIVE_MAP -> base(geomTheme)
            }
        }
    }
}