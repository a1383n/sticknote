package ir.amirsobhan.sticknote.helper

import androidx.room.TypeConverter
import com.google.firebase.Timestamp
import java.util.*

/**
 * Converters class for convert object Values can be stored in the database
 */
class Converters {
    /**
     * Convert Timestamp to Long
     */
    @TypeConverter
    fun fromTimestamp(timestamp: Timestamp?) : Long?{
        return timestamp?.toDate()?.time
    }

    /**
     * Convert Long to Timestamp
     */
    @TypeConverter
    fun fromLong(long : Long?) : Timestamp?{
        return long?.let { Timestamp(Date(long)) }
    }
}