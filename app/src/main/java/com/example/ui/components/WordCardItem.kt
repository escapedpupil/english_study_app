package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EnglishWord

@Composable
fun WordCardItem(
    word: EnglishWord,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCorrectState: Boolean = false,
    isWrongState: Boolean = false,
    isSilhouette: Boolean = false
) {
    val scaleAnim = remember { Animatable(1f) }
    val shakeAnim = remember { Animatable(0f) }

    if (isCorrectState) {
        LaunchedEffect(Unit) {
            scaleAnim.animateTo(1.15f, tween(150, easing = FastOutSlowInEasing))
            scaleAnim.animateTo(1f, tween(120, easing = FastOutSlowInEasing))
        }
    }

    if (isWrongState) {
        LaunchedEffect(isWrongState) {
            shakeAnim.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 350
                    -12f at 60
                    12f at 120
                    -8f at 180
                    8f at 240
                    0f at 300
                }
            )
        }
    }

    val baseColor = Color(word.accentColor)

    Card(
        modifier = modifier
            .aspectRatio(0.9f)
            .scale(scaleAnim.value)
            .clip(RoundedCornerShape(24.dp))
            .border(
                width = if (isCorrectState) 4.dp else 2.dp,
                color = if (isCorrectState) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.8f),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White),
                onClick = onClick
            )
            .testTag("word_card_${word.id}"),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            baseColor.copy(alpha = 0.25f),
                            baseColor.copy(alpha = 0.08f)
                        )
                    )
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Large visual emoji / silhouette
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSilhouette) {
                        // Silhouette presentation
                        Text(
                            text = word.emoji,
                            fontSize = 48.sp,
                            modifier = Modifier.scale(0.95f)
                        )
                    } else {
                        Text(
                            text = word.emoji,
                            fontSize = 50.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // English word title
                Text(
                    text = word.english,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF263238)
                )

                // Chinese subtitle for toddler connection
                Text(
                    text = word.chinese,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF78909C)
                )
            }
        }
    }
}
