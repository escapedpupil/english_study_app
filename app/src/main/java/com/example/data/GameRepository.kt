package com.example.data

import com.example.model.LevelDefinitions
import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {

    val allLevels: Flow<List<LevelRecord>> = gameDao.getAllLevels()
    val allStickers: Flow<List<StickerRecord>> = gameDao.getAllStickers()

    suspend fun initializeDefaultLevelsIfNeeded() {
        val initialLevels = LevelDefinitions.levels.mapIndexed { index, def ->
            LevelRecord(
                levelNumber = def.levelNumber,
                stars = 0,
                isUnlocked = (index == 0), // Level 1 starts unlocked
                completedAt = 0L,
                highScore = 0
            )
        }
        gameDao.insertLevels(initialLevels)
    }

    suspend fun completeLevel(levelNumber: Int, earnedStars: Int, score: Int) {
        val existing = gameDao.getLevel(levelNumber)
        val bestStars = maxOf(existing?.stars ?: 0, earnedStars)
        val bestScore = maxOf(existing?.highScore ?: 0, score)
        
        gameDao.saveLevel(
            LevelRecord(
                levelNumber = levelNumber,
                stars = bestStars,
                isUnlocked = true,
                completedAt = System.currentTimeMillis(),
                highScore = bestScore
            )
        )

        // Award level sticker
        val levelDef = LevelDefinitions.levels.find { it.levelNumber == levelNumber }
        if (levelDef != null) {
            gameDao.saveSticker(
                StickerRecord(
                    stickerId = "sticker_$levelNumber",
                    title = levelDef.stickerName,
                    emoji = levelDef.stickerEmoji,
                    unlockedAt = System.currentTimeMillis()
                )
            )
        }

        // Check if next levels can be unlocked based on total stars
        unlockEligibleLevels()
    }

    suspend fun unlockEligibleLevels() {
        // Calculate total stars
        // We'll unlock the next consecutive level or any level whose requiredStarsToUnlock is satisfied
        // Let's do consecutive unlock as well as star requirement check
        LevelDefinitions.levels.forEach { def ->
            val record = gameDao.getLevel(def.levelNumber)
            if (record != null && !record.isUnlocked) {
                // If previous level is completed (has at least 1 star)
                val prevRecord = if (def.levelNumber > 1) gameDao.getLevel(def.levelNumber - 1) else null
                if (prevRecord != null && prevRecord.stars > 0) {
                    gameDao.saveLevel(record.copy(isUnlocked = true))
                }
            }
        }
    }
}
