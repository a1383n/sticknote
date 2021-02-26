package ir.amirsobhan.sticknote.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NoteDao {
    @Query("SELECT * FROM Note ORDER BY id DESC")
    fun getAll(): List<Note>

    @Insert
    fun insert(note: Note)
}