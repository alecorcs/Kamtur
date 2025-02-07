package ui.notificacion

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import bbdd.CambioTurnoFirebase
import com.example.kamtur.R
import com.example.kamtur.databinding.ActivityNotificationBinding
import com.google.firebase.auth.FirebaseAuth
import ui.calendar.YourCalendarActivity
import ui.chat.ChatActivity
import ui.group.GroupActivity
import ui.profile.ProfileActivity
import ui.turno.TurnoActivity

class NotificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initListeners()
        initUI()
    }

    private fun initListeners() {
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
            if (servicios.isNotEmpty()){
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
                binding.rvNotification.layoutManager = LinearLayoutManager(this)
                notificationAdapter = NotificationAdapter(notificaciones)
                binding.rvNotification.adapter = notificationAdapter
                notificationAdapter.notifyDataSetChanged()
            }
        }
    }
}