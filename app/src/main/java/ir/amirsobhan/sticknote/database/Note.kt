package ir.amirsobhan.sticknote.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.common.hash.HashCode
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.grpc.KnownLength
import ir.amirsobhan.sticknote.diskIO
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.HashMap

@Entity
data class Note(
        @PrimaryKey val id: String = generateRandomId(),
        var title: String,
        var text: String?,
        var timestamp: Long = System.currentTimeMillis()
){
    companion object{
        fun fromHashMap(hashMap: HashMap<String,Objects>) : Note{
            return Note(hashMap["id"].toString(),hashMap["title"].toString(),hashMap["text"].toString(),hashMap["timestamp"].toString().toLong())
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Note

        if (id != other.id) return false
        if (title != other.title) return false
        if (text != other.text) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }


}

fun grs(length: Int) : String{
    val allowedChars = ('a'..'z') + ('0'..'9')
    return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
}

fun generateRandomId() : String{
    return grs(8) + "-" + grs(4) + "-" + grs(4) + "-" + grs(4)  + "-" + grs(12)
}
