package ir.amirsobhan.sticknote.database

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ir.amirsobhan.sticknote.Constants
import ir.amirsobhan.sticknote.helper.Converters

@Database(entities = [Note::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(application: Application) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(application).also { instance = it }
        }

        private fun buildDatabase(application: Application) = Room.databaseBuilder(
            application,
            AppDatabase::class.java, Constants.APP_DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }
}