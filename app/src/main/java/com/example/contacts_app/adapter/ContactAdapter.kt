package com.example.contacts_app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.contacts_app.R
import com.example.contacts_app.data.Contact
import java.text.SimpleDateFormat
import java.util.*

class ContactAdapter(
    private val onEditClick: (Contact) -> Unit,
    private val onDeleteClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    private var contacts = emptyList<Contact>()

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contactName: TextView = itemView.findViewById(R.id.contactName)
        val editContact: ImageView = itemView.findViewById(R.id.editContact)
        val deleteContact: ImageView = itemView.findViewById(R.id.deleteContact)
        val contactDate: TextView = itemView.findViewById(R.id.contactDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val currentContact = contacts[position]
        holder.contactName.text = currentContact.name
        holder.editContact.setOnClickListener { onEditClick(currentContact) }
        holder.deleteContact.setOnClickListener { onDeleteClick(currentContact) }
        holder.contactDate.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(currentContact.registrationDate)
    }

    internal fun setContacts(contacts: List<Contact>) {
        this.contacts = contacts
        notifyDataSetChanged()
    }

    override fun getItemCount() = contacts.size
}
