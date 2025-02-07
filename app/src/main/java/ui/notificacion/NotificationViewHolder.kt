package ui.notificacion

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.kamtur.databinding.ItemNotificationBinding
import com.kamtur.modelo.Notificaciones

class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val binding = ItemNotificationBinding.bind(view)

    fun bind(notification: Notificaciones) {
        if (notification.leida == 0) {
            binding.ivCartaNoLeida.visibility = View.VISIBLE
            binding.ivCartaLeida.visibility = View.GONE
        } else {
            binding.ivCartaNoLeida.visibility = View.GONE
            binding.ivCartaLeida.visibility = View.VISIBLE
        }

        binding.tvDescripcion.text = notification.mensaje
    }
}