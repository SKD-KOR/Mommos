package com.example.memp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "checklist_items",
    foreignKeys = [
        ForeignKey(
            entity = MemoEntity::class,
            parentColumns = ["id"],
            childColumns = ["memoId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ChecklistItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memoId: Long,
    val content: String,
    val isCompleted: Boolean = false
)