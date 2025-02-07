package ui.group

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.kamtur.databinding.ItemTasksDayBinding
import com.kamtur.modelo.TurnosEmpleados

class TurnoCompaViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    private val binding = ItemTasksDayBinding.bind(view)

    fun bind(turno: TurnosEmpleados) {
        binding.tvTurnoDate.text = turno.fecha_turno
        binding.tvTurnoDescription.text = turno.nombre_turno
    }

}