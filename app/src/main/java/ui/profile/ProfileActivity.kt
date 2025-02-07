package ui.profile

import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import bbdd.CambioTurnoFirebase
import com.bumptech.glide.Glide
import com.example.kamtur.R
import com.example.kamtur.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.kamtur.modelo.Empleados
import ui.calendar.YourCalendarActivity
import ui.turno.TurnoActivity
import ui.chat.ChatActivity
import ui.group.GroupActivity
import ui.login.LoginActivity
import ui.notificacion.NotificationActivity

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var empleado: Empleados
    private val idEmpleado = FirebaseAuth.getInstance().currentUser?.uid
    private var usuario: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initListeners()
        initUI()
        cambiarEstadoCuenta()
    }


    private fun initListeners() {
        binding.ivEditImg.setOnClickListener {
            val intent = android.content.Intent(this, EditImageProfileActivity::class.java)
            startActivity(intent)
        }

        binding.ibInfo.setOnClickListener {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.dialog_info_app)
            val goBack: Button = dialog.findViewById(R.id.btnReturn)
            goBack.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
            dialog.setCanceledOnTouchOutside(false)
        }
        binding.btnGuardar.setOnClickListener {
            actualizarInformacionEmpleado()
        }
        binding.btnlogOut.setOnClickListener {
            val hashMap = HashMap<String, Any>()
            hashMap["estado_empleado"] = "offline"
            CambioTurnoFirebase().updateEmpleadoFirebase(hashMap)
            FirebaseAuth.getInstance().signOut()
            val intent = android.content.Intent(this, LoginActivity::class.java)
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
            startActivity(intent)
        }
        binding.mbVerificar.setOnClickListener {
            if (usuario!!.isEmailVerified) {
                Toast.makeText(this, "Ya has verificado tu email", Toast.LENGTH_SHORT).show()
            } else{
                val alertDialog = AlertDialog.Builder(this)

                alertDialog.setTitle("Verificar Email")
                    .setMessage("¿Quieres enviar un email de verificación a ${usuario!!.email}?")
                    .setPositiveButton("Sí") { _, _ ->
                        verificarEmail()
                        Toast.makeText(this, "Se ha enviado un email de verificación", Toast.LENGTH_SHORT).show()
                    }.setNegativeButton("No") { d, _ ->
                        d.dismiss()
                    }.show()


            }
        }
        binding.ibCalendar.setOnClickListener {
            val intent = android.content.Intent(this, YourCalendarActivity::class.java)
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
            binding.tvServicio.text = servicios
        }


        CambioTurnoFirebase().getNotificacionesFirebase(usuario!!.uid) { notificaciones ->
            if (notificaciones.isNotEmpty()) {
                for (notificacion in notificaciones) {
                    if (notificacion.leida == 0) {
                        binding.ivNoLeido.visibility = View.VISIBLE
                        break
                    }
                }
            }
        }

        CambioTurnoFirebase().getEmpleadoFirebase(idEmpleado) { datosEmpleado ->
            datosEmpleado?.let {
                empleado = it
                Log.d("ProfileActivity", "Datos del empleado: $empleado")
                binding.tvTituloPerfil.text = "Perfil de ${empleado.nombre_empleado}"
                binding.tvId.text = empleado.id_empleado
                val nombreEmpleado = empleado.nombre_empleado
                val apellidosEmpleado = empleado.apellidos_empleado
                binding.tvEmail.text = empleado.email_empleado

                binding.etNombre.setText(nombreEmpleado)
                binding.etApellidos.setText(apellidosEmpleado)

                Glide.with(this).load(empleado.foto_empleado).placeholder(R.drawable.ic_user)
                    .into(binding.ivPerfil)

                Glide.with(this).load(empleado.foto_portada).placeholder(R.drawable.imagen_portada).into(binding.ivPortada)
            } ?: run {
                Toast.makeText(this, "Error al cargar los datos del empleado", Toast.LENGTH_SHORT)
                    .show()
            }

        }

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Cargando...")
        progressDialog!!.setCanceledOnTouchOutside(false)
    }

    private fun actualizarInformacionEmpleado() {
        val hashmap = HashMap<String, Any>()
        hashmap["nombre_empleado"] = binding.etNombre.text.toString()
        hashmap["apellidos_empleado"] = binding.etApellidos.text.toString()

        CambioTurnoFirebase().updateEmpleadoFirebase(hashmap)

    }


    private fun verificarEmail() {
        progressDialog!!.setMessage("Enviando email verificacion a ${usuario!!.email}")
        progressDialog!!.show()

        usuario!!.sendEmailVerification().addOnSuccessListener {
            progressDialog!!.dismiss()
            Toast.makeText(this, "Enviado email. Revisa tu email", Toast.LENGTH_SHORT)
                .show()

        }.addOnFailureListener{ e ->
            progressDialog!!.dismiss()
            Toast.makeText(this, "No se ha podido enviar el email de verificación por ${e.message}", Toast.LENGTH_SHORT).show()

        }
    }

    private fun cambiarEstadoCuenta() {
       if (usuario!!.isEmailVerified) {
           binding.mbVerificar.text = "Verificado"
       } else {
           binding.mbVerificar.text = "No Verificado"
       }
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