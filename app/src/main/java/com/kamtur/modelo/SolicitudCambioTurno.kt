package com.kamtur.modelo

data class SolicitudCambioTurno(
    var id_solicitud: String,
    var id_interesado: String,
    var id_turno_interesado: String,
    var id_cambio: String,
    var estado: String = "pendiente", //pendiente, aceptado, rechazado

) {
    constructor() : this("", "", "", "", "pendiente")
}