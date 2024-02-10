package com.androvine.statussaver.activity

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
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


    private fun isWhatsappInstalled(): Boolean {
        val pm = this.packageManager
        var appInstalled = false
        try {
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
            appInstalled = true
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("TAG", "isWhatsappInstalled: ", e)
        }
        return appInstalled
    }

    private fun showWhatsappNotInstalledDialog() {

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_whatsapp_not_installed)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        val okButton = dialog.findViewById<Button>(R.id.btnOk)
        okButton.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()

    }

    override fun onResume() {
        super.onResume()
        if (!isWhatsappInstalled()) {
            showWhatsappNotInstalledDialog()
        }
    }
}