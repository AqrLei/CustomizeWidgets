package com.aqrlei.roomlib

import android.app.Application
import com.aqrlei.roomlib.data.repository.UserRepository
import com.aqrlei.roomlib.data.source.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * created by AqrLei on 4/1/21
 */
class RoomApplication: Application() {
    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { AppDatabase.instance(this,applicationScope) }
    val repository by lazy { UserRepository(database.userDao()) }
}