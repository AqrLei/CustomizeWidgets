package com.aqrlei.roomlib.data.source.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aqrlei.roomlib.data.source.local.dao.UserDao
import com.aqrlei.roomlib.data.source.local.entity.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * created by AqrLei on 4/1/21
 */
@Database(entities = [User::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        private const val DATA_BASE_NAME = "app_data.db"
        @Volatile
        private var instance: AppDatabase? = null
        fun instance(context: Context, scope: CoroutineScope): AppDatabase {
            return instance ?: synchronized(this) {
                buildDatabase(context, scope).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context, scope: CoroutineScope) =
            Room.databaseBuilder(context, AppDatabase::class.java, DATA_BASE_NAME)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        //TODO
                    }
                })
                .build()
    }

    abstract fun userDao(): UserDao
}

