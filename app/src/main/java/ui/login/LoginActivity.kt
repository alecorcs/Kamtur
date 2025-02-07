package ui.login

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kamtur.R
import com.example.kamtur.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import ui.calendar.YourCalendarActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var cargando : ProgressDialog
    var firebaseUser : FirebaseUser? = null



    override fun onStart() {
        checkSession()
        super.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
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

        binding.tvOlvidar.setOnClickListener {

            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
        binding.btInicioSesion.setOnClickListener {
            if(binding.etEmail.text.toString().isNotEmpty() && binding.etPassword.text.toString().isNotEmpty()){

                cargando.setMessage("Espere un momento")
                cargando.show()
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(binding.etEmail.text.toString(),
                        binding.etPassword.text.toString()).addOnCompleteListener {
                            if (it.isSuccessful) {
                                cargando.dismiss()
                                Toast.makeText(this, "Bienvenido Usuario", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, YourCalendarActivity::class.java)
                                startActivity(intent)
                            } else {
                                cargando.dismiss()
                                Toast.makeText(this, "Usuario no registrado", Toast.LENGTH_SHORT).show()
                            }
                    }.addOnFailureListener {
                        cargando.dismiss()
                        Toast.makeText(this, "No se ha podido iniciar sesión, pruebe más tarde", Toast.LENGTH_SHORT).show()
                    }
            }else {
                Toast.makeText(this, "Debes rellenar todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvRegistrarse.setOnClickListener {
           val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initUI() {
        cargando = ProgressDialog(this)
        cargando.setTitle("Iniciando Sesion")
        cargando.setCanceledOnTouchOutside(false)

    }

    private fun checkSession() {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            val intent = Intent(this, YourCalendarActivity::class.java)
            Toast.makeText(this, "Sesión Activa", Toast.LENGTH_SHORT).show()
            startActivity(intent)
            finish()
        }
    }
}