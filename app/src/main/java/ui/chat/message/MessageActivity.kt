package ui.chat.message

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import bbdd.CambioTurnoFirebase
import com.bumptech.glide.Glide
import com.example.kamtur.R
import com.example.kamtur.databinding.ActivityMessageBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.kamtur.modelo.Mensajes

class MessageActivity : AppCompatActivity() {
    lateinit var binding: ActivityMessageBinding
    private lateinit var mensajes: Mensajes
    private lateinit var mensajeAdapter: MensajeAdapter

    private var firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
    private var id_empleado: String? = ""
    private lateinit var fotoEmpleado: String

    private var img: Uri? = null
    private var vistoListener: ValueEventListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMessageBinding.inflate(layoutInflater)
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
        binding.ibSend.setOnClickListener {
            val mensaje = binding.etMessage.text.toString()
            if (mensaje.isNotEmpty()) {
                sendMessage(firebaseUser.uid, id_empleado!!, mensaje)
                binding.etMessage.text.clear()
            } else {
                Toast.makeText(this, "Escriba un mensaje", Toast.LENGTH_SHORT).show()
            }
        }

        binding.ibAttachment.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            }else  permisoGaleria.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
        }
    }

    private fun initUI() {

        val linearlayoutManager = LinearLayoutManager(applicationContext)
        linearlayoutManager.stackFromEnd = true
        binding.rvChat.layoutManager = linearlayoutManager

        id_empleado = intent.getStringExtra("id_empleado").toString()

        CambioTurnoFirebase().getEmpleadoFirebase(id_empleado) { empleado ->
            if (empleado != null) {
                binding.tvUserName.text = empleado.nombre_empleado
                Glide.with(this).load(empleado.foto_empleado)
                    .placeholder(R.drawable.ic_profile_chat).into(binding.ivPerfilUser)
                fotoEmpleado = empleado.foto_empleado.toString()

                CambioTurnoFirebase().getMensajesFirebase(
                    firebaseUser.uid,
                    empleado.id_empleado
                ) { listaMensajes ->
                    mensajeVisto(empleado.id_empleado)
                    mensajeAdapter = MensajeAdapter(this, listaMensajes, fotoEmpleado)
                    binding.rvChat.adapter = mensajeAdapter
                    mensajeAdapter.notifyDataSetChanged()

                }

            }

        }


    }

    private fun sendMessage(idEmisor: String, idReceptor: String, mensaje: String) {
        val reference = FirebaseDatabase.getInstance().getReference("mensajes")
        val idMensaje = reference.push().key
        mensajes = Mensajes(
            idMensaje!!,
            idEmisor,
            idReceptor,
            mensaje,
            "",
            false
        )

        CambioTurnoFirebase().addMessageFirebase(mensajes, idReceptor)

    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        openGalleryForResult.launch(intent)
    }


    private val openGalleryForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { resultado ->
            if (resultado.resultCode == RESULT_OK) {
                val data = resultado.data
                img = data?.data

                val cargando = ProgressDialog(this)
                cargando.setMessage("Cargando imagen")
                cargando.setCanceledOnTouchOutside(false)
                cargando.show()

                val storageReference =
                    FirebaseStorage.getInstance().getReference().child("img_chat")
                val reference = FirebaseDatabase.getInstance().reference
                val idMensaje = reference.push().key
                val filePath = storageReference.child("$idMensaje.jpg")

                val actualizarTarea: StorageTask<*>
                actualizarTarea = filePath.putFile(img!!)
                actualizarTarea.continueWithTask(com.google.android.gms.tasks.Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation filePath.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        cargando.dismiss()
                        val downloadUrl = task.result
                        val url = downloadUrl.toString()


                        mensajes = Mensajes(
                            idMensaje!!,
                            firebaseUser.uid,
                            id_empleado!!,
                            "Imagen enviada",
                            url,
                            false
                        )

                        CambioTurnoFirebase().addMessageFirebase(mensajes, id_empleado!!)
                        Toast.makeText(this, "Imagen enviada", Toast.LENGTH_SHORT).show()


                    }
                }


            } else {
                Toast.makeText(this, "cancelada seleccion", Toast.LENGTH_SHORT).show()
            }
        })

    private fun mensajeVisto(idReceptor: String) {
        val refMensajes = FirebaseDatabase.getInstance().getReference("mensajes")

        vistoListener = refMensajes.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (mensajeSnapshot in snapshot.children) {
                    val mensaje = mensajeSnapshot.getValue(Mensajes::class.java)
                    if (mensaje!!.id_receptor == firebaseUser.uid && mensaje.id_emisor == idReceptor) {
                        val hashMap = HashMap<String, Any>()
                        hashMap["visto"] = true
                        CambioTurnoFirebase().updateMensajesFirebase(mensaje, hashMap)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MessageActivity", "Error al recuperar los mensajes: ${error.message}")
            }

        })
    }

    private val permisoGaleria = registerForActivityResult(ActivityResultContracts.RequestPermission()){ concedido ->
        if (concedido){
            openGallery()
            Toast.makeText(this, "Tiene permiso", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "No tiene permiso", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        vistoListener?.let {
            FirebaseDatabase.getInstance().getReference("mensajes").removeEventListener(it)
        }
        val hashMap = HashMap<String, Any>()
        hashMap["estado_empleado"] = "offline"
        CambioTurnoFirebase().updateEmpleadoFirebase(hashMap)
    }

    override fun onResume() {
        super.onResume()
        val hashMap = HashMap<String, Any>()
        hashMap["estado_empleado"] = "online"

        CambioTurnoFirebase().updateEmpleadoFirebase(hashMap)
    }

}