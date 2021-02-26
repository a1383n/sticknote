package ir.amirsobhan.sticknote

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
    }

    private fun setupBottomNavigation(){
        activityMainBinding.viewPager.adapter = MainViewPagerAdapter(supportFragmentManager,lifecycle)
        activityMainBinding.bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.setting -> activityMainBinding.viewPager.setCurrentItem(0)
                R.id.cloud -> activityMainBinding.viewPager.setCurrentItem(1)
                R.id.notes -> activityMainBinding.viewPager.setCurrentItem(2)
                R.id.info -> activityMainBinding.viewPager.setCurrentItem(3)
                else -> false
            }

            true
        }
    }
}