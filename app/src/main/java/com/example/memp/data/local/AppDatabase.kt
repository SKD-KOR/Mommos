package com.example.memp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.memp.data.local.dao.MemoDao
import com.example.memp.data.local.entity.ChecklistItemEntity
import com.example.memp.data.local.entity.MemoEntity
import com.example.memp.data.local.entity.ReminderEntity

@Database(
    entities = [MemoEntity::class, ReminderEntity::class, ChecklistItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memoDao(): MemoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "memp_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}