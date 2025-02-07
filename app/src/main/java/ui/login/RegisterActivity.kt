package ui.login

import android.app.ProgressDialog
import bbdd.CambioTurnoFirebase
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kamtur.R
import com.example.kamtur.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.kamtur.modelo.Empleados

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var cargando: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initialisers()
        initUI()
    }

    private fun initialisers() {
        auth = FirebaseAuth.getInstance()

        binding.btRegistrarUsuario.setOnClickListener {
            if (binding.etNombre.text.toString().isNotEmpty()
                && binding.etApellidos.text.toString().isNotEmpty()
                && binding.etEmailRegister.text.toString().isNotEmpty()
            ) {
                cargando.setMessage("Espere un momento")
                cargando.show()
                if (equalPasswords()) {
                    auth.createUserWithEmailAndPassword(
                        binding.etEmailRegister.text.toString(),
                        binding.etPasswordRegister.text.toString()
                    ).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(this, "Registro correcto", Toast.LENGTH_SHORT).show()
                            CambioTurnoFirebase().addEmpleadoFirebase(enviarEmpleado())
                            intent = android.content.Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                            cargando.dismiss()

                        } else {
                            Toast.makeText(this, "Se ha producido un error al registrar el usuario", Toast.LENGTH_SHORT).show()
                            cargando.dismiss()
                        }
                    }

                } else {
                    Toast.makeText(this, "Las contraseñas no coinciden o se necesitan más de 6 caracteres", Toast.LENGTH_SHORT).show()
                    cargando.dismiss()
                }

            } else {
               Toast.makeText(this, "Debes rellenar todos los campos", Toast.LENGTH_SHORT).show()
                cargando.dismiss()
            }
        }

    }
    private fun initUI() {
        cargando = ProgressDialog(this)
        cargando.setTitle("Registrando Usuario")
        cargando.setCanceledOnTouchOutside(false)
    }


    private fun equalPasswords(): Boolean {
        val password = binding.etPasswordRegister.text.toString()
        val repeatPassword = binding.etRepetirPassword.text.toString()
        return password.isNotEmpty() && repeatPassword.isNotEmpty() && password == repeatPassword && password.length >= 6
    }

    private fun enviarEmpleado(): Empleados {
        val id = auth.currentUser?.uid

        val empleados = Empleados(
            id ?: "",
            binding.etNombre.text.toString(),
            binding.etApellidos.text.toString(),
            null,
            null,
            null,
            binding.etEmailRegister.text.toString(),
            "offline",
            null
        )
        return empleados

    }


}
