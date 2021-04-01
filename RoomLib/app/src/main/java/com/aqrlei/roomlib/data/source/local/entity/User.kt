package com.aqrlei.roomlib.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * created by AqrLei on 4/1/21
 */
@Entity
data class User(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "media_name") val mediaName: String? = "",
    @ColumnInfo(name = "artist_name") val artistName: String? = "",
    @ColumnInfo(name = "path") val path: String? = "",
    @ColumnInfo(name = "category") val category: String? = ""
)