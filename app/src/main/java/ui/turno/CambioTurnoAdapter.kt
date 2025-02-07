package ui.turno

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kamtur.R
import com.google.firebase.auth.FirebaseAuth
import com.kamtur.modelo.CambioTurno
import ui.turno.gestionPeticiones.GestionCambioActivity

class CambioTurnoAdapter(private var cambios: MutableList<CambioTurno>) : RecyclerView.Adapter<CambioTurnoViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CambioTurnoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cambio_turno, parent, false)
        return CambioTurnoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CambioTurnoViewHolder, position: Int) {
        holder.bind(cambios[position])

        val context = holder.itemView.context

        val firebaseAuth = FirebaseAuth.getInstance()
        if (cambios[position].id_solicitante == firebaseAuth.currentUser?.uid) {

            holder.itemView.setOnClickListener {
                val intent = Intent(context, GestionCambioActivity::class.java)
                intent.putExtra("id_solicitante", cambios[position].id_solicitante)
                intent.putExtra("id_cambio", cambios[position].id_cambio)
                intent.putExtra("id_turno_solicitante", cambios[position].id_turno_solicitante)
                context.startActivity(intent)
            }

        }

    }

    override fun getItemCount() = cambios.size

}