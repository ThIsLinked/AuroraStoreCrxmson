package com.aurora.store.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aurora.store.data.room.download.Download
import com.aurora.store.data.room.download.DownloadConverter
import com.aurora.store.data.room.download.DownloadDao
import com.aurora.store.data.room.favourites.Favourite
import com.aurora.store.data.room.favourites.FavouriteDao
import com.aurora.store.data.room.update.Update
import com.aurora.store.data.room.update.UpdateDao

@Database(
    entities = [Download::class, Favourite::class, Update::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(DownloadConverter::class)
abstract class AuroraDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
    abstract fun favouriteDao(): FavouriteDao
    abstract fun updateDao(): UpdateDao
}
