package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM level_records ORDER BY levelNumber ASC")
    fun getAllLevels(): Flow<List<LevelRecord>>

    @Query("SELECT * FROM level_records WHERE levelNumber = :levelNumber")
    suspend fun getLevel(levelNumber: Int): LevelRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLevel(level: LevelRecord)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLevels(levels: List<LevelRecord>)

    @Query("SELECT * FROM sticker_records ORDER BY unlockedAt DESC")
    fun getAllStickers(): Flow<List<StickerRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSticker(sticker: StickerRecord)
}
