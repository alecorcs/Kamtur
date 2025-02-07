package ui.group

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import bbdd.CambioTurnoFirebase
import com.example.kamtur.R
import com.example.kamtur.databinding.ActivityGroupDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.kamtur.modelo.Empleados
import ui.calendar.YourCalendarActivity
import ui.turno.TurnoActivity
import ui.chat.ChatActivity
import ui.notificacion.NotificationActivity
import ui.profile.ProfileActivity

class GroupDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGroupDetailBinding
    private var empleados: MutableList<Empleados> = mutableListOf()
    private lateinit var empleadoSesion: Empleados
    private lateinit var empleadoAdapter: EmpleadoAdapter
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupDetailBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        getEmpleadoSesion()
        initListeners()
        initUI()



    }

    private fun getEmpleadoSesion(){
        val idEmpleado = firebaseAuth.currentUser?.uid
        CambioTurnoFirebase().getEmpleadoFirebase(idEmpleado) { datosEmpleado ->
            datosEmpleado?.let {
                empleadoSesion = it
                recoverEmpleados(empleadoSesion)
                Log.d("GroupDetailActivity", "Datos del empleado: $empleadoSesion")
            }
        }
    }
    private fun recoverEmpleados(empleadoSesion: Empleados) {
        val idServicio = empleadoSesion.id_servicio
        if (idServicio != null){
            CambioTurnoFirebase().getEmpleadosServiceFirebase(idServicio) { listaEmpleados ->
                empleados.clear()
                empleados.addAll(listaEmpleados)
                empleadoAdapter.notifyDataSetChanged()
                Log.d("GroupDetailActivity", "Empleados recuperados: $empleados")
                //showMembersService()
            }
        }

    }

    private fun initListeners(){
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
        CambioTurnoFirebase().getServiciosEmpleadoFirebase { servicios ->
            if (servicios.isNotEmpty()){
                binding.tvNombreGrupo.text = servicios
            }

        }

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

        val groupName = intent.getStringExtra("nombreGrupo")
        binding.tvGroupName.text = groupName

        empleadoAdapter = EmpleadoAdapter(empleados)
        binding.rvEmpleadosGrupo.layoutManager = LinearLayoutManager(this)
        binding.rvEmpleadosGrupo.adapter = empleadoAdapter
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