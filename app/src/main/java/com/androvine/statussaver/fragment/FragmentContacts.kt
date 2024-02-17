package com.androvine.statussaver.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.androvine.statussaver.adapter.ContactAdapter
import com.androvine.statussaver.databinding.FragmentContactsBinding
import com.androvine.statussaver.db.DirectChatContactDB
import com.androvine.statussaver.model.ModelContact

class FragmentContacts : Fragment() {

    private val binding: FragmentContactsBinding by lazy {
        FragmentContactsBinding.inflate(layoutInflater)
    }

    private val contactList = mutableListOf<ModelContact>()
    private lateinit var contactAdapter: ContactAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contactAdapter = ContactAdapter(requireContext(), mutableListOf())
        setupUI()
    }

    private fun updateContactList() {
        val contactDB = DirectChatContactDB(requireActivity())
        val contactListData = contactDB.getAllContacts()
        contactList.clear()
        contactList.addAll(contactListData)
        contactAdapter.updateList(contactList)
    }

    private fun setupRecyclerView() {

        binding.savedContactsRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = contactAdapter

        }

    }

    private fun setupUI() {
        if (contactList.isEmpty()) {
            binding.savedContactsRv.visibility = View.GONE
            binding.noContactsLayout.visibility = View.VISIBLE
        } else {
            binding.savedContactsRv.visibility = View.VISIBLE
            binding.noContactsLayout.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        updateContactList()
        setupRecyclerView()
        setupUI()
    }


}