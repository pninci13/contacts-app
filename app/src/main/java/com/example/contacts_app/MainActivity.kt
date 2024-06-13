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
import com.example.contacts_app.adapter.ContactAdapter
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.contacts_app.data.Contact
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

// A main activty gerencia interações
class MainActivity : AppCompatActivity() {

    // Inicia viewmodel
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


        // Quando ocorrer mudança de dados, o adpter é atualizado
        contactViewModel.allContacts.observe(this, Observer { contacts ->
            contacts?.let { adapter.setContacts(it) }
        })

        // Botão de adicionar o contato
        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            showAddEditDialog()
        }
    }

    // Verifica quantidade de numeros no CPF
    private fun isCPFValid(cpf: String): Boolean {
        return cpf.length == 11
    }

    // Verifica a idade, pois no caso MG precisa ser +18 usando calendario
    private fun isAdult(birthDate: Date): Boolean {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, - 18)
        return birthDate.before(calendar.time)
    }

    // Formata a data no formado padrao
    private fun parseDate(dateString: String): Date? {
        return try {
            SimpleDateFormat("dd/MM/yyyy", Locale.US).parse(dateString)
        } catch (e: Exception) {
            null
        }
    }


    // Função para toda a manipulação do dialog de criar contato
    private fun showAddEditDialog(contact: Contact? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_contact, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etName)
        val etCPF = dialogView.findViewById<TextInputEditText>(R.id.etCPF)
        val etBirthDate = dialogView.findViewById<TextInputEditText>(R.id.etBirthDate)
        val spinnerState = dialogView.findViewById<Spinner>(R.id.spinnerState)
        val etPhoneNumbers = dialogView.findViewById<TextInputEditText>(R.id.etPhoneNumbers)

        // Siglas dos estados para selecionar no dropdown
        val states = arrayOf("AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS",
            "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO")

        // Referencia do Dropdown
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, states)
        spinnerState.adapter = adapter

        // Se o contato não for Null, seta as informações
        if (contact != null) {
            etName.setText(contact.name)
            etCPF.setText(contact.cpf)
            etBirthDate.setText(SimpleDateFormat("dd/MM/yyyy", Locale.US).format(contact.birthDate))
            spinnerState.setSelection(states.indexOf(contact.state))
            etPhoneNumbers.setText(contact.phoneNumbers.joinToString(","))
        }

        // Configuração do  MaterialDatePicker com um intervalo adequado
        etBirthDate.setOnClickListener {

            // Pega os 100 ultimos anos a partir de 2024
            val calendarStart = Calendar.getInstance()
            calendarStart.add(Calendar.YEAR, -100) //
            val calendarEnd = Calendar.getInstance()

            val constraintsBuilder = CalendarConstraints.Builder()
                .setStart(calendarStart.timeInMillis)   // Data incial do calendario
                .setEnd(calendarEnd.timeInMillis)       // Data final
                .setOpenAt(calendarEnd.timeInMillis)
                .setValidator(object : CalendarConstraints.DateValidator {
                    override fun isValid(date: Long): Boolean {
                        return date <= calendarEnd.timeInMillis
                    }

                    override fun writeToParcel(dest: android.os.Parcel, flags: Int) {}

                    override fun describeContents(): Int {
                        return 0
                    }
                })

            // Builda o picker de datas
            val builder = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecione Data")     // Titulo
                .setCalendarConstraints(constraintsBuilder.build())

            val picker = builder.build()

            picker.addOnPositiveButtonClickListener {
                val selectedDate = Date(it)
                etBirthDate.setText(SimpleDateFormat("dd/MM/yyyy", Locale.US).format(selectedDate))
            }

            picker.show(supportFragmentManager, picker.toString())
        }

        // Configurar o título personalizado do dialog no XML
        val customTitleView = LayoutInflater.from(this).inflate(R.layout.dialog_title, null)
        val dialogTitle = customTitleView.findViewById<TextView>(R.id.dialogTitle)
        dialogTitle.text = if (contact == null) "Adicionar Contato" else "Editar Contato"

        // Configuração do dialog (popup) que abre ao clicar no botao de +
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

                // Nome sempre é de preenchimento obrigatório
                if (name.isEmpty()) {
                    Toast.makeText(this, "Nome é obrigatório", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Se o estado for SP o CPF é obrigatório
                if (state == "SP" && !isCPFValid(cpf)) {
                    // Exibe mensagem de erro
                    Toast.makeText(this, "CPF inválido", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Se for MG, o contato precisa ter + 18 anos
                if (state == "MG" && (birthDate == null || !isAdult(birthDate))) {
                    // Se nao exibe erro
                    Toast.makeText(this, "Contato deve ter pelo menos 18 anos", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Data de nascimento também precisa ser preenchido
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

                // Se não tiver o contato, insere na lista se não tiver vazio quer dizer que existe
                // entao atualiza os dados do contato
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
