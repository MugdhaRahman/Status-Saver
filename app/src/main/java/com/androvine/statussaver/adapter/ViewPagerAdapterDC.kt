package com.androvine.statussaver.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.androvine.statussaver.fragment.FragmentContacts
import com.androvine.statussaver.fragment.FragmentDirectChat
import com.androvine.statussaver.fragment.FragmentSaved
import com.androvine.statussaver.fragment.FragmentStatus

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