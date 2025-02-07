package ui.profile

import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import bbdd.CambioTurnoFirebase
import com.bumptech.glide.Glide
import com.example.kamtur.R
import com.example.kamtur.databinding.ActivityEditImageProfileBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.kamtur.modelo.Empleados

class EditImageProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityEditImageProfileBinding
    lateinit var empleados: Empleados
    lateinit var firebaseAuth: FirebaseAuth
    val idEmpleado = FirebaseAuth.getInstance().currentUser?.uid

    private var imgUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditImageProfileBinding.inflate(layoutInflater)
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
        binding.btnCambiarImagen.setOnClickListener {
            showDialog()
        }

        binding.btnActualizarImagen.setOnClickListener {
            validarImg()
        }
    }

    private fun initUI() {
        CambioTurnoFirebase().getEmpleadoFirebase(idEmpleado) { datosEmpleado ->
            datosEmpleado?.let {
                empleados = it
                Glide.with(this).load(empleados.foto_empleado).placeholder(R.drawable.ic_user)
                    .into(binding.ivProfileUpdate)
            }
        }
    }

    // Dialogo para seleccionar imagen y comprobar permisos
    private fun showDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_change_image)

        val btnCamara = dialog.findViewById<Button>(R.id.btnCamara)
        val btnGaleria = dialog.findViewById<Button>(R.id.btnGaleria)

        btnGaleria.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                showGallery()
                dialog.dismiss()
            }else  permisoGaleria.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
        }

        btnCamara.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                showcamera()
                dialog.dismiss()
            }else  permisoCamara.launch(android.Manifest.permission.CAMERA)
        }
        dialog.show()
    }

    private fun showGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        openGalleryForResult.launch(intent)
    }

    private val openGalleryForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { resultado ->
            if (resultado.resultCode == RESULT_OK) {
                val data = resultado.data
                imgUri = data?.data
                binding.ivProfileUpdate.setImageURI(imgUri)
            } else {
                Toast.makeText(this, "cancelada seleccion", Toast.LENGTH_SHORT).show()
            }
        })

    private val permisoGaleria = registerForActivityResult(ActivityResultContracts.RequestPermission()){ concedido ->
        if (concedido){
            showGallery()
            Toast.makeText(this, "Tiene permiso", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "No tiene permiso", Toast.LENGTH_SHORT).show()
        }
    }

    private val permisoCamara = registerForActivityResult(ActivityResultContracts.RequestPermission()){ concedido ->
        if (concedido){
            showcamera()
            Toast.makeText(this, "Tiene permiso", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "No tiene permiso", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showcamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Titulo")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Descripcion")
        imgUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri)

        openCameraForResult.launch(intent)
    }

    private val openCameraForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == RESULT_OK) {
            binding.ivProfileUpdate.setImageURI(imgUri)
        } else {
            Toast.makeText(this, "cancelada captura", Toast.LENGTH_SHORT).show()
        }

    }

    private fun validarImg() {

        if (imgUri == null) {
            Toast.makeText(this, "No hay imagen", Toast.LENGTH_SHORT).show()
        } else {
            uploadImage()
        }
    }

    private fun uploadImage() {
        val options = arrayOf<CharSequence>("Actualizar imagen Perfil", "Actualizar imagen Fondo", "Cancelar")
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("¿Qué quieres hacer?")
        builder.setItems(options, DialogInterface.OnClickListener { dialogInterface, i ->
            if (i == 0) {
                uploadFirebaseStorage("profile_employee")
            }
            if (i == 1) {
                uploadFirebaseStorage("ImagenFondo")
            }
        })
        builder.show()


    }

    private fun uploadFirebaseStorage(imgUpdate: String){
        firebaseAuth = FirebaseAuth.getInstance()
        val pathImg = "$imgUpdate/" + firebaseAuth.currentUser?.uid
        val storageReference = FirebaseStorage.getInstance().getReference(pathImg)
        storageReference.putFile(imgUri!!).addOnSuccessListener { task ->
            val uriTask: Task<Uri> = task.storage.downloadUrl
            while (!uriTask.isSuccessful);
            val imageUrl = "${uriTask.result}"
            Toast.makeText(this, "Imagen Actualizada", Toast.LENGTH_SHORT).show()

            uploadImageFirebase(imgUpdate, imageUrl)

        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error al subir imagen por: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageFirebase(imgUpdate: String, imageUrl: String) {
    if(imgUpdate == "profile_employee"){
        val hashMap = HashMap<String, Any>()
        hashMap["foto_empleado"] = imageUrl

        CambioTurnoFirebase().updateEmpleadoFirebase(hashMap)

        intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }else {
        val hashMap = HashMap<String, Any>()
        hashMap["foto_portada"] = imageUrl
        CambioTurnoFirebase().updateEmpleadoFirebase(hashMap)

        intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
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