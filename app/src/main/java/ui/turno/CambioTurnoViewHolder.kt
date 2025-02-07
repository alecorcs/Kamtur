package ui.turno

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import bbdd.CambioTurnoFirebase
import com.bumptech.glide.Glide
import com.example.kamtur.R
import com.example.kamtur.databinding.ItemCambioTurnoBinding
import com.google.firebase.auth.FirebaseAuth
import com.kamtur.modelo.CambioTurno
import com.kamtur.modelo.Notificaciones
import com.kamtur.modelo.SolicitudCambioTurno
import com.kamtur.modelo.TurnosEmpleados
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class CambioTurnoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val binding = ItemCambioTurnoBinding.bind(view)
    private lateinit var dialog: Dialog
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var notificacion: Notificaciones
    private lateinit var solicitudCambioTurno: SolicitudCambioTurno
    private var turnoSeleccionado: String? = null

    private var turnoEncontrado = false
    private var turnosEncontrados: MutableList<TurnosEmpleados> = mutableListOf()

    fun bind(cambioTurno: CambioTurno){

        val idSolicitante = cambioTurno.id_solicitante

         CambioTurnoFirebase().getEmpleadoFirebase(idSolicitante){ empleado ->
             if (empleado != null){
                 binding.tvNombreEmpleado.text = empleado.nombre_empleado

                 Glide.with(binding.ivImgEmpleado.context).load(empleado.foto_empleado).placeholder(
                     R.drawable.ic_item_empleado).into(binding.ivImgEmpleado)
             }
         }

        CambioTurnoFirebase().getTurnosEmpleadoFirebase(idSolicitante){ turnos ->
            if (turnos.isNotEmpty()){
                val turnoSolicitante = turnos.find { it.id_turno == cambioTurno.id_turno_solicitante }
                if (turnoSolicitante != null){
                    binding.tvFechaCambio.text = turnoSolicitante.fecha_turno
                    binding.tvNombreTurno.text = turnoSolicitante.nombre_turno
                }
            }
        }

        CambioTurnoFirebase().getSolicitudFirebase(cambioTurno.id_cambio) { solicitudes ->
            if (solicitudes.isNotEmpty()) {
                binding.tvEstado.visibility = View.VISIBLE
            }else binding.tvEstado.visibility = View.GONE
        }

        binding.btnCambio.setOnClickListener{
            val context = itemView.context
            dialog = Dialog(context)
            dialog.setContentView(R.layout.dialog_change_proposition)
            proponerCambio(cambioTurno, context)
            dialog.setCanceledOnTouchOutside(true)
        }
    }

    private fun proponerCambio(cambioTurno: CambioTurno, context: Context){
        val calendarView: CalendarView = dialog.findViewById(R.id.calendarView)
        //val tvNombreTurno: TextView = dialog.findViewById(R.id.tvNombreTurno)
        val spinTurno: Spinner = dialog.findViewById(R.id.spinTurnos)
        val proponer: Button = dialog.findViewById(R.id.btnPrponerCambio)
        val nombreTurnos: MutableList<String> = mutableListOf()



        firebaseAuth = FirebaseAuth.getInstance()

        if(firebaseAuth.currentUser!!.uid != cambioTurno.id_solicitante){
            calendarView.setOnDateChangeListener { _, dayOfMonth, month, year ->
                //turnoEncontrado = false
                val selectedDate = Calendar.getInstance().apply {
                    set(dayOfMonth, month, year)
                }
                val seletedDateString = dateToString(selectedDate)
                Log.d("YourCalendarActivity", "Fecha seleccionada: $seletedDateString")


                CambioTurnoFirebase().getTurnosEmpleadoFirebase(firebaseAuth.currentUser!!.uid) { turnos ->
                    if (turnos.isNotEmpty()) {
                        nombreTurnos.clear()
                        turnosEncontrados.clear()
                        turnoEncontrado = false

                        for (turno in turnos) {
                            if (turno.fecha_turno == seletedDateString) {
                                nombreTurnos.add(turno.nombre_turno)
                                turnosEncontrados.add(turno)
                                turnoEncontrado = true
                            }
                        }
                        if (!turnoEncontrado) {
                            nombreTurnos.add("No hay turnos")
                        }
                    }else {
                        Toast.makeText(context, "Primero debes añadir turnos a tu calendario", Toast.LENGTH_SHORT).show()
                    }

                    turnoSeleccionado = nombreTurnos[0]

                    // Crear un ArrayAdapter usando el array de strings y un layout de spinner predeterminado
                    val adapter = ArrayAdapter(context, R.layout.item_spinner, nombreTurnos)
                    // Especificar el layout a usar cuando la lista de opciones aparezca
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    // Aplicar el adapter al spinner
                    spinTurno.adapter = adapter
                }
            }


            spinTurno.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    turnoSeleccionado = nombreTurnos[position]

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Por si no se seleciona nada
                }
            }

            proponer.setOnClickListener {
                for (turno in turnosEncontrados){
                    if (turno.nombre_turno == turnoSeleccionado){
                        solicitudCambioTurno = SolicitudCambioTurno(
                            "",
                            firebaseAuth.currentUser!!.uid,
                            turno.id_turno,
                            cambioTurno.id_cambio,
                            "pendiente"
                        )
                    }
                    break
                }
                CambioTurnoFirebase().addSolicitudFirebase(solicitudCambioTurno)
                enviarNotificacion(cambioTurno)
                dialog.dismiss()
            }
            dialog.show()

        }else{
            val dialog: AlertDialog.Builder = AlertDialog.Builder(context)
            dialog.setTitle("Error")
            dialog.setMessage("No puedes proponer un cambio a ti mismo")
            dialog.setPositiveButton("Aceptar", null)
            dialog.show()
        }



    }
    private fun dateToString(calendar: Calendar): String {
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        return "$day/$month/$year"
    }

    private fun enviarNotificacion(cambioTurno: CambioTurno){
        val date = LocalDate.now()
        val formato = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val fechaNotificacion = formato.format(date)

        notificacion = Notificaciones(
            "",
            cambioTurno.id_solicitante,
            cambioTurno.id_cambio,
            fechaNotificacion,
            0,
            "Se ha realizado una propuesta de cambio"
        )

        CambioTurnoFirebase().addNotificacionFirebase(notificacion)
    }
}