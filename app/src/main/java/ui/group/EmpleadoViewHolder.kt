package ui.group

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kamtur.R
import com.example.kamtur.databinding.ItemEmpleadoBinding
import com.kamtur.modelo.Empleados

class EmpleadoViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    private val binding = ItemEmpleadoBinding.bind(view)

    fun bind(empleado: Empleados) {
        Glide.with(this.itemView).load(empleado.foto_empleado).placeholder(R.drawable.ic_item_empleado).into(binding.ivImgEmpleado)
        binding.tvNombreEmpleado.text = empleado.nombre_empleado
    }
}