package com.karthek.android.s.helper.state.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [App::class], version = 1)
abstract class SAppDatabase : RoomDatabase() {
    abstract fun getAppDao(): AppDao
}