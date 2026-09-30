package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun StarRatingView(
    stars: Int,
    modifier: Modifier = Modifier,
    maxStars: Int = 3,
    starSize: Dp = 32.dp,
    animate: Boolean = false
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxStars) {
            val isEarned = i <= stars
            val scaleAnim = remember { Animatable(if (animate) 0f else 1f) }

            if (animate && isEarned) {
                LaunchedEffect(i) {
                    kotlinx.coroutines.delay((i - 1) * 200L)
                    scaleAnim.animateTo(
                        targetValue = 1.3f,
                        animationSpec = tween(250, easing = FastOutSlowInEasing)
                    )
                    scaleAnim.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(150, easing = FastOutSlowInEasing)
                    )
                }
            }

            if (isEarned) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "星星",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier
                        .size(starSize)
                        .scale(scaleAnim.value)
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = "未获得星星",
                    tint = Color(0xFFBDBDBD),
                    modifier = Modifier.size(starSize)
                )
            }
        }
    }
}
