package ui.chat

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import bbdd.CambioTurnoFirebase
import com.example.kamtur.R
import com.example.kamtur.databinding.ActivityChatBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kamtur.modelo.Mensajes
import ui.calendar.YourCalendarActivity
import ui.turno.TurnoActivity
import ui.chat.fragmentos.FragmentoChat
import ui.chat.fragmentos.FragmentoUsuario
import ui.group.GroupActivity
import ui.notificacion.NotificationActivity
import ui.profile.ProfileActivity

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private var usuario = FirebaseAuth.getInstance().currentUser!!.uid


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
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
            binding.tvNombreGrupo.text = servicios
        }

        CambioTurnoFirebase().getNotificacionesFirebase(usuario) { notificaciones ->
            if (notificaciones.isNotEmpty()) {
                for (notificacion in notificaciones) {
                    if (notificacion.leida == 0) {
                        binding.ivNoLeido.visibility = View.VISIBLE
                        break
                    }
                }
            }
        }

        val refMensajes = FirebaseDatabase.getInstance().getReference("mensajes")
        val tabLayout = binding.tabMain
        val viewPager = binding.viewMain

        refMensajes.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var contadorMensajes = 0
                for (mensajeSnapshot in snapshot.children) {
                    val mensaje = mensajeSnapshot.getValue(Mensajes::class.java)
                    if (mensaje!!.id_receptor == usuario && !mensaje.visto) {
                        contadorMensajes += 1
                    }
                }

                chatAdapter = ChatAdapter(this@ChatActivity)
                if (contadorMensajes == 0){
                    chatAdapter.addFragment(FragmentoChat(), "Chats")
                }else{
                    chatAdapter.addFragment(FragmentoChat(), "($contadorMensajes) Chats")
                }
                chatAdapter.addFragment(FragmentoUsuario(), "Usuarios")
                viewPager.adapter = chatAdapter

                TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                    tab.text = chatAdapter.getTitle(position)
                }.attach()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatActivity", "Error al recuperar los mensajes: ${error.message}")
            }
        })
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