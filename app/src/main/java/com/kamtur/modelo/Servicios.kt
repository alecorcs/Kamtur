package com.kamtur.modelo

/**
 * Clase con los datos de los Servicios que se almacenarán en la base de datos
 **/
data class Servicios(
    var id_servicio: String,
    var nombre_servicio: String
){
    constructor() : this("", "")
}
