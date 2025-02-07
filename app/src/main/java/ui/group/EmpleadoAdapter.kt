package ui.group

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kamtur.R
import com.kamtur.modelo.Empleados

class EmpleadoAdapter(var empleados: MutableList<Empleados> = mutableListOf()) :
    RecyclerView.Adapter<EmpleadoViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmpleadoViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_empleado, parent, false)
        return EmpleadoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmpleadoViewHolder, position: Int) {
        holder.bind(empleados[position])
        val context = holder.itemView.context


        holder.itemView.setOnClickListener {
            val intent = Intent(context, MembersActivity::class.java)
            intent.putExtra("idEmpleado", empleados[position].id_empleado)
            intent.putExtra("nombreEmpleado", empleados[position].nombre_empleado)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = empleados.size
}