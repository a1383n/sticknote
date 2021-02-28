package ir.amirsobhan.sticknote

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Html
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import ir.amirsobhan.sticknote.databinding.SplashActivityBinding
import ir.amirsobhan.sticknote.viewmodel.NoteViewModel

class SplashActivity : AppCompatActivity(){
    private lateinit var splashActivityBinding: SplashActivityBinding
    private lateinit var handler: Handler
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashActivityBinding = SplashActivityBinding.inflate(layoutInflater)
        setContentView(splashActivityBinding.root)

        //Colorful app name text
        val string : String = "<font color=${resources.getString(R.color.primary_text)}>StickMe</font>  <font color=#FA8704>Note</font>";
        splashActivityBinding.appName.text = Html.fromHtml(string)


        //Start MainActivity after 1.5 second
        val intent : Intent = Intent(this,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        handler = Handler()
        handler.postDelayed(Runnable {
            finish()
            startActivity(intent)
        },1500)

    }
}