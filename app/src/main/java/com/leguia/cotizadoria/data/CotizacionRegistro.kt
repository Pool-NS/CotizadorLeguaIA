package com.leguia.cotizadoria.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CotizacionRegistro(
    val id: String,
    val cotizador: String,
    val servicio: String,
    var caracteristicasIngresadas: String = "",
    var datosInterpretadosIA: String = "N/A",
    var datosCorregidosManual: String = "",
    var precioReferencial: Double = 0.0,
    var descuento: Double = 0.0,
    var precioFinal: Double = 0.0,
    val horaInicio: Long = System.currentTimeMillis(),
    var horaFin: Long = 0L,
    var estado: EstadoCotizacion = EstadoCotizacion.EN_PROCESO,
    var observaciones: String = "Sin observaciones",
    var correcta: String = "PENDIENTE",
    var oportuna: String = "PENDIENTE",
    val eventos: MutableList<EventoTrazabilidad> = mutableListOf()
) {
    fun registrarEvento(nombre: String) {
        eventos.add(EventoTrazabilidad(nombre))
    }

    fun obtenerTiempoTotalSegundos(): Long {
        if (horaFin <= 0L) return 0L
        return (horaFin - horaInicio) / 1000
    }

    fun obtenerFechaFormateada(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(horaInicio))
    }

    fun obtenerHoraInicioFormateada(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(horaInicio))
    }

    fun obtenerHoraFinFormateada(): String {
        if (horaFin <= 0L) return "N/A"
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(horaFin))
    }

    fun toCsvRow(): String {
        val tiempoSeg = obtenerTiempoTotalSegundos()
        val caracteristicasEscapadas = caracteristicasIngresadas.replace("\"", "\"\"")
        val obsEscapadas = observaciones.replace("\"", "\"\"")

        return "$id,${obtenerFechaFormateada()},$cotizador,$servicio,${obtenerHoraInicioFormateada()},${obtenerHoraFinFormateada()},$tiempoSeg,${String.format(Locale.US, "%.2f", precioReferencial)},${String.format(Locale.US, "%.2f", descuento)},${String.format(Locale.US, "%.2f", precioFinal)},$correcta,$oportuna,\"$obsEscapadas\""
    }

    companion object {
        fun obtenerEncabezadoCsv(): String {
            return "cotizacion_id,fecha,cotizador,servicio,hora_inicio,hora_fin,tiempo_segundos,precio_referencial,descuento,precio_final,correcta,oportuna,observaciones"
        }
    }
}