package com.androvine.statussaver.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.androvine.statussaver.databinding.ItemSavedContactBinding
import com.androvine.statussaver.db.DirectChatContactDB
import com.androvine.statussaver.model.ModelContact
import java.io.UnsupportedEncodingException
import java.net.URLEncoder


class ContactAdapter(
    private val context: Context,
    private val listOfContact: MutableList<ModelContact>,
) :
    RecyclerView.Adapter<ContactAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemSavedContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = listOfContact[position]

        holder.binding.contactName.text = contact.name
        holder.binding.contactNumber.text = contact.number

        holder.binding.deleteContact.setOnClickListener {
            deleteContact(position)
        }

        holder.binding.message.setOnClickListener {
            holder.binding.rlMessage.visibility = View.VISIBLE
        }

        holder.binding.ivClose.setOnClickListener {
            holder.binding.messageField.setText("")
            holder.binding.rlMessage.visibility = View.GONE
        }

        holder.binding.send.setOnClickListener {
            holder.binding.rlMessage.visibility = View.GONE
            sendMessages(position, holder)

        }

    }

    override fun getItemCount(): Int {
        return listOfContact.size
    }

    fun updateList(list: MutableList<ModelContact>) {
        listOfContact.clear()
        listOfContact.addAll(list)
        notifyDataSetChanged()
    }


    fun deleteContact(position: Int) {
        val db = DirectChatContactDB(context)
        db.deleteSingleContact(listOfContact[position])
        listOfContact.removeAt(position)
        notifyItemRemoved(position)
    }


    fun deleteAllContacts() {
        val db = DirectChatContactDB(context)
        db.deleteAllContacts()
        listOfContact.clear()
        notifyDataSetChanged()
    }

    fun sendMessages(position: Int, holder: ViewHolder) {
        val contact = listOfContact[position]
        val number = contact.number
        val message = holder.binding.messageField.text.toString()

        if (message.isEmpty()) {
            Toast.makeText(context, "Please enter a message", Toast.LENGTH_SHORT).show()
        } else {
            try {
                context.startActivity(
                    Intent(
                        "android.intent.action.VIEW",
                        Uri.parse(
                            "whatsapp://send/?text=" + URLEncoder.encode(
                                message,
                                "UTF-8"
                            ) + "&phone=" + number
                        )
                    )
                )
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        }


    }

    class ViewHolder(val binding: ItemSavedContactBinding) : RecyclerView.ViewHolder(binding.root)

}