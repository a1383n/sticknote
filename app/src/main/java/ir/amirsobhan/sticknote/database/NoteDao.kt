package ir.amirsobhan.sticknote.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {
    /**
     * Export all notes as List<Note> order by descending
     */
    @Query("SELECT * FROM Note ORDER BY timestamp DESC")
    fun exportAll() : List<Note>

    /**
     * Get all notes as LiveDate<List<Note>> order by descending
     */
    @Query("SELECT * FROM Note ORDER BY id DESC")
    fun getAll(): LiveData<List<Note>>

    /**
     * Get single note by id
     */
    @Query("SELECT * FROM Note WHERE id = :id LIMIT 1")
    fun getByID(id : String) : Note

    /**
     * Insert all notes with replace strategy
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(noteList: List<Note>)

    /**
     * Delete all notes in table
     */
    @Query("DELETE FROM Note")
    fun deleteAll()

    @Insert
    fun insert(note: Note)

    @Update
    fun update(note: Note)

    @Delete
    fun delete(note: Note)
}