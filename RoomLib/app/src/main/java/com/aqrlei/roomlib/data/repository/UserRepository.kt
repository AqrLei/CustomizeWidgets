package com.aqrlei.roomlib.data.repository

import androidx.annotation.WorkerThread
import com.aqrlei.roomlib.data.source.local.dao.UserDao
import com.aqrlei.roomlib.data.source.local.entity.User
import kotlinx.coroutines.flow.Flow

/**
 * created by AqrLei on 4/1/21
 */
class UserRepository(private val userDao: UserDao) {

    val allUsers: Flow<List<User>> = userDao.getAll()

    @WorkerThread
    fun loadAllByCategory(category: String) = userDao.loadAllByCategory(category)

    @WorkerThread
    suspend fun insertAll(vararg users: User) {
        userDao.insertAll(*users)
    }

    @WorkerThread
    suspend fun delete(vararg user: User) {
        userDao.delete(*user)
    }

    @WorkerThread
    suspend fun update(vararg user: User) {
        userDao.update(*user)
    }

    suspend fun updateCategory(id: Int, category: String) {
        userDao.updateCategory(id, category)
    }

}