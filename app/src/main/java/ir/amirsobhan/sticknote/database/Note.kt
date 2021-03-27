package ir.amirsobhan.sticknote.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

@Entity
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    var title: String,
    var text: String?,
    var timestamp: Long
)
