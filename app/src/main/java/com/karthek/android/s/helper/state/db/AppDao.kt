package com.karthek.android.s.helper.state.db

import androidx.room.*

@Dao
interface AppDao {
    @Query("SELECT * FROM app ")
    suspend fun getAll(): List<App>

    @Query("SELECT * FROM app  WHERE package_name IN (:ids)")
    fun loadAllByIds(ids: Array<String>): List<App>

    @Query("SELECT * FROM app  WHERE package_name LIKE :packageName LIMIT 1")
    fun findByName(packageName: String): App

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(app: App)

    @Insert
    suspend fun insertAll(apps: List<App>)

    @Delete
    suspend fun delete(app: App)
}