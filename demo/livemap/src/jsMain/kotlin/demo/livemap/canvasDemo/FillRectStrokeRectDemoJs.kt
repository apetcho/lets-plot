/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package demo.livemap.canvasDemo

@Suppress("unused")
@JsName("fillRectStrokeRectDemo")
fun fillRectStrokeRectDemo() {
    demo.livemap.canvasDemo.baseCanvasDemo { canvas, _ ->
        demo.livemap.canvasDemo.FillRectStrokeRectDemoModel(canvas)
    }
}