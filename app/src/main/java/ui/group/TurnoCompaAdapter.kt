package ui.group


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kamtur.R
import com.kamtur.modelo.TurnosEmpleados

class TurnoCompaAdapter(var turnos: MutableList<TurnosEmpleados> = mutableListOf()) :
    RecyclerView.Adapter<TurnoCompaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TurnoCompaViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_tasks_day, parent, false)
        return TurnoCompaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TurnoCompaViewHolder, position: Int) {
        holder.bind(turnos[position])
    }

    override fun getItemCount() = turnos.size

}