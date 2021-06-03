package ir.amirsobhan.sticknote.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.databinding.ActivityMainBinding
import ir.amirsobhan.sticknote.repositories.NoteRepository
import ir.amirsobhan.sticknote.ui.fragments.SettingFragmentDirections
import org.koin.android.ext.android.inject


class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding
    private val noteRepository: NoteRepository by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        setupBottomNavigation()

        activityMainBinding.floatingAction.setOnClickListener {
            startActivity(Intent(this, NoteActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
        NavigationUI.setupWithNavController(activityMainBinding.bottomNavigation, navHostFragment!!.navController)

        navHostFragment.navController.addOnDestinationChangedListener { _, destination, _ ->
            activityMainBinding.bottomBar.isVisible = destination.label != "ProfileFragment"
            activityMainBinding.floatingAction.isVisible = destination.label != "ProfileFragment"
        }

        // Add Corner to BottomBar
        var radius = resources.getDimension(R.dimen.bottom_nav_corner)
        val bottomBarBackground = activityMainBinding.bottomBar.background as MaterialShapeDrawable
        bottomBarBackground.shapeAppearanceModel = bottomBarBackground.shapeAppearanceModel
                .toBuilder()
                .setTopRightCorner(CornerFamily.ROUNDED, radius)
                .setTopLeftCorner(CornerFamily.ROUNDED, radius)
                .build()
    }
}