package ui.turno.gestionPeticiones

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kamtur.R
import com.kamtur.modelo.SolicitudCambioTurno

class SolicitudAdapter(private val listaSolicitudes: List<SolicitudCambioTurno>): RecyclerView.Adapter<SolicitudViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SolicitudViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_solicitudes, parent, false)
        return SolicitudViewHolder(view)
    }

    override fun onBindViewHolder(holder: SolicitudViewHolder, position: Int) {
        holder.bind(listaSolicitudes[position])
    }

    override fun getItemCount() = listaSolicitudes.size

}