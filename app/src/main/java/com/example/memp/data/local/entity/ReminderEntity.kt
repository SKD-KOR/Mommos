package com.example.memp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = MemoEntity::class,
            parentColumns = ["id"],
            childColumns = ["memoId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memoId: Long,
    val reminderTime: Long,
    val repeatType: String,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)