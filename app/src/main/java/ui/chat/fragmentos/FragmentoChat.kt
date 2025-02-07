package ui.chat.fragmentos

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import bbdd.CambioTurnoFirebase
import com.example.kamtur.R
import com.example.kamtur.databinding.FragmentFragmentoChatBinding
import com.example.kamtur.databinding.FragmentFragmentoUsuarioBinding
import com.google.firebase.auth.FirebaseAuth
import com.kamtur.modelo.Conversaciones
import com.kamtur.modelo.Empleados
import ui.chat.UsuarioAdapter


class FragmentoChat : Fragment() {
    private lateinit var binding: FragmentFragmentoChatBinding
    private lateinit var usuarioAdapter: UsuarioAdapter

    private var empleadosService: MutableList<Empleados> = mutableListOf()
    private var empleadosConversacion: MutableList<Empleados> = mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFragmentoChatBinding.inflate(inflater, container, false)

        getConversaciones()

        usuarioAdapter = UsuarioAdapter(empleadosConversacion, true)
        binding.rvItemChat.layoutManager = LinearLayoutManager(requireContext())
        binding.rvItemChat.adapter = usuarioAdapter

        return binding.root
    }

    private fun getConversaciones() {

        val idEmpleado = FirebaseAuth.getInstance().currentUser?.uid
        CambioTurnoFirebase().getEmpleadoFirebase(idEmpleado) { empleado ->
            if (empleado?.id_servicio != null) {
                CambioTurnoFirebase().getEmpleadosServiceFirebase(empleado.id_servicio!!) { empleados ->
                    empleadosService.clear()
                    empleadosService.addAll(empleados)
                    CambioTurnoFirebase().getConversacionesFirebase(empleadosService) { conversaciones ->
                        empleadosConversacion.clear()
                        empleadosConversacion.addAll(conversaciones)
                        usuarioAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }
}