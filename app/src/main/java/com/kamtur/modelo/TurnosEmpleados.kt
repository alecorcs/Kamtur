package com.kamtur.modelo

/**
 * Clase con los datos de los Turnos de los Empleados que se almacenarán en la base de datos
 **/
data class TurnosEmpleados (
    var id_turno: String = "",
    var nombre_turno: String = "",
    var fecha_turno: String = "",
    var id_empleado: String = "",
) {
    constructor() : this("", "", "", "")

}