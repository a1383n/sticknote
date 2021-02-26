package ir.amirsobhan.sticknote.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Note(
    @PrimaryKey val id: Int,
    var title: String,
    var text: String?,
    val timestamp: Long
)
