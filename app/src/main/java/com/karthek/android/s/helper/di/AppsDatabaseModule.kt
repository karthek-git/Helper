package com.karthek.android.s.helper.di

import android.content.Context
import androidx.room.Room
import com.karthek.android.s.helper.state.db.AppDao
import com.karthek.android.s.helper.state.db.SAppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppsDatabaseModule {

    @Provides
    @Singleton
    fun provideSAppDatabase(@ApplicationContext context: Context): SAppDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            SAppDatabase::class.java,
            "s_apps"
        ).build()

    @Provides
    fun provideAppDao(database: SAppDatabase): AppDao = database.getAppDao()
}