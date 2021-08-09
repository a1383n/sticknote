package ir.amirsobhan.sticknote.helper

import ir.amirsobhan.sticknote.Constants
import ir.amirsobhan.sticknote.database.Note
import ir.amirsobhan.sticknote.database.decrypte
import ir.amirsobhan.sticknote.database.encrypte
import javax.crypto.BadPaddingException

enum class DecryptionResult{
    SUCCESSES,
    SECRET_NOT_FOUND,
    SECRET_INCORRECT,
    BAD_INPUT
}

enum class EncryptionResult{
    SUCCESSES,
    SECRET_NOT_FOUND,
    PROCESS_FAILED,
    BAD_INPUT
}

object EncryptionHelper{

    fun tryDecrypt(note: Note): Pair<DecryptionResult,Note?> {

        if (!note.isEncrypted){
            return Pair(DecryptionResult.BAD_INPUT,null)
        }else if (!Constants.Encryption.isReady()){
            return Pair(DecryptionResult.SECRET_NOT_FOUND,null)
        }

        return try {
            val note = note.decrypte()
            Pair(DecryptionResult.SUCCESSES,note)
        }catch (e : BadPaddingException){
            e.printStackTrace()
            Pair(DecryptionResult.SECRET_INCORRECT,null)
        }
    }

    fun tryEncrypt(note : Note): Pair<EncryptionResult, Note?> {
        if (!Constants.Encryption.isReady()){
            return Pair(EncryptionResult.SECRET_NOT_FOUND,null)
        }else if (note.isEncrypted){
            return Pair(EncryptionResult.BAD_INPUT,null)
        }

        return try {
            val note = note.encrypte()
            Pair(EncryptionResult.SUCCESSES,note)
        }catch (e : Exception){
            e.printStackTrace()
            Pair(EncryptionResult.PROCESS_FAILED,null)
        }
    }

    fun notReadyNote(note: Note) = Note(note.id,"This note was encrypted","To display this, enable 'Encrypt Notes' in settings",note.timestamp,false)

}