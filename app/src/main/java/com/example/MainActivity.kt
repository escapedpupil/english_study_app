package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.GameViewModel
import com.example.ui.ScreenDestination
import com.example.ui.screens.GamePlayScreen
import com.example.ui.screens.MapScreen
import com.example.ui.screens.StickerAlbumScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                KidEnglishApp()
            }
        }
    }
}

@Composable
fun KidEnglishApp(
    viewModel: GameViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val levelRecords by viewModel.levelRecords.collectAsStateWithLifecycle()
    val stickerRecords by viewModel.stickerRecords.collectAsStateWithLifecycle()
    val gamePlayState by viewModel.gamePlayState.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.voicePlayer.isSpeaking.collectAsStateWithLifecycle()

    // Friendly greeting on first launch
    LaunchedEffect(Unit) {
        viewModel.voicePlayer.speakEnglishWord("Welcome to English Island!", "小朋友你好！欢迎来到萌宝英语岛！")
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        when (currentScreen) {
            ScreenDestination.MAP -> {
                MapScreen(
                    levelRecords = levelRecords,
                    stickerRecords = stickerRecords,
                    onSelectLevel = { levelConfig ->
                        viewModel.startLevel(levelConfig)
                    },
                    onOpenStickerAlbum = {
                        viewModel.navigateTo(ScreenDestination.STICKER_ALBUM)
                    },
                    onVoiceGreeting = {
                        viewModel.voicePlayer.speakEnglishWord("Let's Play!", "跟小奇奇一起闯关吧！")
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            ScreenDestination.GAMEPLAY -> {
                val state = gamePlayState
                if (state != null) {
                    GamePlayScreen(
                        state = state,
                        isSpeaking = isSpeaking,
                        onBack = {
                            viewModel.navigateTo(ScreenDestination.MAP)
                        },
                        onSpeakerClick = {
                            viewModel.speakCurrentQuestion()
                        },
                        onRepeatWordClick = {
                            viewModel.repeatTargetWord()
                        },
                        onSelectOption = { word ->
                            viewModel.selectOption(word)
                        },
                        onPopBubble = { bubble ->
                            viewModel.popBubble(bubble)
                        },
                        onFlipCard = { cardId ->
                            viewModel.flipCard(cardId)
                        },
                        onNextLevel = {
                            viewModel.playNextLevel()
                        },
                        onRestartLevel = {
                            viewModel.restartCurrentLevel()
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                } else {
                    // Fallback to Map if state is null
                    viewModel.navigateTo(ScreenDestination.MAP)
                }
            }

            ScreenDestination.STICKER_ALBUM -> {
                StickerAlbumScreen(
                    unlockedStickers = stickerRecords,
                    onBack = {
                        viewModel.navigateTo(ScreenDestination.MAP)
                    },
                    onStickerClick = { name, emoji ->
                        viewModel.voicePlayer.speakEnglishWord("You got a star!", "这是你获得的贴纸：$name $emoji！")
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
