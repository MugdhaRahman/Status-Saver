package com.mrapps.statussaver.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mrapps.statussaver.databinding.FragmentDirectChatBinding
import com.mrapps.statussaver.db.DirectChatContactDB
import com.mrapps.statussaver.model.ModelContact
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class FragmentDirectChat : Fragment() {

    private val binding: FragmentDirectChatBinding by lazy {
        FragmentDirectChatBinding.inflate(layoutInflater)
    }

    private val directChatContactDB: DirectChatContactDB by lazy {
        DirectChatContactDB(requireContext())
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupOnClicks()

    }

    private fun setupOnClicks() {

        binding.ivClear.setOnClickListener {
            binding.number.setText("")
            hideKeyboard()
        }

        binding.chkSave.setOnCheckedChangeListener { _, b ->
            if (b) {
                binding.name.visibility = View.VISIBLE
            } else {
                binding.name.visibility = View.GONE
            }
        }

        binding.send.setOnClickListener {
            if (binding.number.text.toString().trim().isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a number", Toast.LENGTH_SHORT).show()
            } else if (binding.messageField.text.toString().trim().isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a message", Toast.LENGTH_SHORT)
                    .show()
            } else {
                if (binding.chkSave.isChecked) {
                    val name: String = binding.name.text.toString()
                    val number =
                        binding.ccp.selectedCountryCode + " " + binding.number.text
                            .toString()
                    if (directChatContactDB.checkIfContactExists(number)) {
                        Toast.makeText(
                            requireContext(),
                            "Contact already saved",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        directChatContactDB.addSingleContact(ModelContact(name, number))
                    }
                }
                sendWhatsAppMessage()
            }
        }


    }


    private fun sendWhatsAppMessage() {
        if (binding.number.text.toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Please Enter a Number", Toast.LENGTH_SHORT).show()
        } else if (binding.messageField.text.toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Please Enter a Message", Toast.LENGTH_SHORT).show()
        } else {
            val str = binding.ccp.selectedCountryCode + " " + binding.number.text.toString()
            try {
                startActivity(
                    Intent(
                        "android.intent.action.VIEW",
                        Uri.parse(
                            "whatsapp://send/?text=" + URLEncoder.encode(
                                binding.messageField.text.toString(),
                                "UTF-8"
                            ) + "&phone=" + str
                        )
                    )
                )
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        }
    }


    private fun hideKeyboard() {
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.messageField.windowToken, 0)
    }

}