package com.example.memp.data.repository

import com.example.memp.data.local.entity.MemoEntity
import kotlinx.coroutines.flow.Flow

interface MemoRepository {
    fun getActiveMemos(): Flow<List<MemoEntity>>
    fun getFavoriteMemos(): Flow<List<MemoEntity>>
    fun searchMemos(query: String): Flow<List<MemoEntity>>
    suspend fun getMemoById(id: Long): MemoEntity?
    suspend fun insertMemo(memo: MemoEntity): Long
    suspend fun updateMemo(memo: MemoEntity)
    suspend fun softDeleteMemo(id: Long)
    suspend fun restoreMemo(id: Long)
    suspend fun hardDeleteMemo(id: Long)
}