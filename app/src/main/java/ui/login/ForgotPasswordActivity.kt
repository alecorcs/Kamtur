package ui.login

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kamtur.R
import com.example.kamtur.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
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
        binding.btnEnviarCorreo.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if(email.isEmpty()){
                Toast.makeText(this, "Debes escribir el correo", Toast.LENGTH_SHORT).show()
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(this, "Correo no válido", Toast.LENGTH_SHORT).show()
            }else recoverPassword(email)
        }
    }

    private fun initUI() {
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Recuperando contraseña")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
    }

    private fun recoverPassword(email: String) {
        progressDialog.setMessage("Enviando email con instrucciones a ${email}")
        progressDialog.show()

        firebaseAuth.sendPasswordResetEmail(email).addOnSuccessListener {
            Toast.makeText(this, "Correo enviado", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            progressDialog.dismiss()
        }.addOnFailureListener {
            Toast.makeText(this, "No se pudo enviar el correo", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
        }

    }

}