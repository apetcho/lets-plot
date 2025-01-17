/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package demo.plot.common.model.plotConfig

import demoAndTestShared.parsePlotSpec
import demo.plot.common.data.AutoMpg
import kotlin.random.Random
import kotlin.random.nextInt

class FacetGridDemo {
    fun plotSpecList(): List<MutableMap<String, Any>> {
        return listOf(
            cols(),
            rows(),
            both(),
            bothFlipped(),
            both_YOrderingDesc(),
            numericFacetVariable(),
        )
    }

    private fun cols(): MutableMap<String, Any> {
        val plotSpec = commonSpecs()
        plotSpec["facet"] = mapOf(
            "name" to "grid",
            "x" to demo.plot.common.data.AutoMpg.cylinders.name,
            "x_format" to "{d} cyl"
        )

        plotSpec["coord"] = mapOf(
            "name" to "fixed",
            "xlim" to listOf(100, 150),
            "ylim" to listOf(0, 50),
        )

        plotSpec["ggtitle"] = mapOf("text" to "coord_fixed")

        return plotSpec
    }

    private fun rows(): MutableMap<String, Any> {
        val plotSpec = commonSpecs()
        plotSpec["facet"] = mapOf(
            "name" to "grid",
            "y" to demo.plot.common.data.AutoMpg.origin.name
        )
        return plotSpec
    }

    private fun both(): MutableMap<String, Any> {
        val plotSpec = commonSpecs()
        plotSpec["facet"] = mapOf(
            "name" to "grid",
            "x" to demo.plot.common.data.AutoMpg.cylinders.name,
            "y" to demo.plot.common.data.AutoMpg.origin.name,
            "x_format" to "{d} cyl"
        )
        return plotSpec
    }

    private fun bothFlipped(): MutableMap<String, Any> {
        val plotSpec = commonSpecs()
        plotSpec["facet"] = mapOf(
            "name" to "grid",
            "x" to demo.plot.common.data.AutoMpg.origin.name,
            "y" to demo.plot.common.data.AutoMpg.cylinders.name,
            "y_format" to "{d} cyl"
        )
        return plotSpec
    }

    @Suppress("FunctionName")
    private fun both_YOrderingDesc(): MutableMap<String, Any> {
        val plotSpec = commonSpecs()
        plotSpec["facet"] = mapOf(
            "name" to "grid",
            "x" to demo.plot.common.data.AutoMpg.cylinders.name,
            "y" to demo.plot.common.data.AutoMpg.origin.name,
            "y_order" to -1,
            "x_format" to "{d} cyl"
        )
        return plotSpec
    }

    private fun commonSpecs(): MutableMap<String, Any> {
        val spec = """
            {
                'kind': 'plot',
                'mapping': {
                    'x': "${demo.plot.common.data.AutoMpg.horsepower.name}",
                    'y': "${demo.plot.common.data.AutoMpg.mpg.name}",     
                    'color': "${demo.plot.common.data.AutoMpg.origin.name}"     
                },
                'layers': [
                    {
                        'geom': 'point'
                    }
                ],
                'theme': {'name': 'grey'}
            }
        """.trimIndent()

        val plotSpec = HashMap(parsePlotSpec(spec))
        plotSpec["data"] = demo.plot.common.data.AutoMpg.df
        return plotSpec
    }

    private fun numericFacetVariable(): MutableMap<String, Any> {
        val rnd = Random(0)
        val n = 100
        val x = (1..n).map() { rnd.nextDouble() }.joinToString { it.toString() }
        val c = (1..n).map { rnd.nextInt(1..6) }.joinToString { it.toString() }
        val spec = """
            |{
            |  "kind": "plot",
            |  "data": {
            |    "x": [$x],
            |    "c": [$c]
            |  },
            |  "mapping": {"x": "x"},
            |  "facet": {
            |    "name": "grid",
            |    "x": "c",
            |    "x_order": 1,
            |    "y_order": 1
            |  },
            |  "layers": [
            |    {
            |      "geom": "histogram",
            |      "tooltips": {
            |        "lines": ["@|@c"]
            |      }
            |    }
            |  ]
            |}            
        """.trimMargin()

        return parsePlotSpec(spec)
    }

}