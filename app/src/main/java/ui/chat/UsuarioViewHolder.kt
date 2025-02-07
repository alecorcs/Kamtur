package ui.chat

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import bbdd.CambioTurnoFirebase
import com.bumptech.glide.Glide
import com.example.kamtur.R
import com.example.kamtur.databinding.ItemEmpleadoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kamtur.modelo.Empleados
import com.kamtur.modelo.Mensajes


class UsuarioViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    private val binding = ItemEmpleadoBinding.bind(view)
    var ulimoMensaje: String = "No hay Mensaje"


    fun bind(empleado: Empleados, chatLeido: Boolean) {
        Glide.with(this.itemView).load(empleado.foto_empleado).placeholder(R.drawable.ic_item_empleado).into(binding.ivImgEmpleado)
        binding.tvNombreEmpleado.text = empleado.nombre_empleado

        if (chatLeido){
            getUltimoMensaje(empleado)
        }else binding.tvUltimoMensaje.visibility = View.GONE

        if(chatLeido){
            if(empleado.estado_empleado == "online"){
                binding.tvEnLinea.visibility = View.VISIBLE
            }else{
                binding.tvEnLinea.visibility = View.GONE
            }

        }else {
            binding.tvEnLinea.visibility = View.GONE
        }


    }

    private fun getUltimoMensaje(empleado: Empleados) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val idReceptor = empleado.id_empleado
        val idEmisor = firebaseUser.uid
        val refMensajes = FirebaseDatabase.getInstance().getReference("mensajes")

        refMensajes.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (mensajeSnapshot in snapshot.children) {
                    val mensaje = mensajeSnapshot.getValue(Mensajes::class.java)
                    if (mensaje != null){
                        if (mensaje.id_receptor == idEmisor && mensaje.id_emisor == idReceptor
                            || mensaje.id_receptor == idReceptor && mensaje.id_emisor == idEmisor) {
                            ulimoMensaje = mensaje.mensaje

                        }
                    }
                }
                when(ulimoMensaje){
                    "No hay Mensaje" -> binding.tvUltimoMensaje.text = "No hay Mensaje"
                    "Imagen enviada" -> binding.tvUltimoMensaje.text = "Imagen enviada"
                    else -> binding.tvUltimoMensaje.text = ulimoMensaje
                }
                ulimoMensaje = "No hay Mensaje"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al recuperar los mensajes: ${error.message}")

            }

        })
    }
}