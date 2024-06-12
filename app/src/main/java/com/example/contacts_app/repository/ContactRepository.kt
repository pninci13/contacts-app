package com.example.contacts_app.repository

import androidx.lifecycle.LiveData
import com.example.contacts_app.data.Contact
import com.example.contacts_app.data.ContactDAO

class ContactRepository(private val contactDao: ContactDAO) {
    val allContacts: LiveData<List<Contact>> = contactDao.getAllContacts()

    suspend fun insert(contact: Contact) {
        contactDao.insert(contact)
    }

    suspend fun update(contact: Contact) {
        contactDao.update(contact)
    }

    suspend fun delete(contact: Contact) {
        contactDao.delete(contact)
    }
}