package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.launch

@Composable
fun MascotGuideView(
    bubbleText: String,
    onSpeakerClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSpeaking: Boolean = false,
    onMascotClick: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val bounceAnim = remember { Animatable(1f) }
    val rotateAnim = remember { Animatable(0f) }

    fun triggerMascotWiggle() {
        coroutineScope.launch {
            bounceAnim.animateTo(1.15f, tween(120, easing = FastOutSlowInEasing))
            rotateAnim.animateTo(8f, tween(80))
            rotateAnim.animateTo(-8f, tween(80))
            rotateAnim.animateTo(0f, tween(80))
            bounceAnim.animateTo(1f, tween(100))
        }
        onMascotClick?.invoke()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mascot avatar with bouncing animation
        Box(
            modifier = Modifier
                .size(76.dp)
                .scale(bounceAnim.value)
                .rotate(rotateAnim.value)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .clickable { triggerMascotWiggle() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_mascot_guide),
                contentDescription = "萌萌小熊猫向导",
                modifier = Modifier.size(74.dp),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Speech Bubble with interactive speaker button
        Box(
            modifier = Modifier
                .weight(1f)
                .shadow(6.dp, RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(Color.White)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "小奇奇说：",
                        fontSize = 12.sp,
                        color = Color(0xFFFF9800),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = bubbleText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF37474F),
                        lineHeight = 22.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                KidSpeakerButton(
                    onClick = onSpeakerClick,
                    isSpeaking = isSpeaking,
                    textHint = null
                )
            }
        }
    }
}
