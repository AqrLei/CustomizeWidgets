package com.aqrlei.roomlib.data.source.local.dao

import androidx.room.*
import com.aqrlei.roomlib.data.source.local.entity.User
import kotlinx.coroutines.flow.Flow

/**
 * created by AqrLei on 4/1/21
 */
@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): Flow<List<User>>

    @Query("SELECT * FROM user WHERE category =:category")
    fun loadAllByCategory(category: String): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg users: User)

    @Delete
    suspend fun delete(vararg user: User)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(vararg user: User)

    @Query("UPDATE user SET category = :category WHERE uid = :id")
    suspend fun updateCategory(id: Int, category: String)
}
