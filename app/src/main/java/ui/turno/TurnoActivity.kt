package ui.turno


import android.app.Dialog

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import bbdd.CambioTurnoFirebase
import com.example.kamtur.R
import com.example.kamtur.databinding.ActivityTurnoBinding
import com.google.firebase.auth.FirebaseAuth
import com.kamtur.modelo.CambioTurno
import com.kamtur.modelo.SolicitudCambioTurno
import com.kamtur.modelo.TurnosEmpleados
import ui.calendar.YourCalendarActivity
import ui.chat.ChatActivity
import ui.group.GroupActivity
import ui.notificacion.NotificationActivity
import ui.profile.ProfileActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class TurnoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTurnoBinding
    private lateinit var dialog: Dialog
    private lateinit var cambioTurno: CambioTurno
    private lateinit var cambioTurnoAdapter: CambioTurnoAdapter
    private var turnoSeleccionado: String? = null

    private var turnosEncontrados: MutableList<TurnosEmpleados> = mutableListOf()
    private var turnoEncontrado = false
    private var turnoEmpleado: TurnosEmpleados? = null
    private var firebaseAuth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTurnoBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //Llamar a adapter y viewHolder con la lista de cambios de turno
        getChanges()
        initListeners()
        initUI()
    }

    private fun initListeners() {
        binding.fabChangeTurn.setOnClickListener {
            showDialog()
        }

        binding.ibCalendar.setOnClickListener {
            val intent = android.content.Intent(this, YourCalendarActivity::class.java)
            startActivity(intent)
        }
        binding.ibProfile.setOnClickListener {
            val intent = android.content.Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        binding.ibGroup.setOnClickListener {
            val intent = android.content.Intent(this, GroupActivity::class.java)
            startActivity(intent)
        }
        binding.ibChat.setOnClickListener {
            val intent = android.content.Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }
        binding.ibChange.setOnClickListener {
            val intent = android.content.Intent(this, TurnoActivity::class.java)
            startActivity(intent)
        }
        binding.ibNotification.setOnClickListener {
            val intent = android.content.Intent(this, NotificationActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initUI() {
        CambioTurnoFirebase().getServiciosEmpleadoFirebase { servicios ->
            if (servicios.isNotEmpty()) {
                binding.tvNombreGrupo.text = servicios
            }
        }

        firebaseAuth = FirebaseAuth.getInstance()
        CambioTurnoFirebase().getNotificacionesFirebase(firebaseAuth.currentUser!!.uid) { notificaciones ->
            if (notificaciones.isNotEmpty()) {
                for (notificacion in notificaciones) {
                    if (notificacion.leida == 0) {
                        binding.ivNoLeido.visibility = View.VISIBLE
                        break
                    }
                }
            }
        }
    }

    private fun showDialog() {
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_change_turn)

        val calendarView: CalendarView = dialog.findViewById(R.id.calendarView)
        val spinTurn: Spinner = dialog.findViewById(R.id.spinTurnos)
        val btnPublicar: Button = dialog.findViewById(R.id.btnPublicarCambio)
        val nombreTurnos: MutableList<String> = mutableListOf()


        val idEmpleado = firebaseAuth.currentUser?.uid


        calendarView.setOnDateChangeListener { _, dayOfMonth, month, year ->
            val selectedDate = Calendar.getInstance().apply {
                set(dayOfMonth, month, year)
            }
            val seletedDateString = dateToString(selectedDate)
            Log.d("YourCalendarActivity", "Fecha seleccionada: $seletedDateString")


            CambioTurnoFirebase().getTurnosEmpleadoFirebase(idEmpleado!!) { turnos ->
                if (turnos.isNotEmpty()) {
                    nombreTurnos.clear()
                    turnosEncontrados.clear()
                    turnoEncontrado = false

                    for (turno in turnos) {
                        if (turno.fecha_turno == seletedDateString) {
                            nombreTurnos.add(turno.nombre_turno)
                            turnosEncontrados.add(turno)
                            turnoEncontrado = true

                        }
                    }
                    if (!turnoEncontrado) {
                        nombreTurnos.add("No hay turnos")
                    }
                }else{
                    Toast.makeText(this, "Primero debes añadir tus turnos al calendario", Toast.LENGTH_SHORT).show()
                }

                turnoSeleccionado = nombreTurnos[0]
                // Crear un ArrayAdapter usando el array de strings y un layout de spinner predeterminado
                val adapter = ArrayAdapter(this, R.layout.item_spinner, nombreTurnos)
                // Especificar el layout a usar cuando la lista de opciones aparezca
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Aplicar el adapter al spinner
                spinTurn.adapter = adapter
            }
        }



        spinTurn.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                turnoSeleccionado = nombreTurnos[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Por si no se seleciona nada
            }
        }

        btnPublicar.setOnClickListener {
            for (turno in turnosEncontrados){
                if (turno.nombre_turno == turnoSeleccionado) {
                    turnoEmpleado = turno
                    break
                }
            }
            if (turnoEmpleado != null) {
                publicarCambio(idEmpleado!!, turnoEmpleado!!)
            } else {
                Toast.makeText(
                    this,
                    "No hay turnos disponibles para la fecha seleccionada",
                    Toast.LENGTH_SHORT
                ).show()
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun publicarCambio(idEmpleado: String, turnoConfirm: TurnosEmpleados) {
        val date = LocalDate.now()
        val formato = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val fechaPublicacion = formato.format(date)

        cambioTurno = CambioTurno(
            "",
            idEmpleado,
            turnoConfirm.id_turno,
            fechaPublicacion,
            "pendiente",
            null
        )

        CambioTurnoFirebase().addCambioFirebase(cambioTurno)

        getChanges()

    }


    private fun dateToString(calendar: Calendar): String {
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        return "$day/$month/$year"
    }

    private fun getChanges() {
        CambioTurnoFirebase().getCambioFirebase("pendiente") { cambios ->
            if (cambios.isNotEmpty()) {
                binding.rvCambios.layoutManager = LinearLayoutManager(this)
                cambioTurnoAdapter = CambioTurnoAdapter(cambios)
                binding.rvCambios.adapter = cambioTurnoAdapter
                cambioTurnoAdapter.notifyDataSetChanged()

            } else Toast.makeText(this, "No hay cambios disponibles", Toast.LENGTH_SHORT).show()
        }

    }
}