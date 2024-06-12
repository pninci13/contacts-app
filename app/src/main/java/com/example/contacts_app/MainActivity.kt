package com.example.contacts_app

import ContactViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.contacts_app.data.Contact
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

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
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etName)
        val etCPF = dialogView.findViewById<TextInputEditText>(R.id.etCPF)
        val etBirthDate = dialogView.findViewById<TextInputEditText>(R.id.etBirthDate)
        val spinnerState = dialogView.findViewById<Spinner>(R.id.spinnerState)
        val etPhoneNumbers = dialogView.findViewById<TextInputEditText>(R.id.etPhoneNumbers)

        val states = arrayOf("SP", "MG", "RJ", "ES")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, states)
        spinnerState.adapter = adapter

        if (contact != null) {
            etName.setText(contact.name)
            etCPF.setText(contact.cpf)
            etBirthDate.setText(SimpleDateFormat("dd/MM/yyyy", Locale.US).format(contact.birthDate))
            spinnerState.setSelection(states.indexOf(contact.state))
            etPhoneNumbers.setText(contact.phoneNumbers.joinToString(","))
        }

        // Configurar MaterialDatePicker com um intervalo adequado
        etBirthDate.setOnClickListener {
            val calendarStart = Calendar.getInstance()
            calendarStart.add(Calendar.YEAR, -100) // Start 100 years ago

            val calendarEnd = Calendar.getInstance() // End at the current date

            val constraintsBuilder = CalendarConstraints.Builder()
                .setStart(calendarStart.timeInMillis)
                .setEnd(calendarEnd.timeInMillis)
                .setOpenAt(calendarEnd.timeInMillis)
                .setValidator(object : CalendarConstraints.DateValidator {
                    override fun isValid(date: Long): Boolean {
                        return date <= calendarEnd.timeInMillis
                    }

                    override fun writeToParcel(dest: android.os.Parcel, flags: Int) {
                        // Not implemented
                    }

                    override fun describeContents(): Int {
                        return 0
                    }
                })

            val builder = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Birth Date")
                .setCalendarConstraints(constraintsBuilder.build())

            val picker = builder.build()

            picker.addOnPositiveButtonClickListener {
                val selectedDate = Date(it)
                etBirthDate.setText(SimpleDateFormat("dd/MM/yyyy", Locale.US).format(selectedDate))
            }

            picker.show(supportFragmentManager, picker.toString())
        }

        // Configurar o título personalizado no XML
        val customTitleView = LayoutInflater.from(this).inflate(R.layout.dialog_title, null)
        val dialogTitle = customTitleView.findViewById<TextView>(R.id.dialogTitle)
        dialogTitle.text = if (contact == null) "Adicionar Contato" else "Editar Contato"

        AlertDialog.Builder(this)
            .setCustomTitle(customTitleView)
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
