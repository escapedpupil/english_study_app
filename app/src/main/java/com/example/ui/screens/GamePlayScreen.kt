package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.EnglishWord
import com.example.model.GameModeType
import com.example.ui.BubbleItem
import com.example.ui.FlipCardItem
import com.example.ui.GamePlayState
import com.example.ui.components.ConfettiEffect
import com.example.ui.components.KidSpeakerButton
import com.example.ui.components.MascotGuideView
import com.example.ui.components.StarRatingView
import com.example.ui.components.WordCardItem

@Composable
fun GamePlayScreen(
    state: GamePlayState,
    isSpeaking: Boolean,
    onBack: () -> Unit,
    onSpeakerClick: () -> Unit,
    onSelectOption: (EnglishWord) -> Unit,
    onPopBubble: (BubbleItem) -> Unit,
    onFlipCard: (Int) -> Unit,
    onNextLevel: () -> Unit,
    onRestartLevel: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val levelConfig = state.levelConfig
    val targetWord = state.targetWord

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE0F7FA), // Light cyan
                        Color(0xFFFFFDE7), // Soft sunny yellow
                        Color(0xFFF1F8E9)  // Soft lime
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Navigation & Progress Bar
            TopPlayNavBar(
                levelTitle = levelConfig.title,
                questionIndex = state.questionIndex,
                totalQuestions = state.totalQuestions,
                onBack = onBack,
                onRestart = onRestartLevel
            )

            // Mascot Guide with instruction bubble
            val instructionText = when (levelConfig.defaultMode) {
                GameModeType.LISTEN_AND_PICK -> "找一找: ${targetWord.english} (${targetWord.chinese}) 在哪里？"
                GameModeType.POP_BUBBLE -> "快戳破 ${targetWord.english} 泡泡！"
                GameModeType.SHADOW_GUESS -> "谁的影子是 ${targetWord.english} 呢？"
                GameModeType.CARD_FLIP -> "翻开卡片，找出所有相同的伙伴！"
            }

            MascotGuideView(
                bubbleText = instructionText,
                onSpeakerClick = onSpeakerClick,
                isSpeaking = isSpeaking
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Game Mode Arena
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                when (levelConfig.defaultMode) {
                    GameModeType.LISTEN_AND_PICK -> {
                        ListenAndPickArena(
                            options = state.options,
                            targetWord = targetWord,
                            isCorrect = state.isAnsweringCorrect,
                            onSelect = onSelectOption
                        )
                    }
                    GameModeType.POP_BUBBLE -> {
                        PopBubbleArena(
                            bubbles = state.bubbles,
                            targetWord = targetWord,
                            isCorrect = state.isAnsweringCorrect,
                            onPop = onPopBubble
                        )
                    }
                    GameModeType.SHADOW_GUESS -> {
                        ShadowGuessArena(
                            targetWord = targetWord,
                            options = state.options,
                            isCorrect = state.isAnsweringCorrect,
                            onSelect = onSelectOption
                        )
                    }
                    GameModeType.CARD_FLIP -> {
                        CardFlipArena(
                            cards = state.flipCards,
                            onFlip = onFlipCard
                        )
                    }
                }
            }

            // Bottom repeat speaker prompt bar
            BottomAudioHelperBar(
                targetWord = targetWord,
                isSpeaking = isSpeaking,
                onSpeakerClick = onSpeakerClick
            )
        }

        // Correct answer burst confetti
        if (state.isAnsweringCorrect) {
            ConfettiEffect(particleCount = 35)
        }

        // Level Victory Dialog
        if (state.isLevelFinished) {
            LevelVictoryDialog(
                stars = state.earnedStars,
                stickerName = levelConfig.stickerName,
                stickerEmoji = levelConfig.stickerEmoji,
                onNextLevel = onNextLevel,
                onReplay = onRestartLevel,
                onBackToMap = onBack
            )
        }
    }
}

