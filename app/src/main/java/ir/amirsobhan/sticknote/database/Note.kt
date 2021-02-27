package ir.amirsobhan.sticknote.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    var title: String,
    var text: String?,
    var timestamp: Long
)
