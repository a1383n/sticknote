package ir.amirsobhan.sticknote.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import ir.amirsobhan.sticknote.R
import ir.amirsobhan.sticknote.adapters.MainViewPagerAdapter
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

    private fun setupBottomNavigation(){
        activityMainBinding.viewPager.adapter = MainViewPagerAdapter(
            supportFragmentManager,
            lifecycle
        )
        activityMainBinding.viewPager.currentItem = 2
        activityMainBinding.bottomNavigation.selectedItemId = R.id.notes
        activityMainBinding.viewPager.isUserInputEnabled = false;
        activityMainBinding.bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.setting -> activityMainBinding.viewPager.currentItem = 0
                R.id.cloud -> activityMainBinding.viewPager.currentItem = 1
                R.id.notes -> activityMainBinding.viewPager.currentItem = 2
                R.id.info -> activityMainBinding.viewPager.currentItem = 3
                else -> false
            }
            true
        }

        // Add Corner to BottomBar
        var radius = resources.getDimension(R.dimen.bottom_nav_corner)
        val bottomBarBackground = activityMainBinding.bottomBar.background as MaterialShapeDrawable
        bottomBarBackground.shapeAppearanceModel = bottomBarBackground.shapeAppearanceModel
            .toBuilder()
            .setTopRightCorner(CornerFamily.ROUNDED,radius)
            .setTopLeftCorner(CornerFamily.ROUNDED,radius)
            .build()

    }
}