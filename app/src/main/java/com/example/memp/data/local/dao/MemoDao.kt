package com.example.memp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.memp.data.local.entity.MemoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoDao {
    // 활성 메모 최신순 및 고정(pinned) 우선 조회
    @Query("SELECT * FROM memos WHERE isDeleted = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getActiveMemos(): Flow<List<MemoEntity>>

    // 즐겨찾기 된 메모 조회
    @Query("SELECT * FROM memos WHERE isDeleted = 0 AND isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoriteMemos(): Flow<List<MemoEntity>>

    // 텍스트 기반 검색 (제목, 내용 포함)
    @Query("SELECT * FROM memos WHERE isDeleted = 0 AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') ORDER BY isPinned DESC, updatedAt DESC")
    fun searchMemos(query: String): Flow<List<MemoEntity>>
    
    @Query("SELECT * FROM memos WHERE id = :id")
    suspend fun getMemoById(id: Long): MemoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemo(memo: MemoEntity): Long

    @Update
    suspend fun updateMemo(memo: MemoEntity)

    // 휴지통용 소프트 딜리트
    @Query("UPDATE memos SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteMemo(id: Long, timestamp: Long = System.currentTimeMillis())

    // 휴지통에서 복구
    @Query("UPDATE memos SET isDeleted = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun restoreMemo(id: Long, timestamp: Long = System.currentTimeMillis())

    // 영구 삭제
    @Query("DELETE FROM memos WHERE id = :id")
    suspend fun hardDeleteMemo(id: Long)
}