package ui.calendar

import android.app.AlertDialog
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bbdd.CambioTurnoFirebase
import com.example.kamtur.R
import com.kamtur.modelo.TurnosEmpleados

class TurnoAdapter(var turnos: MutableList<TurnosEmpleados> = mutableListOf()) :
    RecyclerView.Adapter<TurnoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TurnoViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_tasks_day, parent, false)
        return TurnoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TurnoViewHolder, position: Int) {
        holder.bind(turnos[position])
        val context = holder.itemView.context

        holder.itemView.setOnLongClickListener{
            val options = arrayOf<CharSequence>("Eliminar turno", "Cancelar")
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder.setTitle("¿Qué quieres hacer?")
            builder.setItems(options, DialogInterface.OnClickListener { _, i ->
                if (i == 0) {
                    CambioTurnoFirebase().removeTurnFirebase(turnos[position])
                    turnos.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, turnos.size)
                }
            })
            builder.show()
            true
        }

    }

    override fun getItemCount() = turnos.size


}