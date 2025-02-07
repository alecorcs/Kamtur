package ui.chat

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.kamtur.R
import com.kamtur.modelo.Empleados
import ui.chat.message.MessageActivity

class UsuarioAdapter(var empleados: MutableList<Empleados> = mutableListOf(), var leido: Boolean) :
    RecyclerView.Adapter<UsuarioViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_empleado, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        holder.bind(empleados[position], leido)
        val context = holder.itemView.context


        holder.itemView.setOnClickListener {
            val intent = Intent(context, MessageActivity::class.java)
            intent.putExtra("id_empleado", empleados[position].id_empleado)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = empleados.size
}