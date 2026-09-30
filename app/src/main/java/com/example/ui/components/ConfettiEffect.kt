package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

data class Particle(
    val x: Float,
    val yStart: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val rotationSpeed: Float
)

@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier,
    particleCount: Int = 45
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
        )
    }

    val colors = listOf(
        Color(0xFFFF5252),
        Color(0xFFFFD700),
        Color(0xFF40C4FF),
        Color(0xFF69F0AE),
        Color(0xFFFF4081),
        Color(0xFFE040FB),
        Color(0xFFFFAB40)
    )

    val particles = remember {
        List(particleCount) {
            Particle(
                x = Random.nextFloat(),
                yStart = Random.nextFloat() * -0.3f,
                speed = 0.8f + Random.nextFloat() * 0.6f,
                size = 14f + Random.nextFloat() * 16f,
                color = colors.random(),
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        particles.forEach { particle ->
            val currentY = (particle.yStart + progress.value * particle.speed) * height
            val currentX = (particle.x * width) + (kotlin.math.sin(progress.value * 10f + particle.x * 20f) * 30f)
            val currentRotation = progress.value * particle.rotationSpeed

            if (currentY in 0f..height + 50f) {
                rotate(degrees = currentRotation, pivot = Offset(currentX, currentY)) {
                    drawRect(
                        color = particle.color,
                        topLeft = Offset(currentX, currentY),
                        size = Size(particle.size, particle.size * 0.6f)
                    )
                }
            }
        }
    }
}
