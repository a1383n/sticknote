package ir.amirsobhan.sticknote.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ir.amirsobhan.sticknote.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAuthBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}