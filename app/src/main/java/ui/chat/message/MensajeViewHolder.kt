package ui.chat.message

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import bbdd.CambioTurnoFirebase
import com.bumptech.glide.Glide
import com.example.kamtur.R
import com.google.firebase.auth.FirebaseAuth
import com.kamtur.modelo.Mensajes

class MensajeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val firebaseUser = FirebaseAuth.getInstance().currentUser!!

    var imgPerfil: ImageView? = null
    var tvMensaje: TextView? = null
    var imgEnviadaEmisor: ImageView? = null
    var imgEnviadaReceptor: ImageView? = null
    var tvVisto: TextView? = null


    fun bind(mensaje: Mensajes, context: Context, img: String) {

        imgPerfil = itemView.findViewById(R.id.imgPerfil)
        tvMensaje = itemView.findViewById(R.id.tvMensaje)
        imgEnviadaEmisor = itemView.findViewById(R.id.imgEnviadaEmisor)
        imgEnviadaReceptor = itemView.findViewById(R.id.imgEnviadaReceptor)
        tvVisto = itemView.findViewById(R.id.tvVisto)

        Glide.with(context).load(img).placeholder(R.drawable.ic_profile_message).into(imgPerfil!!)

        // Emisor
        if (mensaje.id_emisor == firebaseUser.uid) {
            if (mensaje.mensaje == "Imagen enviada" && mensaje.url.isNotEmpty()) {
                // Mostrar solo la imagen
                tvMensaje!!.visibility = View.GONE
                imgEnviadaEmisor!!.visibility = View.VISIBLE
                Glide.with(context).load(mensaje.url).placeholder(R.drawable.ic_imagen)
                    .into(imgEnviadaEmisor!!)

                imgEnviadaEmisor!!.setOnClickListener {
                    val options = arrayOf<CharSequence>("Eliminar imagen", "Cancelar")
                    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                    builder.setTitle("¿Qué quieres hacer?")
                    builder.setItems(options, DialogInterface.OnClickListener{ dialogInterface, i ->
                        if (i == 0){
                            CambioTurnoFirebase().removeMensajesFirebase(mensaje)
                        }
                    })
                    builder.show()
                }
            } else {
                // Mostrar solo el mensaje
                tvMensaje!!.visibility = View.VISIBLE
                tvMensaje!!.text = mensaje.mensaje
                imgEnviadaEmisor!!.visibility = View.GONE

                tvMensaje!!.setOnClickListener {
                    val options = arrayOf<CharSequence>("Eliminar mensaje", "Cancelar")
                    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                    builder.setTitle("¿Qué quieres hacer?")
                    builder.setItems(options, DialogInterface.OnClickListener { dialogInterface, i ->
                        if (i == 0) {
                            CambioTurnoFirebase().removeMensajesFirebase(mensaje)
                        }
                    })
                    builder.show()
                }
            }

        }
        // Receptor
        else {
            if (mensaje.mensaje == "Imagen enviada" && mensaje.url.isNotEmpty()) {
                // Mostrar solo imagen
                tvMensaje!!.visibility = View.GONE
                imgEnviadaReceptor!!.visibility = View.VISIBLE
                Glide.with(context).load(mensaje.url).placeholder(R.drawable.ic_imagen)
                    .into(imgEnviadaReceptor!!)

            } else {
                // Mostrar solo el mensaje
                tvMensaje!!.visibility = View.VISIBLE
                tvMensaje!!.text = mensaje.mensaje
                imgEnviadaReceptor!!.visibility = View.GONE

            }
        }
    }
}