package com.androvine.statussaver.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.androvine.statussaver.R
import com.androvine.statussaver.adapter.ViewPagerAdapter
import com.androvine.statussaver.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate((layoutInflater))
    }

    private val viewPagerAdapter: ViewPagerAdapter by lazy {
        ViewPagerAdapter(
            supportFragmentManager, lifecycle
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)


        setupBottomNav()
        binding.settings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun setupBottomNav() {

        binding.viewPager.adapter = viewPagerAdapter

        binding.bottomBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.n_status -> {
                    binding.viewPager.currentItem = 0
                    binding.toolbarTitle.text = getString(R.string.app_name)
                }

                R.id.n_downloads -> {
                    binding.viewPager.currentItem = 1
                    binding.toolbarTitle.text = "Downloads"
                }
            }
            true
        }

    }
}