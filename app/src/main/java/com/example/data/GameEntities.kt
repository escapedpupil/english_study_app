package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_records")
data class LevelRecord(
    @PrimaryKey val levelNumber: Int,
    val stars: Int = 0,
    val isUnlocked: Boolean = false,
    val completedAt: Long = 0L,
    val highScore: Int = 0
)

@Entity(tableName = "sticker_records")
data class StickerRecord(
    @PrimaryKey val stickerId: String,
    val title: String,
    val emoji: String,
    val unlockedAt: Long = System.currentTimeMillis()
)
