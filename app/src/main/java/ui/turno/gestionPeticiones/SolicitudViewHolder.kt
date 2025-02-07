package ui.turno.gestionPeticiones

import android.view.View
import androidx.core.app.ActivityCompat.recreate
import androidx.recyclerview.widget.RecyclerView
import bbdd.CambioTurnoFirebase
import com.bumptech.glide.Glide
import com.example.kamtur.R
import com.example.kamtur.databinding.ItemSolicitudesBinding
import com.kamtur.modelo.HistorialCambios
import com.kamtur.modelo.Notificaciones
import com.kamtur.modelo.SolicitudCambioTurno
import ui.turno.TurnoActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SolicitudViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val binding = ItemSolicitudesBinding.bind(view)

    fun bind(solicitud: SolicitudCambioTurno) {
        val idInteresado = solicitud.id_interesado

        CambioTurnoFirebase().getEmpleadoFirebase(idInteresado) { empleado ->
            if (empleado != null) {
                binding.tvNombreEmpleado.text = empleado.nombre_empleado

                Glide.with(binding.ivImgEmpleado.context).load(empleado.foto_empleado)
                    .placeholder(R.drawable.ic_item_empleado).into(binding.ivImgEmpleado)
            }
        }

        CambioTurnoFirebase().getTurnosEmpleadoFirebase(idInteresado) { turnos ->
            if (turnos.isNotEmpty()) {
                val turnoInteresado = turnos.find { it.id_turno == solicitud.id_turno_interesado }
                if (turnoInteresado != null) {
                    binding.tvFechaCambio.text = turnoInteresado.fecha_turno
                    binding.tvNombreTurno.text = turnoInteresado.nombre_turno
                }
            }
        }

        binding.btnAceptarCambio.setOnClickListener {
            val date = LocalDate.now()
            val formato = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val fechaAprobado = formato.format(date)

            val hashmapSolicitud = HashMap<String, Any>()
            hashmapSolicitud["estado"] = "aceptado"
            CambioTurnoFirebase().updateSolicitudFirebase(solicitud, hashmapSolicitud)

            val hashmapCambio = HashMap<String, Any>()
            hashmapCambio["estado"] = "cambio realizado"
            hashmapCambio["fecha_aprobado"] = fechaAprobado
            CambioTurnoFirebase().updateCambioFirebase(solicitud.id_cambio, hashmapCambio)

            CambioTurnoFirebase().updateTurno(solicitud)
            enviarNotificacionSolicitante(solicitud)
            enviarNotificacionInteresado(solicitud)
            addHistorialCambio(solicitud)

            val intent = android.content.Intent(itemView.context, TurnoActivity::class.java)
            itemView.context.startActivity(intent)
        }

        binding.btnRechazarCambio.setOnClickListener {
            val hashmapSolicitud = HashMap<String, Any>()
            hashmapSolicitud["estado"] = "rechazado"
            CambioTurnoFirebase().updateSolicitudFirebase(solicitud, hashmapSolicitud)
            enviarNotificacionInteresadoRechazado(solicitud)

            recreate(this.itemView.context as GestionCambioActivity)
        }

    }

    private fun enviarNotificacionSolicitante(solicitud: SolicitudCambioTurno){
        val date = LocalDate.now()
        val formato = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val fechaNotificacion = formato.format(date)

        //Buscamos los cambios realizados
        CambioTurnoFirebase().getCambioFirebase("cambio realizado") { cambios ->
            //Buscamos el cambio recien aceptado en la lista de cambios
            val cambioSolicitado = cambios.find { it.id_cambio == solicitud.id_cambio }
            //Si los cambios no son nulos
            if (cambioSolicitado != null) {
                val notificacion = Notificaciones(
                    "",
                    cambioSolicitado.id_solicitante,
                    solicitud.id_cambio,
                    fechaNotificacion,
                    0,
                    "Tu cambio ha sido aceptado"
                )

                CambioTurnoFirebase().addNotificacionFirebase(notificacion)
            }
        }
    }

    private fun enviarNotificacionInteresado(solicitud: SolicitudCambioTurno){
        val date = LocalDate.now()
        val formato = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val fechaNotificacion = formato.format(date)

        val notificacion = Notificaciones(
            "",
            solicitud.id_interesado,
            solicitud.id_cambio,
            fechaNotificacion,
            0,
            "Tu propuesta ha sido aceptada"
        )

        CambioTurnoFirebase().addNotificacionFirebase(notificacion)
    }
    private fun enviarNotificacionInteresadoRechazado(solicitud: SolicitudCambioTurno){
        val date = LocalDate.now()
        val formato = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val fechaNotificacion = formato.format(date)
        val notificacion = Notificaciones(
            "",
            solicitud.id_interesado,
            solicitud.id_cambio,
            fechaNotificacion,
            0,
            "Tu propuesta ha sido rechazada"
        )
        CambioTurnoFirebase().addNotificacionFirebase(notificacion)
    }

    private fun addHistorialCambio(solicitud: SolicitudCambioTurno) {
        val date = LocalDate.now()
        val formato = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val fechaAceptado = formato.format(date)

        val historialCambios = HistorialCambios(
            "",
            solicitud.id_cambio,
            solicitud.id_solicitud,
            fechaAceptado
        )
        CambioTurnoFirebase().addHistorialCambioFirebase(historialCambios)
    }
}