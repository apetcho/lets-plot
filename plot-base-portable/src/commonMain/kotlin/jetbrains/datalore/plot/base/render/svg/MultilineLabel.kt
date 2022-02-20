/*
 * Copyright (c) 2022. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.base.render.svg

import jetbrains.datalore.base.observable.property.Property
import jetbrains.datalore.base.observable.property.WritableProperty
import jetbrains.datalore.base.values.Color
import jetbrains.datalore.plot.base.render.svg.Text.HorizontalAnchor
import jetbrains.datalore.plot.base.render.svg.Text.VerticalAnchor
import jetbrains.datalore.plot.base.render.svg.Text.toDY
import jetbrains.datalore.plot.base.render.svg.Text.toTextAnchor
import jetbrains.datalore.vis.svg.SvgConstants
import jetbrains.datalore.vis.svg.SvgTSpanElement
import jetbrains.datalore.vis.svg.SvgTextElement


class MultilineLabel(text: String) : SvgComponent() {
    private val myText = SvgTextElement()
    private var myTextColor: Color? = null
    private var myFontSize = 0.0
    private var myFontWeight: String? = null
    private var myFontFamily: String? = null
    private var myFontStyle: String? = null

    init {
        addTSpanElements(text.split('\n'))
        rootGroup.children().add(myText)
    }

    override fun buildComponent() {
    }

    fun textColor(): WritableProperty<Color?> {
        return object : WritableProperty<Color?> {
            override fun set(value: Color?) {
                // set attribute for svg->canvas mapping to work
                myText.fillColor()

                // duplicate in 'style' to override styles of container
                myTextColor = value
                updateStyleAttribute()
            }
        }
    }

    fun textOpacity(): WritableProperty<Double?> {
        return myText.fillOpacity()
    }

    fun x(): Property<Double?> {
        return myText.x()
    }

    fun y(): Property<Double?> {
        return myText.y()
    }

    fun setHorizontalAnchor(anchor: HorizontalAnchor) {
        myText.setAttribute(SvgConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, toTextAnchor(anchor))
    }

    fun setVerticalAnchor(anchor: VerticalAnchor) {
        // replace "dominant-baseline" with "dy" because "dominant-baseline" is not supported by Batik
        //    myText.setAttribute("dominant-baseline", toDominantBaseline(anchor));
        myText.setAttribute(SvgConstants.SVG_TEXT_DY_ATTRIBUTE, toDY(anchor))
    }

    fun setFontSize(px: Double) {
        myFontSize = px
        updateStyleAttribute()
    }

    /**
     * @param cssName : normal, bold, bolder, lighter
     */
    fun setFontWeight(cssName: String?) {
        myFontWeight = cssName
        updateStyleAttribute()
    }

    /**
     * @param cssName : normal, italic, oblique
     */
    fun setFontStyle(cssName: String?) {
        myFontStyle = cssName
        updateStyleAttribute()
    }

    /**
     * @param fontFamily : for example 'sans-serif' or 'Times New Roman'
     */
    fun setFontFamily(fontFamily: String?) {
        myFontFamily = fontFamily
        updateStyleAttribute()
    }

    private fun updateStyleAttribute() {
        val styleAttr = Text.buildStyle(
            myTextColor,
            myFontSize,
            myFontWeight,
            myFontFamily,
            myFontStyle
        )
        myText.setAttribute(SvgConstants.SVG_STYLE_ATTRIBUTE, styleAttr)
    }

    private fun addTSpanElements(lines: List<String>) {
        lines.forEach { line ->
            myText.addTSpan(line)
        }
    }

    fun setX(x: Double) {
        x().set(x)

        // set X for tspan elements
        myText.children()
            .filterIsInstance<SvgTSpanElement>()
            .forEach { tspan ->
                tspan.x().set(x)
            }
    }

    fun setLineVerticalMargin(margin: Double) {
        myText.children()
            .filterIsInstance<SvgTSpanElement>()
            .forEachIndexed { index, tspan ->
                val dyString = if (index == 0) {
                    "0.0"
                } else {
                    margin.toString()
                }
                tspan.textDy().set(dyString)
            }
    }

    fun linesCount(): Int {
        return myText.children().filterIsInstance<SvgTSpanElement>().size
    }
}