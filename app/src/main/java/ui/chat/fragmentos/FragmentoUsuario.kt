package ui.chat.fragmentos

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import bbdd.CambioTurnoFirebase
import com.example.kamtur.databinding.FragmentFragmentoUsuarioBinding
import com.google.firebase.auth.FirebaseAuth
import com.kamtur.modelo.Empleados
import ui.chat.UsuarioAdapter

class FragmentoUsuario : Fragment() {
    private lateinit var binding: FragmentFragmentoUsuarioBinding
    private lateinit var usuarioAdapter: UsuarioAdapter

    private var empleadosService: MutableList<Empleados> = mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentFragmentoUsuarioBinding.inflate(inflater, container, false)

        searchEmpleado("")
        binding.etBuscarUsuario.addTextChangedListener{
            val etBuscar = it.toString().lowercase()
            searchEmpleado(etBuscar)
        }


        usuarioAdapter = UsuarioAdapter(empleadosService, false)
        binding.rvItem.layoutManager = LinearLayoutManager(requireContext())
        binding.rvItem.adapter = usuarioAdapter


        return binding.root
    }
    private fun searchEmpleado(busqueda: String) {
        val idEmpleado = FirebaseAuth.getInstance().currentUser?.uid
        if (busqueda.isNotEmpty()) {
            CambioTurnoFirebase().getEmpleadoFirebase(idEmpleado) { empleado ->
                if (empleado?.id_servicio != null) {
                    CambioTurnoFirebase().searchEmpleado(busqueda, empleado.id_servicio!!) { empleados ->
                        empleadosService.clear()
                        empleadosService.addAll(empleados)
                        usuarioAdapter.notifyDataSetChanged()
                    }
                }
            }

        }else {
            CambioTurnoFirebase().getEmpleadoFirebase(idEmpleado) { empleado ->
                if (empleado?.id_servicio != null) {
                    CambioTurnoFirebase().getEmpleadosServiceFirebase(empleado.id_servicio!!) { empleados ->
                        empleadosService.clear()
                        empleadosService.addAll(empleados)
                        usuarioAdapter.notifyDataSetChanged()
                    }
                }
            }
        }

    }
}