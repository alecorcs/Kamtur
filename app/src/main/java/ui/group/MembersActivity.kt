package ui.group

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import bbdd.CambioTurnoFirebase
import com.applandeo.materialcalendarview.CalendarDay
import com.applandeo.materialcalendarview.listeners.OnCalendarDayClickListener
import com.example.kamtur.R
import com.example.kamtur.databinding.ActivityMembersBinding
import com.google.firebase.auth.FirebaseAuth
import com.kamtur.modelo.TurnosEmpleados
import ui.calendar.TurnoAdapter
import ui.calendar.YourCalendarActivity
import ui.turno.TurnoActivity
import ui.chat.ChatActivity
import ui.notificacion.NotificationActivity
import ui.profile.ProfileActivity
import java.util.Calendar
import java.util.Locale

class MembersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMembersBinding
    private lateinit var turnoCompaAdapter: TurnoCompaAdapter
    private lateinit var firebaseAuth: FirebaseAuth
    //Arraylist de calendario
    private val calendars: ArrayList<CalendarDay> = ArrayList()
    //Se crea la lista de turnos
    private val turno: MutableList<TurnosEmpleados> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMembersBinding.inflate(layoutInflater)
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
        val idEmpleado = intent.getStringExtra("idEmpleado")
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

    private fun initListeners(){
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

    private fun initUI(){
        turnoCompaAdapter = TurnoCompaAdapter(turno)
        binding.rvTasks.layoutManager = LinearLayoutManager(this)
        binding.rvTasks.adapter = turnoCompaAdapter

        CambioTurnoFirebase().getServiciosEmpleadoFirebase { servicios ->
            binding.tvNombreGrupo.text = servicios
        }
        val nombreEmpleado = intent.getStringExtra("nombreEmpleado")
        binding.tvMemberName.text = "Calendario de $nombreEmpleado"

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
    private fun dateToString(calendar: Calendar): String {
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        return "$day/$month/$year"
    }

    private fun getTurnos(date: String) {

        val turnoSelect = turno.filter { it.fecha_turno == date }
        Log.d("YourCalendarActivity", "Lista de turnos filtrados: $turno")

        turnoCompaAdapter.turnos = turnoSelect.toMutableList()
        turnoCompaAdapter.notifyDataSetChanged()

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