package com.example.memp.data.repository

import com.example.memp.data.local.dao.MemoDao
import com.example.memp.data.local.entity.MemoEntity
import kotlinx.coroutines.flow.Flow

class MemoRepositoryImpl(private val memoDao: MemoDao) : MemoRepository {
    override fun getActiveMemos(): Flow<List<MemoEntity>> = memoDao.getActiveMemos()
    override fun getFavoriteMemos(): Flow<List<MemoEntity>> = memoDao.getFavoriteMemos()
    override fun searchMemos(query: String): Flow<List<MemoEntity>> = memoDao.searchMemos(query)
    override suspend fun getMemoById(id: Long): MemoEntity? = memoDao.getMemoById(id)
    override suspend fun insertMemo(memo: MemoEntity): Long = memoDao.insertMemo(memo)
    override suspend fun updateMemo(memo: MemoEntity) = memoDao.updateMemo(memo)
    override suspend fun softDeleteMemo(id: Long) = memoDao.softDeleteMemo(id)
    override suspend fun restoreMemo(id: Long) = memoDao.restoreMemo(id)
    override suspend fun hardDeleteMemo(id: Long) = memoDao.hardDeleteMemo(id)
}