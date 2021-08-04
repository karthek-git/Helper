package com.karthek.android.s.helper.state

import androidx.annotation.WorkerThread
import com.karthek.android.s.helper.state.db.App
import com.karthek.android.s.helper.state.db.AppDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAccess @Inject constructor(private val AppDao: AppDao) {
    suspend fun sApps() = AppDao.getAll()

    @WorkerThread
    suspend fun insert(app: App) {
        AppDao.insert(app)
    }

    @WorkerThread
    suspend fun delete(app: App) {
        AppDao.delete(app)
    }

}