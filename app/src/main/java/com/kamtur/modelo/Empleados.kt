package com.kamtur.modelo

/**
 * Clase con los datos de los Empleados que se almacenarán en la base de datos
 **/
data class Empleados(
    var id_empleado: String = "",
    var nombre_empleado: String = "",
    var apellidos_empleado: String = "",
    var foto_empleado: String? = null,
    var foto_portada: String? = null,
    var id_servicio: String? = null,
    var email_empleado: String = "",
    var estado_empleado: String = "offline",
    var id_supervisor: String? = null
) {
    constructor() : this("", "", "", null, null, null, "", "offline", null)

}