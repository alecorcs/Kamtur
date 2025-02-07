package com.kamtur.modelo

/**
 * Clase con los datos de los Cambios de Turno que se almacenarán en la base de datos
 **/

data class CambioTurno (
    var id_cambio: String,
    var id_solicitante: String,
    var id_turno_solicitante: String,
    var fecha_solicitud: String,
    var estado: String = "pendiente", //pendiente, cambio realizado
    var fecha_aprobado: String? = null
) {

    constructor() : this("", "", "", "", "pendiente", null)

}