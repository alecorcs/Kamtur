package bbdd

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kamtur.modelo.CambioTurno
import com.kamtur.modelo.Conversaciones
import com.kamtur.modelo.Empleados
import com.kamtur.modelo.HistorialCambios
import com.kamtur.modelo.Mensajes
import com.kamtur.modelo.Notificaciones
import com.kamtur.modelo.Servicios
import com.kamtur.modelo.SolicitudCambioTurno
import com.kamtur.modelo.TurnosEmpleados

class CambioTurnoFirebase() {
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    /**
     * Create
     */
    fun addEmpleadoFirebase(empleado: Empleados) {
        val refEmpleados = database.getReference("empleados")
        val key = empleado.id_empleado
        key.let {
            refEmpleados.child(empleado.id_empleado).setValue(empleado).addOnSuccessListener {
                Log.d("Firebase", "Empleado añadido correctamente")
            }.addOnFailureListener { e ->
                Log.e("Firebase", "Error al añadir el empleado: ${e.message}")
            }

        }
    }

    fun addTurnFirebase(turnosEmpleado: TurnosEmpleados) {
        val refTurnos = database.getReference("turnosEmpleados")
        val key = refTurnos.push().key
        key?.let {
            turnosEmpleado.id_turno = it
            refTurnos.child(it).setValue(turnosEmpleado).addOnSuccessListener {
                Log.d("Firebase", "Turno añadido correctamente")
            }.addOnFailureListener { e ->
                Log.e("Firebase", "Error al añadir el turno: ${e.message}")
            }

        }
    }

    fun addServiceFirebase(servicio: Servicios) {
        val refServicios = database.getReference("servicios")
        val key = servicio.id_servicio
        key.let {
            refServicios.child(servicio.id_servicio).setValue(servicio).addOnSuccessListener {
                Log.d("Firebase", "Servicio añadido correctamente")
            }.addOnFailureListener { e ->
                Log.e("Firebase", "Error al añadir el servicio: ${e.message}")
            }
        }
    }

    fun addMessageFirebase(mensaje: Mensajes, idReceptor: String) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val referenceMensajes = FirebaseDatabase.getInstance().reference.child("mensajes")

