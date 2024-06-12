package com.example.contacts_app

import ContactViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.contacts_app.adapter.ContactAdapter
import com.example.contacts_app.data.Contact
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val contactViewModel: ContactViewModel by viewModels()
    private lateinit var adapter: ContactAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        adapter = ContactAdapter(
            onEditClick = { contact -> showAddEditDialog(contact) },
            onDeleteClick = { contact -> contactViewModel.delete(contact) }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        contactViewModel.allContacts.observe(this, Observer { contacts ->
            contacts?.let { adapter.setContacts(it) }
        })

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            showAddEditDialog()
        }
    }

    private fun isCPFValid(cpf: String): Boolean {
        return cpf.length == 11
    }

    private fun isAdult(birthDate: Date): Boolean {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -18)
        return birthDate.before(calendar.time)
    }

    private fun parseDate(dateString: String): Date? {
        return try {
            SimpleDateFormat("dd/MM/yyyy", Locale.US).parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    private fun showAddEditDialog(contact: Contact? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_contact, null)
        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etCPF = dialogView.findViewById<EditText>(R.id.etCPF)
        val etBirthDate = dialogView.findViewById<EditText>(R.id.etBirthDate)
        val spinnerState = dialogView.findViewById<Spinner>(R.id.spinnerState)
        val etPhoneNumbers = dialogView.findViewById<EditText>(R.id.etPhoneNumbers)

        val states = arrayOf("SP", "MG", "RJ", "ES")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, states)
        spinnerState.adapter = adapter

        if (contact != null) {
            etName.setText(contact.name)
            etCPF.setText(contact.cpf)
            etBirthDate.setText(SimpleDateFormat("dd/MM/yyyy", Locale.US).format(contact.birthDate))
            spinnerState.setSelection(states.indexOf(contact.state))
            etPhoneNumbers.setText(contact.phoneNumbers.joinToString(","))
            if (contact.state == "SP") {
                etCPF.visibility = View.VISIBLE
            }
        }

        spinnerState.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val state = states[position]
                etCPF.visibility = if (state == "SP") View.VISIBLE else View.GONE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        AlertDialog.Builder(this)
            .setTitle(if (contact == null) "Adicionar Contato" else "Editar Contato")
            .setView(dialogView)
            .setPositiveButton("Salvar") { _, _ ->
                val name = etName.text.toString().trim()
                val cpf = etCPF.text.toString().trim()
                val birthDateString = etBirthDate.text.toString().trim()
                val birthDate = parseDate(birthDateString)
                val state = spinnerState.selectedItem.toString()
                val phoneNumbers = etPhoneNumbers.text.toString().split(",").map { it.trim() }

                if (name.isEmpty()) {
                    Toast.makeText(this, "Nome é obrigatório", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (state == "SP" && !isCPFValid(cpf)) {
                    Toast.makeText(this, "CPF inválido", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (state == "MG" && (birthDate == null || !isAdult(birthDate))) {
                    Toast.makeText(this, "Contato deve ter pelo menos 18 anos", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (birthDate == null) {
                    Toast.makeText(this, "Data de nascimento inválida", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val newContact = Contact(
                    id = contact?.id ?: 0,
                    name = name,
                    cpf = if (state == "SP") cpf else null,
                    registrationDate = contact?.registrationDate ?: Date(),
                    birthDate = birthDate,
                    state = state,
                    phoneNumbers = phoneNumbers
                )

                if (contact == null) {
                    contactViewModel.insert(newContact)
                } else {
                    contactViewModel.update(newContact)
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()
            .show()
    }
}
