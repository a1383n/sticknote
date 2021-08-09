package ir.amirsobhan.sticknote.database
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import ir.amirsobhan.sticknote.helper.AES
import java.util.*

@Entity
data class Note(
        @PrimaryKey val id: String = generateRandomId(),
        var title: String,
        var text: String?,
        var timestamp: Timestamp = Timestamp.now(),
        var isEncrypted : Boolean = false

){
    companion object{

        /**
         * Create Note object from firestore
         * @param hashMap The hashmap of firestore
         * @return Note object
         */
        fun fromHashMap(hashMap: HashMap<String,Objects>) : Note{
            return Note(
                hashMap["id"].toString(),
                hashMap["title"].toString(),
                hashMap["text"].toString(),
                hashMap["timestamp"] as Timestamp,
                hashMap["encrypted"] as Boolean
            )
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

/**
 * Generate random string
 * @param length The string length for generate
 */
fun grs(length: Int) : String{
    val allowedChars = ('a'..'z') + ('0'..'9')
    return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
}

/**
 * Generate random id for store note in database
 * @return The string id for note. Ex:ABCD1234-EF56-HI78-GK90-LMNOPQ123456
 */
fun generateRandomId() : String{
    return grs(8) + "-" + grs(4) + "-" + grs(4) + "-" + grs(4)  + "-" + grs(12)
}

fun Note.encrypte() = AES.encryptNote(this)
fun Note.decrypte() = AES.decryptNote(this)