       referenceMensajes.child(mensaje.id_mensaje).setValue(mensaje).addOnCompleteListener { tarea ->
           if (tarea.isSuccessful) {

               addConversacion(firebaseUser.uid, idReceptor)
               addConversacion(idReceptor, firebaseUser.uid)


           } else {
               Log.e("Firebase", "Error al añadir el mensaje: ${tarea.exception}")
           }

       }.addOnFailureListener { e ->
           Log.e("Firebase", "Error al añadir el mensaje: ${e.message}")
       }
    }

    private fun addConversacion(idEmisor: String, idReceptor: String) {
        val referenceConversacion = FirebaseDatabase.getInstance().reference
            .child("conversaciones")
            .child(idEmisor)
            .child(idReceptor)

        referenceConversacion.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    referenceConversacion.child("uid").setValue(idReceptor)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al añadir la conversación: ${error.message}")
            }

        })
    }

    fun addCambioFirebase(cambio: CambioTurno){
        val refCambio = database.getReference("cambioTurno")
        val key = refCambio.push().key

        key?.let {
            cambio.id_cambio = it
            refCambio.child(it).setValue(cambio).addOnSuccessListener {
                Log.d("Firebase", "Cambio añadido correctamente")
            }.addOnFailureListener { e ->
                Log.e("Firebase", "Error al añadir el cambio: ${e.message}")
            }

        }
    }

    fun addSolicitudFirebase(solicitud: SolicitudCambioTurno){
        val refSolicitud = database.getReference("solicitudCambio")
        val key = refSolicitud.push().key

        key?.let {
            solicitud.id_solicitud = it
            refSolicitud.child(it).setValue(solicitud).addOnSuccessListener {
                Log.d("Firebase", "Solicitud añadida correctamente")
            }.addOnFailureListener { e ->
                Log.e("Firebase", "Error al añadir la solicitud: ${e.message}")
            }
        }
    }

    fun addNotificacionFirebase(notificacion: Notificaciones){
        val refNotificaciones = database.getReference("notificaciones")
        val key = refNotificaciones.push().key

        key?.let{
            notificacion.id_notificacion = it
            refNotificaciones.child(it).setValue(notificacion).addOnSuccessListener {
                Log.d("Firebase", "Notificacion añadida correctamente")
            }.addOnFailureListener { e ->
                Log.e("Firebase", "Error al añadir la notificacion: ${e.message}")
            }

        }
    }

    fun addHistorialCambioFirebase(historialCambio: HistorialCambios){
        val refHistorial = database.getReference("historialCambio")
        val key = refHistorial.push().key
        key?.let {
            historialCambio.id_historial = it
            refHistorial.child(it).setValue(historialCambio).addOnSuccessListener {
                Log.d("Firebase", "Historial añadido correctamente")
            }.addOnFailureListener { e ->
                Log.e("Firebase", "Error al añadir el historial: ${e.message}")
            }
        }
    }

    /**
     * Read
     */
    fun getServiciosFirebase(resultado: (List<Servicios>) -> Unit) {
        val refServicios = database.getReference("servicios")
        refServicios.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaServicios = mutableListOf<Servicios>()
                for (servicioSnapshot in snapshot.children) {
                    val servicio = servicioSnapshot.getValue(Servicios::class.java)
                    servicio?.let {
                        listaServicios.add(it)
                    }
                }
                resultado(listaServicios)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al recuperar los servicios: ${error.message}")
            }
        })
    }

    fun getEmpleadoFirebase(idEmpleado: String? = null, resultado: (Empleados?) -> Unit) {
        val refEmpleados = database.getReference("empleados")

        //Filtro por id
        refEmpleados.orderByChild("id_empleado").equalTo(idEmpleado)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(getEmpleados: DataSnapshot) {
                    if (getEmpleados.exists()) {
                        val empleado =
                            getEmpleados.children.firstOrNull()
                                ?.getValue(Empleados::class.java)
                        resultado(empleado)
                    } else {
                        resultado(null)
                    }
                }


                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error al recuperar el empleado: ${error.message}")
                }

            })
    }

    fun getTurnosEmpleadoFirebase(
        idEmpleado: String,
        resultado: (List<TurnosEmpleados>) -> Unit
    ) {
        val refTurnos = database.getReference("turnosEmpleados")
        refTurnos.orderByChild("id_empleado").equalTo(idEmpleado)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(getTurnos: DataSnapshot) {
                    val listaTurnos = mutableListOf<TurnosEmpleados>()

                    for (turnoSnapshot in getTurnos.children) {
                        val turno = turnoSnapshot.getValue(TurnosEmpleados::class.java)

                        turno?.let {
                            listaTurnos.add(it)
                        }
                    }

                    resultado(listaTurnos)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error al recuperar los turnos: ${error.message}")
                }
            })
    }

    fun getServiciosEmpleadoFirebase(resultado: (String) -> Unit) {
        val idEmpleado = FirebaseAuth.getInstance().currentUser?.uid
        val refServicios = database.getReference("servicios")
        val refEmpleados = database.getReference("empleados")

        if (idEmpleado != null) {
            refEmpleados.child(idEmpleado).child("id_servicio")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val idServicio = dataSnapshot.getValue(String::class.java)
                        if (idServicio != null) {
                            refServicios.child(idServicio).child("nombre_servicio")
                                .addListenerForSingleValueEvent(object :
                                    ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val nombreServicio =
                                            snapshot.getValue(String::class.java)
                                        if (nombreServicio != null) {
                                            resultado(nombreServicio)
                                            Log.d(
                                                "Firebase",
                                                "Nombre del servicio: $nombreServicio"
                                            )
                                        } else {
                                            Log.e("Firebase", "No hay nombre de servicio")
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.e(
                                            "Firebase",
                                            "Error al recuperar el nombre del servicio: ${error.message}"
                                        )
                                    }
                                })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(
                            "Firebase",
                            "Error al recuperar el ID del servicio: ${error.message}"
                        )
                    }

                })
        } else {
            Log.e("Firebase", "No hay id del empleado")
        }


    }

    fun getEmpleadosServiceFirebase(
        idServicio: String,
        resultado: (MutableList<Empleados>) -> Unit
    ) {
        val refEmpleado = database.getReference("empleados")
        val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid

        refEmpleado.orderByChild("id_servicio").equalTo(idServicio)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val empleados = mutableListOf<Empleados>()

                    for (empleadosSnapshot in snapshot.children) {
                        val empleado = empleadosSnapshot.getValue(Empleados::class.java)
                        if ((empleado!!.id_empleado) != firebaseUser) {
                            empleados.add(empleado)
                        }
                    }
                    resultado(empleados)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(
                        "Firebase",
                        "Error al recuperar los empleados del servicio: ${error.message}"
                    )
                    resultado(mutableListOf())
                }

            })

    }

    fun getMensajesFirebase(
        idEmisor: String,
        idReceptor: String,
        resultado: (MutableList<Mensajes>) -> Unit
    ) {
        val refMensajes = database.getReference("mensajes")
        val conversaciones: MutableList<Mensajes> = mutableListOf()
        refMensajes.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                conversaciones.clear()
                for (mensajeSnapshot in snapshot.children) {
                    val mensaje = mensajeSnapshot.getValue(Mensajes::class.java)
                    if (mensaje != null){
                        if (mensaje.id_receptor == idEmisor && mensaje.id_emisor == idReceptor
                            || mensaje.id_receptor == idReceptor && mensaje.id_emisor == idEmisor) {
                            conversaciones.add(mensaje)

                        }
                    }
                }
                Log.d("Firebase", "Mensajes recuperados: $conversaciones")
                resultado(conversaciones)

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al recuperar los mensajes: ${error.message}")

            }

        })

    }
    fun getConversacionesFirebase(empleadosServicio: MutableList<Empleados>, resultado: (MutableList<Empleados>) -> Unit){
        val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid
        val refConversaciones = database.getReference("conversaciones").child(firebaseUser)
        val conversaciones: MutableList<Conversaciones> = mutableListOf()
        val empleadosconversacion: MutableList<Empleados> = mutableListOf()

        refConversaciones.addValueEventListener( object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                conversaciones.clear()
                empleadosconversacion.clear()
                for (converSnapshot in snapshot.children){
                    val conversacion = converSnapshot.getValue(Conversaciones::class.java)
                    if (conversacion != null){
                        conversaciones.add(conversacion)
                        Log.d("Firebase", "Conversaciones recuperadas: $conversaciones")
                    }
                    Log.d("Firebase", "Empleados Servicio: $empleadosServicio")
                    for (empleado in empleadosServicio){
                        if (conversacion!!.uid == empleado.id_empleado){
                            empleadosconversacion.add(empleado)
                            Log.d("Firebase", "Empleados recuperados: $empleadosconversacion")
                        }
                    }
                }
                resultado(empleadosconversacion)

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al recuperar las conversaciones: ${error.message}")
            }

        })



    }

    fun getCambioFirebase(estado: String, resultado: (MutableList<CambioTurno>) -> Unit) {
        val refCambio = database.getReference("cambioTurno")
        val cambios: MutableList<CambioTurno> = mutableListOf()

        refCambio.orderByChild("estado").equalTo(estado)
            .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (cambioSnapshot in snapshot.children) {
                    val cambio = cambioSnapshot.getValue(CambioTurno::class.java)
                    if (cambio != null) {
                        cambios.add(cambio)
                    }

                }
                resultado(cambios)
                Log.d("Firebase", "Cambios recuperados: $cambios")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al recuperar los cambios: ${error.message}")
            }

        })
    }

    fun getSolicitudFirebase(idCambio: String, resultado: (MutableList<SolicitudCambioTurno>) -> Unit) {
        val refSolicitud = database.getReference("solicitudCambio")
        val solicitudes: MutableList<SolicitudCambioTurno> = mutableListOf()
        refSolicitud.orderByChild("id_cambio").equalTo(idCambio)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (solicitudSnapshot in snapshot.children) {
                        val solicitud = solicitudSnapshot.getValue(SolicitudCambioTurno::class.java)
                        if (solicitud != null) {
                            solicitudes.add(solicitud)
                        }
                    }
                    resultado(solicitudes)
                    Log.d("Firebase", "Solicitudes recuperadas: $solicitudes")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error al recuperar las solicitudes: ${error.message}")
                }

            })
    }

    fun getHistorialCamnbiosFirebase(idCambio: String, resultado: (MutableList<HistorialCambios>) -> Unit){
        val refHistorial = database.getReference("historialCambio")
        val historial: MutableList<HistorialCambios> = mutableListOf()
        refHistorial.orderByChild("id_cambio").equalTo(idCambio)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (historialSnapshot in snapshot.children) {
                        val cambio = historialSnapshot.getValue(HistorialCambios::class.java)
                        if (cambio != null) {
                            historial.add(cambio)
                        }
                    }
                    resultado(historial)
                    Log.d("Firebase", "Historial recuperados: $historial")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error al recuperar el historial: ${error.message}")
                }
            })
    }

    fun getNotificacionesFirebase(idEmpleado: String, resultado: (MutableList<Notificaciones>) -> Unit){
        val refNotificaciones = database.getReference("notificaciones")
        val notificaciones: MutableList<Notificaciones> = mutableListOf()
        refNotificaciones.orderByChild("id_receptor").equalTo(idEmpleado)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (notificacionSnapshot in snapshot.children) {
                        val notificacion = notificacionSnapshot.getValue(Notificaciones::class.java)
                        if (notificacion != null) {
                            notificaciones.add(notificacion)
                        }
                    }
                    resultado(notificaciones)
                    Log.d("Firebase", "Notificaciones recuperadas: $notificaciones")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error al recuperar las notificaciones: ${error.message}")
                }

            })
    }
    /**
     * Update
     */

    fun updateEmpleadoFirebase(hashMapEmpleado: HashMap<String, Any>) {
        val refEmpleados = database.getReference("empleados")
        val idEmpleado = FirebaseAuth.getInstance().currentUser?.uid
        if (idEmpleado != null) {
            refEmpleados.child(idEmpleado).updateChildren(hashMapEmpleado)
                .addOnCompleteListener {
                    Log.d("Firebase", "Empleado actualizado correctamente")
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Error al actualizar el empleado: ${e.message}")
                }
        }
    }

    fun updateTurnoFirebase(turno: TurnosEmpleados, hashMapTurno: HashMap<String, Any>) {
        val refTurnos = database.getReference("turnosEmpleados")
        val idTurno = turno.id_turno
        refTurnos.child(idTurno).updateChildren(hashMapTurno)
            .addOnCompleteListener {
                Log.d("Firebase", "Turno actualizado correctamente")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al actualizar el turno: ${e.message}")
            }
    }

    fun updateMensajesFirebase(mensaje : Mensajes, hashMapMensajes: HashMap<String, Any>) {
        val refMensajes = database.getReference("mensajes")
        val idMensaje = mensaje.id_mensaje

        refMensajes.child(idMensaje).updateChildren(hashMapMensajes)
            .addOnCompleteListener {
                Log.d("Firebase", "Mensaje actualizado correctamente")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al actualizar el mensaje: ${e.message}")
            }
    }

    fun updateCambioFirebase(idCambio: String, hashMapCambio: HashMap<String, Any>){
        val refCambio = database.getReference("cambioTurno")
        refCambio.child(idCambio).updateChildren(hashMapCambio)
            .addOnCompleteListener {
            Log.d("Firebase", "Cambio actualizado correctamente")
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Error al actualizar el cambio: ${e.message}")
        }
    }

    fun updateSolicitudFirebase(solicitud: SolicitudCambioTurno, hashMapSolicitud: HashMap<String, Any>){
        val refSolicitud = database.getReference("solicitudCambio")
        val idSolicitud = solicitud.id_solicitud
        refSolicitud.child(idSolicitud).updateChildren(hashMapSolicitud)
            .addOnCompleteListener {
            Log.d("Firebase", "Solicitud actualizada correctamente")
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Error al actualizar la solicitud: ${e.message}")
        }
    }

    fun updateHistorialFirebase(historial: HistorialCambios, hashMapHistorial: HashMap<String, Any>){
        val refHistorial = database.getReference("historialCambio")
        val idHistorial = historial.id_historial
        refHistorial.child(idHistorial).updateChildren(hashMapHistorial)
            .addOnCompleteListener {
                Log.d("Firebase", "Historial actualizado correctamente")
            }.addOnFailureListener { e ->
                Log.e("Firebase", "Error al actualizar el historial: ${e.message}")
            }
    }

    fun updateNotificacionFirebase(notificacion: Notificaciones, hashMapNotificacion: HashMap<String, Any>){
        val refNotificaciones = database.getReference("notificaciones")
        val idNotificacion = notificacion.id_notificacion
        refNotificaciones.child(idNotificacion).updateChildren(hashMapNotificacion)
            .addOnCompleteListener {
                Log.d("Firebase", "Notificacion actualizada correctamente")
            }.addOnFailureListener { e ->
                Log.e("Firebase", "Error al actualizar la notificacion: ${e.message}")
            }
    }


    /**
     * Delete
     */

    fun removeEmpleadoFirebase(empleado: Empleados) {
        val refEmpleados = database.getReference("empleados")
        val idEmpleado = empleado.id_empleado
        refEmpleados.child(idEmpleado).removeValue().addOnCompleteListener {
            Log.d("Firebase", "Empleado eliminado correctamente")
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Error al eliminar el empleado: ${e.message}")
        }
    }

    fun removeTurnFirebase(turno: TurnosEmpleados) {
        val refTurnos = database.getReference("turnosEmpleados")
        val idTurno = turno.id_turno
        refTurnos.child(idTurno).removeValue().addOnCompleteListener {
            Log.d("Firebase", "Turno eliminado correctamente")
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Error al eliminar el turno: ${e.message}")
        }
    }

    fun removeServiceFirebase(servicio: Servicios) {
        val refServicios = database.getReference("servicios")
        val idServicio = servicio.id_servicio
        refServicios.child(idServicio).removeValue().addOnCompleteListener {
            Log.d("Firebase", "Servicio eliminado correctamente")
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Error al eliminar el servicio: ${e.message}")
        }
    }

    fun removeMensajesFirebase(mensaje: Mensajes) {
        val refMensajes = database.getReference("mensajes")
        val idMensaje = mensaje.id_mensaje
        refMensajes.child(idMensaje).removeValue().addOnCompleteListener {
            Log.d("Firebase", "Mensaje eliminado correctamente")
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Error al eliminar el mensaje: ${e.message}")
        }
    }

    fun removeConversacionesFirebase(conversacion: Conversaciones) {
        val refConversaciones = database.getReference("conversaciones")
        val idConversacion = conversacion.uid
        refConversaciones.child(idConversacion).removeValue().addOnCompleteListener {
            Log.d("Firebase", "Conversacion eliminada correctamente")
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Error al eliminar la conversacion: ${e.message}")
        }
    }

    fun removeCambioFirebase(cambio: CambioTurno){
        val refCambio = database.getReference("cambioTurno")
        val idCambio = cambio.id_cambio
        refCambio.child(idCambio).removeValue().addOnCompleteListener {
            Log.d("Firebase", "Cambio eliminado correctamente")
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Error al eliminar el cambio: ${e.message}")
        }

    }

    fun removeNotificacionFirebase(notificacion: Notificaciones){
        val refNotificaciones = database.getReference("notificaciones")
        val idNotificacion = notificacion.id_notificacion
        refNotificaciones.child(idNotificacion).removeValue().addOnCompleteListener {
            Log.d("Firebase", "Notificacion eliminada correctamente")
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Error al eliminar la notificacion: ${e.message}")
        }
    }

    fun removeSolicitudFirebase(solicitud: SolicitudCambioTurno){
        val refSolicitud = database.getReference("solicitudCambio")
        val idSolicitud = solicitud.id_solicitud
        refSolicitud.child(idSolicitud).removeValue().addOnCompleteListener {
            Log.d("Firebase", "Solicitud eliminada correctamente")
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Error al eliminar la solicitud: ${e.message}")
        }
    }

    fun removeHistoriaCambioslFirebase(historial: HistorialCambios){
        val refHistorial = database.getReference("historialCambio")
        val idHistorial = historial.id_historial
        refHistorial.child(idHistorial).removeValue().addOnCompleteListener {
            Log.d("Firebase", "Historial eliminado correctamente")
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Error al eliminar el historial: ${e.message}")
        }
    }

    /**
     * Métodos con BBDD
     */
    fun checkToAddServicio(nombreGrupo: String) {
        val refServicios = FirebaseDatabase.getInstance().getReference("servicios")
        val idEmpleado = FirebaseAuth.getInstance().currentUser?.uid

        //Comprobar si el empleado tiene servicio/grupo
        getEmpleadoFirebase(idEmpleado) { empleado ->
            if (empleado?.id_servicio == null) {
                Log.d("Firebase", "El empleado no tiene servicio")
                //Comprobar si el servicio existe
                getServiciosFirebase { listaServicios ->
                    Log.d("Firebase", "lista de servicios $listaServicios")
                    var servicioExistente: Servicios? = null

                    for (servicio in listaServicios) {
                        if (servicio.nombre_servicio == nombreGrupo) {
                            servicioExistente = servicio
                            break
                        }
                    }

                    if (servicioExistente != null) {
                        val hasmap = hashMapOf<String, Any>()
                        hasmap["id_servicio"] = servicioExistente.id_servicio
                        updateEmpleadoFirebase(hasmap)
                        Log.d("Firebase", "lista de servicios $servicioExistente")
                        Log.d("Firebase", "Te has añadido al servicio existente")
                    } else {
                        val id = refServicios.push().key
                        addServiceFirebase(Servicios(id!!, nombreGrupo))
                        val hasmap = hashMapOf<String, Any>()
                        hasmap["id_servicio"] = id
                        updateEmpleadoFirebase(hasmap)
                    }
                }
            } else {
                getServiciosFirebase { listaServicios ->
                    Log.d("Firebase", "lista de servicios $listaServicios")
                    var servicioExistente: Servicios? = null

                    for (servicio in listaServicios) {
                        if (servicio.nombre_servicio == nombreGrupo) {
                            servicioExistente = servicio
                            break
                        }
                    }

                    if (servicioExistente != null) {
                        val hasmap = hashMapOf<String, Any>()
                        hasmap["id_servicio"] = servicioExistente!!.id_servicio
                        updateEmpleadoFirebase(hasmap)
                        Log.d("Firebase", "lista de servicios $servicioExistente")
                        Log.d("Firebase", "Te has añadido al servicio existente")
                    } else {
                        val id = refServicios.push().key
                        addServiceFirebase(Servicios(id!!, nombreGrupo))
                        val hasmap = hashMapOf<String, Any>()
                        hasmap["id_servicio"] = id
                        updateEmpleadoFirebase(hasmap)
                    }
                }
            }
        }

    }

    fun searchEmpleado(
        busqueda: String,
        idServicio: String,
        resultado: (MutableList<Empleados>) -> Unit
    ) {
        val refEmpleado = database.getReference("empleados")
        val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid

        refEmpleado.orderByChild("id_servicio").equalTo(idServicio)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val empleados = mutableListOf<Empleados>()


                    for (empleadoSnapshot in snapshot.children) {
                        val empleado = empleadoSnapshot.getValue(Empleados::class.java)

                        // Asegurarse de que el empleado no sea el usuario actual y filtrar por el nombre
                        if (empleado != null && empleado.id_empleado != firebaseUser) {
                            // Verifica si el nombre_empleado comienza con lo que el usuario está buscando
                            if (empleado.nombre_empleado.lowercase()
                                    .startsWith(busqueda.lowercase())
                            ) {
                                empleados.add(empleado)
                            }
                        }
                    }

                    // Devolver la lista filtrada
                    resultado(empleados)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(
                        "Firebase",
                        "Error al recuperar los empleados del servicio: ${error.message}"
                    )
                    resultado(mutableListOf())
                }
            })


    }

    fun updateTurno(solicitud: SolicitudCambioTurno){
        //Buscamos los cambios realizados
        CambioTurnoFirebase().getCambioFirebase("cambio realizado") { cambios ->
            //Buscamos el cambio recien aceptado en la lista de cambios
            val cambioSolicitado = cambios.find { it.id_cambio == solicitud.id_cambio }
            //Si los cambios no son nulos
            if (cambioSolicitado != null) {
                //Buscamos los turnos del empleado que solicita el cambio
                CambioTurnoFirebase().getTurnosEmpleadoFirebase(cambioSolicitado.id_solicitante){ turnosSolicitante ->
                    //Si la lista de turnos del empleado que solicita el cambio no esta vacia
                    if (turnosSolicitante.isNotEmpty()) {
                        //Buscamos el turno solicitado en la lista de turnos del empleado que solicita el cambio
                        val turnoSolicitado = turnosSolicitante.find { it.id_turno == cambioSolicitado.id_turno_solicitante }
                        //Si lo encontramos
                        if (turnoSolicitado != null) {
                            //Buscamos los turnos del empleado interesado en el cambio
                            CambioTurnoFirebase().getTurnosEmpleadoFirebase(solicitud.id_interesado) { turnosInteresado ->
                                //Si la lista de turnos del empleado interesado no esta vacia
                                if (turnosInteresado.isNotEmpty()) {
                                    //Buscamos el turno interesado en el cambio en la lista de turnos del empleado interesado
                                    val turnoInteresado = turnosInteresado.find { it.id_turno == solicitud.id_turno_interesado }
                                    //Si lo encontramos
                                    if (turnoInteresado != null) {
                                        //Obtenemos el nombre y fecah del turno solicitado y del turno interesado
                                        val nombreTurnoSolicitado = turnoSolicitado.nombre_turno
                                        val fechaTurnoSolicitado = turnoSolicitado.fecha_turno

                                        val nombreTurnoInteresado = turnoInteresado.nombre_turno
                                        val fechaTurnoInteresado = turnoInteresado.fecha_turno

                                        //Actualizamos los turnos del empleado que solicita el cambio con el turno del empleado interesado en el cambio
                                        val hashmapTurnoSolicitado = HashMap<String, Any>()
                                        hashmapTurnoSolicitado["nombre_turno"] = nombreTurnoInteresado
                                        hashmapTurnoSolicitado["fecha_turno"] = fechaTurnoInteresado

                                        val hashmapTurnoInteresado = HashMap<String, Any>()
                                        hashmapTurnoInteresado["nombre_turno"] = nombreTurnoSolicitado
                                        hashmapTurnoInteresado["fecha_turno"] = fechaTurnoSolicitado

                                        CambioTurnoFirebase().updateTurnoFirebase(turnoSolicitado, hashmapTurnoSolicitado)
                                        CambioTurnoFirebase().updateTurnoFirebase(turnoInteresado, hashmapTurnoInteresado)
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }
    }


}