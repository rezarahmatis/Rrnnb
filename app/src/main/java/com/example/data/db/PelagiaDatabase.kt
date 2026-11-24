package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SavedAquariumEntity::class,
        TrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PelagiaDatabase : RoomDatabase() {
    abstract fun aquariumDao(): AquariumDao
    abstract fun musicDao(): MusicDao

    companion object {
        @Volatile
        private var INSTANCE: PelagiaDatabase? = null

        fun getInstance(context: Context): PelagiaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PelagiaDatabase::class.java,
                    "pelagia_sanctuary.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
