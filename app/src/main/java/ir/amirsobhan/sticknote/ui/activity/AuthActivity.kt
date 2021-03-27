package ir.amirsobhan.sticknote.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.databinding.ActivityAuthBinding
import ir.amirsobhan.sticknote.worker.FirstSync

class AuthActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAuthBinding
    private lateinit var navController : NavController

    override fun onCreate(savedInstanceState: Bundle?) {

        // TODO: Improve auth ui & handle FirebaseException

        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()


        navController.addOnDestinationChangedListener { controller, destination, arguments -> binding.textView3.text = destination.label }

        setSupportActionBar(binding.materialToolbar)
        setupActionBarWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onStop() {
        super.onStop()
        if(Firebase.auth.currentUser != null){
            WorkManager.getInstance(this)
                    .enqueue(OneTimeWorkRequestBuilder<FirstSync>().build())

            Toast.makeText(this,"Sync in progress ...", Toast.LENGTH_LONG).show()
        }
    }
}