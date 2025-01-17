/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package demo.plot.batik.plotConfig

import demo.plot.common.model.plotConfig.Path
import demo.common.batik.demoUtils.PlotSpecsDemoWindowBatik

fun main() {
    with(Path()) {
        PlotSpecsDemoWindowBatik(
            "Path",
            plotSpecList()
        ).open()
    }
}