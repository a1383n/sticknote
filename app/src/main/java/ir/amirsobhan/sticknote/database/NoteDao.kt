package ir.amirsobhan.sticknote.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {
    @Query("SELECT * FROM Note ORDER BY timestamp DESC")
    fun exportAll() : List<Note>

    @Query("SELECT * FROM Note ORDER BY id DESC")
    fun getAll(): LiveData<List<Note>>

    @Query("SELECT * FROM Note WHERE id = :id LIMIT 1")
    fun getByID(id : String) : Note

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(noteList: List<Note>)

    @Insert
    fun insert(note: Note)

    @Update
    fun update(note: Note)

    @Delete
    fun delete(note: Note)
}