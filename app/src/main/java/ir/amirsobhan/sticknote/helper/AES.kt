package ir.amirsobhan.sticknote.helper

import android.content.SharedPreferences
import android.util.Base64
import androidx.core.content.edit
import ir.amirsobhan.sticknote.Constants
import ir.amirsobhan.sticknote.database.Note
import ir.amirsobhan.sticknote.database.encrypte
import ir.amirsobhan.sticknote.repositories.NoteRepository
import org.koin.java.KoinJavaComponent.inject
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object AES {
    private var AES_ALGORITHM = Constants.Encryption.ALGORITHM
    private var AES_TRANSFORMATION = Constants.Encryption.TRANSFORMATION
    private var secretKey: SecretKeySpec? = null

    private fun setKey(): SecretKeySpec? {
        val sharedPreferences : SharedPreferences by inject(SharedPreferences::class.java)
        val myKey = sharedPreferences.getString("secret",null)
            ?: throw NullPointerException("SecretKey is null")

        try {
            var key = myKey.toByteArray(charset("UTF-8"))
            key = key.copyOf(16)
            secretKey = SecretKeySpec(key, AES_ALGORITHM)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        return secretKey
    }

    private fun encrypt(strToEncrypt: String): String {
        val cipher: Cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, setKey())
        val inputBytes: ByteArray = cipher.doFinal(strToEncrypt.toByteArray())
        return Base64.encodeToString(inputBytes, 0)
    }

    private fun decrypt(strToDecrypt: String?): String {
        val cipher: Cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, setKey())
        val inputBytes: ByteArray = cipher.doFinal(Base64.decode(strToDecrypt, 0))
        return String(inputBytes)
    }

    fun buildSecret(string: String){
        val sharedPreferences : SharedPreferences by inject(SharedPreferences::class.java)
        val repository : NoteRepository by inject(NoteRepository::class.java)

        sharedPreferences.edit { putString("secret", calculateHash(string)).also { putBoolean(Constants.SharedPreferences.ENCRYPTION_READY,true) } }
        repository.insertAll(repository.exportAll().map { it.encrypte() })
    }

    fun decryptNote(note: Note): Note {
        note.apply {
            title = decrypt(title)
            text = if (text != null) decrypt(text) else null
            isEncrypted = false
        }

        return note
    }

    fun encryptNote(note: Note): Note {
        note.apply {
            title = encrypt(title)
            text = if (text != null) encrypt(text!!) else null
            isEncrypted = true
        }

        return note
    }

    private fun calculateHash(input: String): String {
        val digest = MessageDigest.getInstance("SHA-1")
        val result = digest.digest(input.toByteArray(Charsets.UTF_8))
        val sb = StringBuilder()
        for (b in result) {
            sb.append(String.format("%02X", b))
        }
        return sb.toString()
    }

    private fun buildHash(input: String): ByteArray {
        val digest = MessageDigest.getInstance("SHA-1")
        digest.reset()

        return digest.digest(input.toByteArray())
    }
}