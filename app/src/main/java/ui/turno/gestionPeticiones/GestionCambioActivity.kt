package ui.turno.gestionPeticiones

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import bbdd.CambioTurnoFirebase
import com.bumptech.glide.Glide
import com.example.kamtur.R
import com.example.kamtur.databinding.ActivityGestionCambioBinding
import com.kamtur.modelo.CambioTurno
import ui.turno.TurnoActivity

class GestionCambioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGestionCambioBinding
    private lateinit var solicitudAdapter: SolicitudAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGestionCambioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val idSolicitante = intent.getStringExtra("id_solicitante")
        val idCambio = intent.getStringExtra("id_cambio")
        val idTurnoSolicitante = intent.getStringExtra("id_turno_solicitante")

        initListeners(idCambio)
        initUI(idSolicitante, idCambio, idTurnoSolicitante)

    }


    private fun initListeners(idCambio: String?){
        binding.btnVolver.setOnClickListener {
            val intent = android.content.Intent(this, TurnoActivity::class.java)
            startActivity(intent)
        }
        binding.btnEliminar.setOnClickListener {

            if (idCambio != null) {
                CambioTurnoFirebase().getSolicitudFirebase(idCambio) { solicitudes ->
                    for (solicitud in solicitudes) {
                        CambioTurnoFirebase().removeSolicitudFirebase(solicitud)
                    }
                }

                CambioTurnoFirebase().getCambioFirebase("pendiente") { cambios ->
                    val cambioSeleccionado: CambioTurno? = cambios.find { it.id_cambio == idCambio }
                    CambioTurnoFirebase().removeCambioFirebase(cambioSeleccionado!!)
                    val intent = android.content.Intent(this, TurnoActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }
    private fun initUI(idSolicitante: String?,idCambio: String?, idTurnoSolicitante: String?) {


        if (idSolicitante != null) {
            CambioTurnoFirebase().getEmpleadoFirebase(idSolicitante) { empleado ->
                if (empleado != null) {
                    binding.tvNombreEmpleado.text = empleado.nombre_empleado

                    Glide.with(binding.ivImgEmpleado.context).load(empleado.foto_empleado)
                        .placeholder(R.drawable.ic_item_empleado).into(binding.ivImgEmpleado)
                }
            }

            CambioTurnoFirebase().getTurnosEmpleadoFirebase(idSolicitante) { turnos ->
                if (turnos.isNotEmpty()) {
                    val turnoSolicitante = turnos.find { it.id_turno == idTurnoSolicitante }
                    if (turnoSolicitante != null){
                        binding.tvFechaCambio.text = turnoSolicitante.fecha_turno
                        binding.tvNombreTurno.text = turnoSolicitante.nombre_turno
                    }
                }
            }

            if (idCambio != null) {
                CambioTurnoFirebase().getSolicitudFirebase(idCambio) { solicitudes ->
                    if (solicitudes.isNotEmpty()) {
                        val solicitudesPendientes = solicitudes.filter { it.estado == "pendiente" }

                        binding.rvSolicitudes.layoutManager = LinearLayoutManager(this)
                        solicitudAdapter = SolicitudAdapter(solicitudesPendientes)
                        binding.rvSolicitudes.adapter = solicitudAdapter
                        solicitudAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }
}