package com.kamtur.modelo

/**
 * Clase con los datos de las Notificaciones que se almacenarán en la base de datos
 **/

data class Notificaciones (
    var id_notificacion: String,
    val id_receptor: String,
    //val id_supervisor: String,
    var id_cambio: String,
    var fecha_notificacion: String,
    var leida: Int = 0, //0 = no leida, 1 = leida
    var mensaje: String // "Se ha realizado una propuesta de cambio", "Tu cambio ha sido aceptado",  "Tu propuesta ha sido aceptada", "Tu propuesta ha sido rechazada"
){
    constructor() : this("", "", "", "", 0, "")
}