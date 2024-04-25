package com.mrapps.statussaver.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mrapps.statussaver.R
import com.mrapps.statussaver.adapter.ViewPagerAdapterDC
import com.mrapps.statussaver.databinding.ActivityDirectChatBinding

class DirectChat : AppCompatActivity() {

    private val binding: ActivityDirectChatBinding by lazy {
        ActivityDirectChatBinding.inflate(layoutInflater)
    }

    private val viewPagerAdapterDC: ViewPagerAdapterDC by lazy {
        ViewPagerAdapterDC(
            supportFragmentManager, lifecycle
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupBottomNav()

        setupOnClickListeners()

    }

    private fun setupBottomNav() {

        binding.viewPagerDC.adapter = viewPagerAdapterDC

        binding.bottomBarDC.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.n_DC -> {
                    binding.viewPagerDC.currentItem = 0
                    binding.toolbarTitle.text = "Direct Chat"
                }

                R.id.n_contacts -> {
                    binding.viewPagerDC.currentItem = 1
                    binding.toolbarTitle.text = "Contacts"
                }
            }
            true
        }
    }

    private fun setupOnClickListeners() {

        binding.settings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.ivBack.setOnClickListener {
            finish()
        }
    }

}