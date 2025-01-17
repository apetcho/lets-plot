/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.core.plot.builder

import org.jetbrains.letsPlot.commons.event.MouseEventPeer
import org.jetbrains.letsPlot.commons.geometry.DoubleRectangle
import org.jetbrains.letsPlot.commons.geometry.DoubleVector
import org.jetbrains.letsPlot.commons.registration.CompositeRegistration
import org.jetbrains.letsPlot.commons.registration.Disposable
import org.jetbrains.letsPlot.commons.registration.Registration
import org.jetbrains.letsPlot.core.interact.*
import org.jetbrains.letsPlot.core.plot.base.PlotContext
import org.jetbrains.letsPlot.core.plot.base.theme.Theme
import org.jetbrains.letsPlot.core.plot.base.tooltip.GeomTargetLocator
import org.jetbrains.letsPlot.core.plot.builder.tooltip.HorizontalAxisTooltipPosition
import org.jetbrains.letsPlot.core.plot.builder.tooltip.TooltipRenderer
import org.jetbrains.letsPlot.core.plot.builder.tooltip.VerticalAxisTooltipPosition
import org.jetbrains.letsPlot.datamodel.svg.dom.SvgNode

internal class PlotInteractor constructor(
    val decorationLayer: SvgNode,
    mouseEventPeer: MouseEventPeer,
    val plotSize: DoubleVector,
    flippedAxis: Boolean,
    theme: Theme,
    plotContext: PlotContext
) : ToolInteractor, Disposable {
    val eventsManager: EventsManager = EventsManager()

    private val reg = CompositeRegistration()
    private val tooltipRenderer: TooltipRenderer

    private val geomBoundsList = ArrayList<DoubleRectangle>()

    init {
        reg.add(Registration.from(eventsManager))
        eventsManager.setEventSource(mouseEventPeer)

        tooltipRenderer = TooltipRenderer(
            decorationLayer,
            flippedAxis,
            plotSize,
            theme.horizontalAxis(flippedAxis),
            theme.verticalAxis(flippedAxis),
            theme.tooltips(),
            theme.plot().backgroundFill(),
            plotContext,
            mouseEventPeer
        )
        reg.add(Registration.from(tooltipRenderer))
    }

    fun onTileAdded(
        geomBounds: DoubleRectangle,
        targetLocators: List<GeomTargetLocator>,
        layerYOrientations: List<Boolean>,
        axisOrigin: DoubleVector,
        hAxisTooltipPosition: HorizontalAxisTooltipPosition,
        vAxisTooltipPosition: VerticalAxisTooltipPosition
    ) {
        tooltipRenderer.addTileInfo(
            geomBounds,
            targetLocators,
            layerYOrientations,
            axisOrigin,
            hAxisTooltipPosition,
            vAxisTooltipPosition
        )
        geomBoundsList.add(geomBounds)
    }

    override fun startToolFeedback(toolFeedback: ToolFeedback): Registration {
        val disposable: Disposable = when (toolFeedback) {
            is DragFeedback -> toolFeedback.start(
                DragInteractionContext(
                    decorationLayer,
                    eventsManager,
                    geomBoundsList
                )
            )

            else -> throw IllegalArgumentException("Unknown tool feedback type: ${toolFeedback::class.simpleName}")
        }
        return Registration.from(disposable)
    }


    override fun dispose() {
        reg.dispose()
    }

    private class DragInteractionContext(
        override val decorationsLayer: SvgNode,
        override val eventsManager: EventsManager,
        val geomBoundsList: List<DoubleRectangle>
    ) : InteractionContext {

        override fun findTarget(plotCoord: DoubleVector): InteractionTarget? {
            val geomBounds = geomBoundsList.find { it.contains(plotCoord) }
            return geomBounds?.let {
                object : InteractionTarget {
                    override val geomBounds: DoubleRectangle
                        get() = geomBounds

                    override fun zoom(geomBounds: DoubleRectangle) {
                        println("Target zoom: $geomBounds")
                    }
                }
            }
        }
    }
}
