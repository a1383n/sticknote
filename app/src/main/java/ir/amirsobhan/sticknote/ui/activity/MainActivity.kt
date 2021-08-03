package ir.amirsobhan.sticknote.ui.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.work.WorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import ir.amirsobhan.sticknote.Constants
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.databinding.ActivityMainBinding
import ir.amirsobhan.sticknote.worker.AutoSync
import org.koin.android.ext.android.inject


class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding
    private val workManager : WorkManager by inject()
    private val auth = Firebase.auth
    private val sharedPreferences : SharedPreferences by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        setupBottomNavigation()

        // If user signIn listen for database change
        if (auth.currentUser != null){
            addSnapshotListener()
        }

        activityMainBinding.floatingAction.setOnClickListener {
            startActivity(Intent(this, NoteActivity::class.java))
        }

        handleAppUpdate()
    }

    private fun handleAppUpdate() {
        if (sharedPreferences.getBoolean(Constants.SharedPreferences.IS_APP_UPDATE,false)){
            val recentChanges = resources.getStringArray(R.array.recent_changes_list)

            MaterialAlertDialogBuilder(this,R.style.AlertDialogTheme)
                .setTitle(R.string.recent_changes)
                .setMessage(recentChanges.map { "- $it\n" }.joinToString(""))
                .setPositiveButton(R.string.ok){_,_ -> }
                .show()

            sharedPreferences.edit { putBoolean(Constants.SharedPreferences.IS_APP_UPDATE,false) }
        }
    }

    private fun setupBottomNavigation() {
        // Find NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
        NavigationUI.setupWithNavController(activityMainBinding.bottomNavigation, navHostFragment!!.navController)

        // Add listener to hide bottomBar and floatingAction when navigate to ProfileFragment
        navHostFragment.navController.addOnDestinationChangedListener { _, destination, _ ->
            activityMainBinding.bottomBar.isVisible = destination.label != getString(R.string.profile_fragment_label)
            activityMainBinding.floatingAction.isVisible = destination.label != getString(R.string.profile_fragment_label)
        }

        // Add Corner to BottomBar
        val radius = resources.getDimension(R.dimen.bottom_nav_corner)
        val bottomBarBackground = activityMainBinding.bottomBar.background as MaterialShapeDrawable
        bottomBarBackground.shapeAppearanceModel = bottomBarBackground.shapeAppearanceModel
                .toBuilder()
                .setTopRightCorner(CornerFamily.ROUNDED, radius)
                .setTopLeftCorner(CornerFamily.ROUNDED, radius)
                .build()
    }

    /**
     * This function listen for database change
     */
    private fun addSnapshotListener(){
       Firebase.firestore.document(Constants.CloudDatabase.getDocumentPath(auth.uid))
           .addSnapshotListener { value, error ->
               if (error != null) {
                   Firebase.crashlytics.recordException(error)
                   return@addSnapshotListener
               }

               if (value != null && value.exists()){
                   // When change detracted start AutoSync
                   workManager.enqueue(AutoSync.Factory(AutoSync.SYNC))
               }
           }
    }
}