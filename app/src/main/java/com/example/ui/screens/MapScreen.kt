package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.LevelRecord
import com.example.data.StickerRecord
import com.example.model.LevelConfig
import com.example.model.LevelDefinitions
import com.example.ui.components.StarRatingView

@Composable
fun MapScreen(
    levelRecords: List<LevelRecord>,
    stickerRecords: List<StickerRecord>,
    onSelectLevel: (LevelConfig) -> Unit,
    onOpenStickerAlbum: () -> Unit,
    onVoiceGreeting: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalStars = levelRecords.sumOf { it.stars }
    val maxStars = LevelDefinitions.levels.size * 3

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_active_level")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "level_pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE1F5FE), // Soft sky blue
                        Color(0xFFFFF9C4), // Warm sunshine yellow
                        Color(0xFFE8F5E9)  // Island green
                    )
                )
            )
    ) {
        // Top Header Bar
        TopHeaderBar(
            totalStars = totalStars,
            maxStars = maxStars,
            stickerCount = stickerRecords.size,
            onOpenStickerAlbum = onOpenStickerAlbum,
            onVoiceGreeting = onVoiceGreeting
        )

        // Adventure map list with hero image header
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Adventure Banner
            item {
                HeroMapBanner()
            }

            // Map Section Title
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🗺️ 奇妙冒险关卡",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E88E5)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "闯关赢贴纸 ⭐",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800)
                    )
                }
            }

            // Stage cards
            itemsIndexed(LevelDefinitions.levels) { index, levelConfig ->
                val record = levelRecords.find { it.levelNumber == levelConfig.levelNumber }
                val isUnlocked = record?.isUnlocked ?: (index == 0)
                val stars = record?.stars ?: 0

                val isNextAvailable = isUnlocked && stars == 0

                StageNodeCard(
                    levelConfig = levelConfig,
                    isUnlocked = isUnlocked,
                    stars = stars,
                    isNextAvailable = isNextAvailable,
                    pulseScale = if (isNextAvailable) pulseAnim else 1f,
                    onClick = {
                        if (isUnlocked) {
                            onSelectLevel(levelConfig)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun TopHeaderBar(
    totalStars: Int,
    maxStars: Int,
    stickerCount: Int,
    onOpenStickerAlbum: () -> Unit,
    onVoiceGreeting: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App title & avatar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onVoiceGreeting() }
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF9E1B)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🐼", fontSize = 26.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "萌宝英语岛",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF263238)
                )
                Text(
                    text = "Magic Island",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Total Stars Badge
        Box(
            modifier = Modifier
                .shadow(4.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "星星",
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$totalStars",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFF9800)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Sticker Album Treasure Button
        Box(
            modifier = Modifier
                .shadow(4.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFFF7043), Color(0xFFFFAB40))
                    )
                )
                .clickable { onOpenStickerAlbum() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .testTag("sticker_album_button")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🎁", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "贴纸 ($stickerCount)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun HeroMapBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(160.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_island),
                contentDescription = "英语冒险岛全景",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x99000000)
                            )
                        )
                    )
            )

            // Cheerful banner slogan
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = "🌟 跟小奇奇一起听音闯关！",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "零基础轻松学单词 · 快乐听音不识字也能玩",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFFE082)
                )
            }
        }
    }
}

@Composable
private fun StageNodeCard(
    levelConfig: LevelConfig,
    isUnlocked: Boolean,
    stars: Int,
    isNextAvailable: Boolean,
    pulseScale: Float,
    onClick: () -> Unit
) {
    val themeColor = Color(levelConfig.themeColor)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .scale(pulseScale)
            .clip(RoundedCornerShape(24.dp))
            .border(
                width = if (isNextAvailable) 3.dp else 1.5.dp,
                color = if (isNextAvailable) Color(0xFFFF9800) else Color.White,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(enabled = isUnlocked, onClick = onClick)
            .testTag("stage_card_${levelConfig.levelNumber}"),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isUnlocked) 6.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color.White else Color(0xFFF5F5F5)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stage Number & Icon Badge
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isUnlocked) {
                            Brush.linearGradient(
                                listOf(themeColor, themeColor.copy(alpha = 0.7f))
                            )
                        } else {
                            Brush.linearGradient(
                                listOf(Color(0xFFBDBDBD), Color(0xFF9E9E9E))
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = levelConfig.iconEmoji,
                            fontSize = 28.sp
                        )
                        Text(
                            text = "第${levelConfig.levelNumber}关",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "已锁定",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Stage Information
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = levelConfig.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isUnlocked) Color(0xFF263238) else Color(0xFF9E9E9E)
                )

                Text(
                    text = levelConfig.subtitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isUnlocked) themeColor else Color(0xFFBDBDBD)
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (isUnlocked) {
                    // Stars achieved
                    StarRatingView(
                        stars = stars,
                        starSize = 20.dp
                    )
                } else {
                    Text(
                        text = "需完成前序关卡解锁 🔒",
                        fontSize = 12.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action button
            if (isUnlocked) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (isNextAvailable) Color(0xFFFF9800) else Color(0xFF4CAF50)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "开始闯关",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
