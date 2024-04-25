package com.mrapps.statussaver.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mrapps.statussaver.fragment.FragmentContacts
import com.mrapps.statussaver.fragment.FragmentDirectChat

class ViewPagerAdapterDC(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentDirectChat()
            1 -> FragmentContacts()
            else -> FragmentDirectChat()
        }
    }

}