@Composable
private fun TopPlayNavBar(
    levelTitle: String,
    questionIndex: Int,
    totalQuestions: Int,
    onBack: () -> Unit,
    onRestart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回地图",
                    tint = Color(0xFF37474F)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = levelTitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF263238)
                )
                Text(
                    text = "第 ${questionIndex + 1} / $totalQuestions 关",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800)
                )
            }

            IconButton(
                onClick = onRestart,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .testTag("restart_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "重新开始",
                    tint = Color(0xFF78909C)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Kid-friendly progress bar
        val progress = (questionIndex + 1).toFloat() / totalQuestions.toFloat()
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = Color(0xFFFF9800),
            trackColor = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun ListenAndPickArena(
    options: List<EnglishWord>,
    targetWord: EnglishWord,
    isCorrect: Boolean,
    onSelect: (EnglishWord) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(options, key = { it.id }) { word ->
            val isTarget = word.id == targetWord.id
            WordCardItem(
                word = word,
                isCorrectState = isCorrect && isTarget,
                onClick = { onSelect(word) }
            )
        }
    }
}

@Composable
private fun PopBubbleArena(
    bubbles: List<BubbleItem>,
    targetWord: EnglishWord,
    isCorrect: Boolean,
    onPop: (BubbleItem) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bubble_float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bubble_y_float"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.4f))
            .border(2.dp, Color(0xFF81D4FA), RoundedCornerShape(24.dp))
    ) {
        val maxWidthPx = constraints.maxWidth.toFloat()
        val maxHeightPx = constraints.maxHeight.toFloat()

        bubbles.forEachIndexed { index, bubble ->
            if (!bubble.isPopped) {
                val posX = (bubble.initialXRatio * maxWidthPx).toInt()
                val posY = ((bubble.initialYRatio * maxHeightPx) + if (index % 2 == 0) floatOffset else -floatOffset).toInt()

                Box(
                    modifier = Modifier
                        .offset { IntOffset(posX.coerceIn(0, (maxWidthPx - 260).toInt().coerceAtLeast(0)), posY.coerceIn(0, (maxHeightPx - 260).toInt().coerceAtLeast(0))) }
                        .size(110.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White,
                                    Color(bubble.word.accentColor).copy(alpha = 0.6f),
                                    Color(0xFF80D8FF).copy(alpha = 0.8f)
                                )
                            )
                        )
                        .border(3.dp, Color.White.copy(alpha = 0.9f), CircleShape)
                        .clickable { onPop(bubble) }
                        .testTag("bubble_${bubble.word.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = bubble.word.emoji, fontSize = 42.sp)
                        Text(
                            text = bubble.word.english,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A237E)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShadowGuessArena(
    targetWord: EnglishWord,
    options: List<EnglishWord>,
    isCorrect: Boolean,
    onSelect: (EnglishWord) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Spotlight Shadow Stage
        Card(
            modifier = Modifier
                .size(170.dp)
                .shadow(12.dp, RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isCorrect) Color(0xFFFFF9C4) else Color(0xFF263238)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (isCorrect) {
                    // Full revealed color with victory glow!
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = targetWord.emoji, fontSize = 72.sp)
                        Text(
                            text = targetWord.english,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF33691E)
                        )
                    }
                } else {
                    // Silhouette / Mystery shadow
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = targetWord.emoji,
                                fontSize = 72.sp,
                                modifier = Modifier.alpha(0.2f)
                            )
                            Text(
                                text = "❓",
                                fontSize = 48.sp
                            )
                        }
                        Text(
                            text = "神秘影子是谁呢？",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD54F)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3 Candidate options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            options.take(3).forEach { option ->
                Box(modifier = Modifier.weight(1f)) {
                    WordCardItem(
                        word = option,
                        isCorrectState = isCorrect && option.id == targetWord.id,
                        onClick = { onSelect(option) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CardFlipArena(
    cards: List<FlipCardItem>,
    onFlip: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(cards, key = { it.cardId }) { card ->
            Card(
                modifier = Modifier
                    .aspectRatio(0.85f)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(enabled = !card.isMatched) { onFlip(card.cardId) }
                    .testTag("flip_card_${card.cardId}"),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (card.isFaceUp || card.isMatched) Color.White else Color(0xFFFF9800)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (card.isFaceUp || card.isMatched) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = card.word.emoji, fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = card.word.english,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF263238)
                            )
                        }
                    } else {
                        // Card back pattern
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "⭐", fontSize = 34.sp)
                            Text(
                                text = "英语岛",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomAudioHelperBar(
    targetWord: EnglishWord,
    isSpeaking: Boolean,
    onSpeakerClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = targetWord.emoji,
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = targetWord.english,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF263238)
                )
                Text(
                    text = targetWord.soundHint,
                    fontSize = 13.sp,
                    color = Color(0xFF78909C),
                    fontWeight = FontWeight.Medium
                )
            }

            KidSpeakerButton(
                onClick = onSpeakerClick,
                isSpeaking = isSpeaking,
                textHint = "听发音"
            )
        }
    }
}

@Composable
private fun LevelVictoryDialog(
    stars: Int,
    stickerName: String,
    stickerEmoji: String,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onBackToMap: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Shiny Celebration Chest Image
                Image(
                    painter = painterResource(id = R.drawable.img_treasure_chest),
                    contentDescription = "宝箱奖励",
                    modifier = Modifier
                        .size(130.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "🎉 闯关成功！",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFF6F00)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Golden Stars
                StarRatingView(
                    stars = stars,
                    starSize = 36.dp,
                    animate = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // New Sticker Reward Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stickerEmoji, fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "解锁全新贴纸！",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF8F00)
                            )
                            Text(
                                text = stickerName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF37474F)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Primary Next Level Button
                Button(
                    onClick = onNextLevel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("victory_next_button"),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Text(
                        text = "🚀 继续挑战下一关！",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onReplay,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEEEEE))
                    ) {
                        Text(
                            text = "再玩一次",
                            color = Color(0xFF424242),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onBackToMap,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0F7FA))
                    ) {
                        Text(
                            text = "返回地图",
                            color = Color(0xFF00838F),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
