package ui.chat.message

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.kamtur.modelo.Mensajes

class MensajeAdapter(
    val context: Context,
    private var mensajes: MutableList<Mensajes> = mutableListOf(),
    private val img: String
) : RecyclerView.Adapter<MensajeViewHolder>() {

    private val firebaseUser = FirebaseAuth.getInstance().currentUser!!


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MensajeViewHolder {
        return if(viewType == 1) {
            val view =
                LayoutInflater.from(context).inflate(com.example.kamtur.R.layout.item_conver_emisor, parent, false)
            MensajeViewHolder(view)
        }else {
            val view = LayoutInflater.from(context).inflate(com.example.kamtur.R.layout.item_conver_receptor, parent, false)
            MensajeViewHolder(view)
        }

    }

    override fun onBindViewHolder(holder: MensajeViewHolder, position: Int) {
        holder.bind(mensajes[position], context, img)

        if (position == mensajes.size - 1) {
            //Mensaje visto o enviado
            if (mensajes[position].visto) {
                holder.tvVisto!!.text = "Visto"
                if (mensajes[position].mensaje == "Imagen enviada" && mensajes[position].url.isNotEmpty()) {
                    val layoutParams: RelativeLayout.LayoutParams =
                        holder.tvVisto!!.layoutParams as RelativeLayout.LayoutParams
                    layoutParams.setMargins(0, 245, 10, 0)
                    holder.tvVisto!!.layoutParams = layoutParams

                }
            } else {
                holder.tvVisto!!.text = "Enviado"
                if (mensajes[position].mensaje == "Imagen enviada" && mensajes[position].url.isNotEmpty()) {
                    val layoutParams: RelativeLayout.LayoutParams =
                        holder.tvVisto!!.layoutParams as RelativeLayout.LayoutParams
                    layoutParams.setMargins(0, 245, 10, 0)
                    holder.tvVisto!!.layoutParams = layoutParams
                }
            }
        } else holder.tvVisto!!.visibility = RelativeLayout.GONE
    }

    override fun getItemCount() = mensajes.size

    override fun getItemViewType(position: Int): Int {
        return if (mensajes[position].id_emisor == firebaseUser.uid) {
            1
        }else 0
    }
}

