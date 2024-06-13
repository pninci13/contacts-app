package com.example.contacts_app.repository

import androidx.lifecycle.LiveData
import com.example.contacts_app.data.Contact
import com.example.contacts_app.data.ContactDAO


// Cria as operações de CRUD que são realizadas no Banco
class ContactRepository(private val contactDao: ContactDAO) {
    // Detectar mudança de dados
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