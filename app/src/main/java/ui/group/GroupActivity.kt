package ui.group

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import bbdd.CambioTurnoFirebase
import com.example.kamtur.R
import com.example.kamtur.databinding.ActivityGroupBinding
import com.google.firebase.auth.FirebaseAuth
import ui.calendar.YourCalendarActivity
import ui.turno.TurnoActivity
import ui.chat.ChatActivity
import ui.notificacion.NotificationActivity
import ui.profile.ProfileActivity

class GroupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGroupBinding
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityGroupBinding.inflate(layoutInflater)
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

        binding.tvNewGroup.setOnClickListener {
            showAddGroupDialog()
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

                recrearGrupo(servicios)
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

    private fun showAddGroupDialog() {

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_new_group)

        val etNameService = dialog.findViewById<EditText>(R.id.etNameService)
        val btnAddGroup = dialog.findViewById<TextView>(R.id.btnAddGroup)

        btnAddGroup.setOnClickListener {
            val groupName = etNameService.text.toString()
            if (groupName.isNotEmpty()) {
                crearNuevoGrupo(groupName)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Por favor, ingrese un nombre de grupo", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        dialog.show()

    }

    private fun crearNuevoGrupo(nombreGrupo: String) {
        CambioTurnoFirebase().checkToAddServicio(nombreGrupo)
        Log.d("GroupActivity", "Llamando a addServicio para grupo: $nombreGrupo")

        val nuevoTextView = TextView(this).apply {
            id = View.generateViewId()  // Genera un ID único para el nuevo TextView
            text = nombreGrupo
            textSize = 28f
            setPadding(20, 20, 20, 20)
            setBackgroundColor(resources.getColor(R.color.kamtur_background_card, null))
            setTextColor(resources.getColor(R.color.black, null))

            val drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_group, null)
            setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
            compoundDrawablePadding = 10

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(18, 18, 18, 18)

            layoutParams = params
            gravity = View.TEXT_ALIGNMENT_CENTER

            setOnClickListener {
                val intent =
                    android.content.Intent(this@GroupActivity, GroupDetailActivity::class.java)
                intent.putExtra("nombreGrupo", nombreGrupo)
                startActivity(intent)
            }
        }

        binding.linearLayoutmain.addView(nuevoTextView)

    }

    private fun recrearGrupo(nombreGrupo: String) {

        val nuevoTextView = TextView(this).apply {
            id = View.generateViewId()  // Genera un ID único para el nuevo TextView
            text = nombreGrupo
            textSize = 28f
            setPadding(20, 20, 20, 20)
            setBackgroundColor(resources.getColor(R.color.kamtur_background_card, null))
            setTextColor(resources.getColor(R.color.black, null))
            val drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_group, null)
            setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
            compoundDrawablePadding = 10

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(18, 18, 18, 18)

            layoutParams = params
            gravity = View.TEXT_ALIGNMENT_CENTER

            setOnClickListener {
                val intent =
                    android.content.Intent(this@GroupActivity, GroupDetailActivity::class.java)
                intent.putExtra("nombreGrupo", nombreGrupo)
                startActivity(intent)
            }
        }

        binding.linearLayoutmain.addView(nuevoTextView)
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