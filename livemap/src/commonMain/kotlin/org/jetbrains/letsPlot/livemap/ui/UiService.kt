/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.livemap.ui

import org.jetbrains.letsPlot.commons.registration.Registration
import org.jetbrains.letsPlot.commons.intern.typedGeometry.Vec
import org.jetbrains.letsPlot.core.canvas.Font
import org.jetbrains.letsPlot.livemap.Client
import org.jetbrains.letsPlot.livemap.core.animation.Animation
import org.jetbrains.letsPlot.livemap.core.ecs.*
import org.jetbrains.letsPlot.livemap.core.graphics.Button
import org.jetbrains.letsPlot.livemap.core.graphics.GraphicsService
import org.jetbrains.letsPlot.livemap.core.graphics.RenderBox
import org.jetbrains.letsPlot.livemap.core.graphics.TextMeasurer
import org.jetbrains.letsPlot.livemap.core.input.*
import org.jetbrains.letsPlot.livemap.core.layers.DirtyCanvasLayerComponent
import org.jetbrains.letsPlot.livemap.core.layers.ParentLayerComponent
import org.jetbrains.letsPlot.livemap.core.util.Geometries.inside
import org.jetbrains.letsPlot.livemap.ui.UiEntitiesRenderingSystem.UiLayerComponent

class UiService(
    private val myComponentManager: EcsComponentManager,
    private val textMeasurer: TextMeasurer
) : GraphicsService {
    override fun measure(text: String, font: Font) = textMeasurer.measure(text, font)

    override fun repaint() {
        val uiLayerEntityId = myComponentManager.getEntity(UiLayerComponent::class).id
        myComponentManager.getEntityById(uiLayerEntityId).tag(::DirtyCanvasLayerComponent)
    }

    override fun addToRenderer(obj: RenderBox) {
        if (!obj.attached) {
            obj.attach(this)
        }

        when (obj) {
            is Button -> addButton(obj)
            else -> addRenderable(obj)
        }
    }

    override fun onClick(renderBox: RenderBox, onClick: () -> Unit): Registration {
        val entity = myComponentManager
            .createEntity("ui_link")
            .addComponents {
                +ParentLayerComponent(myComponentManager.getEntity(UiLayerComponent::class).id)
                +CursorStyleComponent(CursorStyle.POINTER)
                +ClickableComponent(renderBox)
                +MouseInputComponent()
                +EventListenerComponent().apply {
                    addClickListener { onClick() }
                }
            }
        return createEntityCleanupRegistration(entity)
    }

    private fun findEntity(obj: RenderBox): EcsEntity? {
        myComponentManager.onEachEntity<UiRenderComponent> { entity, uiComponent ->
            if (uiComponent.renderBox === obj) {
                return entity
            }
        }

        return null
    }

    override fun removeFromRenderer(obj: RenderBox) {
        findEntity(obj)?.let(myComponentManager::removeEntity)
    }

    override fun setCursor(obj: RenderBox, cursorStyle: CursorStyle) {
        findEntity(obj)?.setComponent(CursorStyleComponent(cursorStyle))
    }

    override fun defaultCursor(obj: RenderBox) {
        findEntity(obj)?.remove<CursorStyleComponent>()
    }


    override fun addAnimation(animation: Animation): Registration {
        val animationEntity = myComponentManager
            .createEntity("animation_${animation.hashCode().toString(32)}")
            .addComponents {
                +AnimationObjectComponent(animation)
            }

        return createEntityCleanupRegistration(animationEntity)
    }


    private fun addRenderable(renderBox: RenderBox, name: String = "ui_renderable"): EcsEntity {
        return addParentLayerComponent(myComponentManager.createEntity(name), renderBox)
    }

    private fun addButton(button: Button): EcsEntity {
        return addParentLayerComponent(
            myComponentManager
                .createEntity(button.name)
                .addComponents {
                    + CursorStyleComponent(CursorStyle.POINTER)
                    + ClickableComponent(button)
                    + MouseInputComponent()
                    + EventListenerComponent().apply {
                        addClickListener(button::dispatchClick)
                        addDoubleClickListener(button::dispatchDoubleClick)
                    }
                },
            button
        )
    }

    private fun addParentLayerComponent(entity: EcsEntity, renderBox: RenderBox): EcsEntity {
        return entity
            .addComponents {
                + ParentLayerComponent(myComponentManager.getEntity(UiLayerComponent::class).id)
                + UiRenderComponent(renderBox)
            }
    }

    private fun createEntityCleanupRegistration(entity: EcsEntity): Registration {
        return object : Registration() {
            override fun doRemove() {
                myComponentManager.removeEntity(entity.id)
            }
        }
    }

    fun containsElementAtCoord(p: Vec<org.jetbrains.letsPlot.livemap.Client>): Boolean {
        myComponentManager.onEachEntity<UiRenderComponent> { _, uiElement ->
            if (inside(p.x, p.y, uiElement.origin, uiElement.dimension)) {
                return true
            }
        }
        return false
    }
}
