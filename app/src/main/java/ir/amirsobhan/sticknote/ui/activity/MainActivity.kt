package ir.amirsobhan.sticknote.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding
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