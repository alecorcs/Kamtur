package ui.notificacion

import android.app.AlertDialog
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bbdd.CambioTurnoFirebase
import com.example.kamtur.R
import com.kamtur.modelo.Notificaciones
import ui.calendar.YourCalendarActivity
import ui.turno.TurnoActivity

class NotificationAdapter(private var notificaciones: MutableList<Notificaciones>): RecyclerView.Adapter<NotificationViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notificaciones[position])
        val context = holder.itemView.context

        holder.itemView.setOnClickListener {
            when(notificaciones[position].mensaje){
                "Se ha realizado una propuesta de cambio" -> {
                    val hashmap = hashMapOf<String, Any>()
                    hashmap["leida"] = 1
                    CambioTurnoFirebase().updateNotificacionFirebase(notificaciones[position], hashmap)

                    val intent = android.content.Intent(context, TurnoActivity::class.java)
                    context.startActivity(intent)
                }
                "Tu cambio ha sido aceptado" -> {
                    val hashmap = hashMapOf<String, Any>()
                    hashmap["leida"] = 1
                    CambioTurnoFirebase().updateNotificacionFirebase(notificaciones[position], hashmap)

                    val intent = android.content.Intent(context, YourCalendarActivity::class.java)
                    context.startActivity(intent)
                }
                "Tu propuesta ha sido aceptada" -> {
                    val hashmap = hashMapOf<String, Any>()
                    hashmap["leida"] = 1
                    CambioTurnoFirebase().updateNotificacionFirebase(notificaciones[position], hashmap)
                    val intent = android.content.Intent(context, YourCalendarActivity::class.java)
                    context.startActivity(intent)
                }
                "Tu propuesta ha sido rechazada" -> {
                    val hashmap = hashMapOf<String, Any>()
                    hashmap["leida"] = 1
                    CambioTurnoFirebase().updateNotificacionFirebase(notificaciones[position], hashmap)
                    val intent = android.content.Intent(context, YourCalendarActivity::class.java)
                    context.startActivity(intent)
                }
            }
        }

        holder.itemView.setOnLongClickListener{
            val options = arrayOf<CharSequence>("Eliminar notificación", "Cancelar")
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder.setTitle("¿Qué quieres hacer?")
            builder.setItems(options, DialogInterface.OnClickListener { dialogInterface, i ->
                if (i == 0) {
                    CambioTurnoFirebase().removeNotificacionFirebase(notificaciones[position])
                    notificaciones.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, notificaciones.size)
                }
            })
            builder.show()
            true
        }
    }

    override fun getItemCount() = notificaciones.size
}