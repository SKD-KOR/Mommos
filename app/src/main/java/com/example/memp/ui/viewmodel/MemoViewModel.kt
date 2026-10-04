package com.example.memp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memp.data.local.entity.MemoEntity
import com.example.memp.data.repository.MemoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MemoViewModel(private val repository: MemoRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val memos: StateFlow<List<MemoEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getActiveMemos()
            } else {
                repository.searchMemos(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addMemo(
        title: String,
        content: String,
        category: String = "일반",
        onInserted: ((Long) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val id = repository.insertMemo(
                MemoEntity(
                    title = title,
                    content = content,
                    category = category
                )
            )
            onInserted?.invoke(id)
        }
    }

    fun updateMemo(memo: MemoEntity) {
        viewModelScope.launch {
            repository.updateMemo(memo.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun togglePin(memo: MemoEntity) {
        viewModelScope.launch {
            repository.updateMemo(memo.copy(isPinned = !memo.isPinned, updatedAt = System.currentTimeMillis()))
        }
    }

    fun toggleFavorite(memo: MemoEntity) {
        viewModelScope.launch {
            repository.updateMemo(memo.copy(isFavorite = !memo.isFavorite, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteMemo(id: Long) {
        viewModelScope.launch {
            repository.softDeleteMemo(id)
        }
    }
}