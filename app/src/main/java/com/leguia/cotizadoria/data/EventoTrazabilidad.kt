package com.leguia.cotizadoria.data

data class EventoTrazabilidad(
    val nombreEvento: String,
    val timestamp: Long = System.currentTimeMillis()
)