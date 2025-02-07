package com.kamtur.modelo

/**
 * Clase con los datos de los Cambios de Turno que se almacenarán en la base de datos
 **/
data class HistorialCambios (
    var id_historial: String,
    var id_cambio: String,
    var id_solicitud: String,
    var fecha_aprobado: String,
    //val id_supervisor: String
)