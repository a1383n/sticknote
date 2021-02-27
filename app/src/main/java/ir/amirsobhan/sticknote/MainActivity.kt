package ir.amirsobhan.sticknote

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
            startActivity(Intent(this, AddNoteActivity::class.java))
        }
    }

    private fun setupBottomNavigation(){
        activityMainBinding.viewPager.adapter = MainViewPagerAdapter(supportFragmentManager,lifecycle)
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
    }
}