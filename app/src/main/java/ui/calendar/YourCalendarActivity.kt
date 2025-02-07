package ui.calendar

import android.app.Dialog
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import bbdd.CambioTurnoFirebase
import com.applandeo.materialcalendarview.CalendarDay
import com.applandeo.materialcalendarview.listeners.OnCalendarDayClickListener
import com.example.kamtur.R
import com.example.kamtur.databinding.ActivityYourCalendarBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.kamtur.modelo.TurnosEmpleados
import ui.turno.TurnoActivity
import ui.chat.ChatActivity
import ui.group.GroupActivity
import ui.notificacion.NotificationActivity
import ui.profile.ProfileActivity
import java.util.Locale
import java.util.Calendar

class YourCalendarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityYourCalendarBinding
    private lateinit var turnoAdapter: TurnoAdapter
    private lateinit var auth: FirebaseAuth

    //Se crea la lista de turnos
    private val turno: MutableList<TurnosEmpleados> = mutableListOf()


    //Arraylist de calendario
    private val calendars: ArrayList<CalendarDay> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityYourCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        recoverTurn()
        initListeners()
        initUI()


    }

    private fun recoverTurn() {
        val idEmpleado = FirebaseAuth.getInstance().currentUser?.uid
        if (idEmpleado != null) {
            CambioTurnoFirebase().getTurnosEmpleadoFirebase(idEmpleado){ listaTurnos ->
                Log.d("YourCalendarActivity", "Lista de turnos: $listaTurnos")
                turno.clear()
                turno.addAll(listaTurnos)
                turno.forEach{ turn ->
                    val fechaTurno = stringtoDate(turn.fecha_turno)
                    if (fechaTurno != null) {
                        drawCalendar(fechaTurno)
                    }

                }
            }
        }
    }

    private fun initListeners() {
        binding.fabAddTurn.setOnClickListener {
            showAddTurnDialog()
        }

        binding.calendarView.setOnCalendarDayClickListener(object: OnCalendarDayClickListener {
            override fun onClick(calendarDay: CalendarDay) {
                val dayOfMonth = calendarDay.calendar.get(Calendar.DAY_OF_MONTH)
                val month = calendarDay.calendar.get(Calendar.MONTH)
                val year = calendarDay.calendar.get(Calendar.YEAR)

                val calendar = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                val dateString = dateToString(calendar)
                Log.d("YourCalendarActivity", "Fecha seleccionada: $dateString")
                recoverTurn()
                getTurnos(dateString)
            }

        })
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

        turnoAdapter = TurnoAdapter(turno)
        binding.rvTasks.layoutManager = LinearLayoutManager(this)
        binding.rvTasks.adapter = turnoAdapter

        CambioTurnoFirebase().getServiciosEmpleadoFirebase { servicios ->
            binding.tvNombreGrupo.text = servicios
        }

        auth = FirebaseAuth.getInstance()
        CambioTurnoFirebase().getNotificacionesFirebase(auth.currentUser!!.uid) { notificaciones ->
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


    private fun dateToString(calendar: Calendar): String {
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        return "$day/$month/$year"
    }


    private fun showAddTurnDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_turn)

        val calendarView = dialog.findViewById<CalendarView>(R.id.calendarView)
        val etTurn = dialog.findViewById<EditText>(R.id.etTurn)
        val btnAddTurn = dialog.findViewById<Button>(R.id.btnAddTurn)

        var seletedDateString: String? = null


        calendarView.setOnDateChangeListener { _, dayOfMonth, month, year ->
            val selectedDate = Calendar.getInstance().apply {
                set(dayOfMonth, month, year)
            }
            seletedDateString = dateToString(selectedDate)
            Log.d("YourCalendarActivity", "Fecha seleccionada: $seletedDateString")
        }


        btnAddTurn.setOnClickListener {
            val turn = etTurn.text.toString().lowercase()
            auth = FirebaseAuth.getInstance()

            if (turn.isNotEmpty() && seletedDateString != null) {
                val refTurnos = FirebaseDatabase.getInstance().getReference("turnosEmpleados")
                val id = refTurnos.push().key
                val idEmpleado = auth.currentUser?.uid

                if (idEmpleado != null) {
                    val turnos = TurnosEmpleados(
                        id ?: "",
                        turn,
                        seletedDateString!!,
                        idEmpleado,
                    )

                    try {
                        CambioTurnoFirebase().addTurnFirebase(turnos)
                        recoverTurn()
                        getTurnos(seletedDateString!!)
                        dialog.dismiss()
                    } catch (e: Exception) {
                        Log.e("YourCalendarActivity", "Error añadiendo turnos: ${e.message}")
                    }
                } else {
                    Log.e("YourCalendarActivity", "Error obteniendo el ID del empleado")
                }

            }else Toast.makeText(this, "Debes rellenar todos los campos", Toast.LENGTH_SHORT).show()
        }
        dialog.show()

    }

    private fun getTurnos(date: String) {

        val turnoSelect = turno.filter { it.fecha_turno == date }
        Log.d("YourCalendarActivity", "Lista de turnos filtrados: $turno")

        turnoAdapter.turnos = turnoSelect.toMutableList()
        turnoAdapter.notifyDataSetChanged()

    }

    private fun stringtoDate(date: String): Calendar? {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        try {
            val fecha = dateFormat.parse(date)
            val calendar = Calendar.getInstance()
            calendar.time = fecha

            return calendar
        }catch (e: Exception){
            Log.e("YourCalendarActivity", "Error transformando fecha: ${e.message}")
            return null
        }


    }

    private fun drawCalendar(date: Calendar) {

        val calendarDay = CalendarDay(date)
        calendarDay.labelColor = R.color.kamtur_background_button_decline
        calendarDay.imageResource = R.drawable.ic_work
        calendars.add(calendarDay)
        binding.calendarView.setCalendarDays(calendars)



    }

    override fun onResume() {
        super.onResume()
        val hashMap = HashMap<String, Any>()
        hashMap["estado_empleado"] = "online"

        CambioTurnoFirebase().updateEmpleadoFirebase(hashMap)
    }

    override fun onPause() {
        super.onPause()
        val hashMap = HashMap<String, Any>()
        hashMap["estado_empleado"] = "offline"
        CambioTurnoFirebase().updateEmpleadoFirebase(hashMap)
    }

}



