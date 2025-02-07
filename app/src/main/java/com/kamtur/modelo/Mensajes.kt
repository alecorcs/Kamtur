package com.kamtur.modelo

data class Mensajes (
    val id_mensaje: String,
    val id_emisor: String,
    val id_receptor: String,
    val mensaje: String,
    val url: String,
    val visto: Boolean
){
    constructor() : this("", "", "", "", "", false)
}