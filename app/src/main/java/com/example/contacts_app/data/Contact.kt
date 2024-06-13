package com.example.contacts_app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

//Entidade de contato, tendo os campos que cada contato terá
@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val cpf: String?,
    val registrationDate: Date,
    val birthDate: Date,
    val state: String,
    val phoneNumbers: List<String>
)
