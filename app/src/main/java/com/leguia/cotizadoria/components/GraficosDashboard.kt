package com.leguia.cotizadoria.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun GraficoDona(
    titulo: String,
    valores: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    val colores = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.secondary
    )
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant
    val total = valores.sumOf { it.second }.coerceAtLeast(0)
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Canvas(Modifier.size(132.dp)) {
                    val stroke = Stroke(width = size.minDimension * 0.22f, cap = StrokeCap.Butt)
                    if (total == 0) {
                        drawArc(
                            color = emptyColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = stroke
                        )
                    } else {
                        var start = -90f
                        valores.forEachIndexed { index, (_, value) ->
                            val sweep = value.coerceAtLeast(0).toFloat() / total * 360f
                            if (sweep > 0f) {
                                drawArc(colores[index % colores.size], start, sweep, false, style = stroke)
                                start += sweep
                            }
                        }
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    valores.forEachIndexed { index, (label, value) ->
                        val percent = if (total == 0) 0 else (value * 100 / total)
                        Text(
                            "$label · $value (${percent}%)",
                            style = MaterialTheme.typography.labelMedium,
                            color = colores[index % colores.size]
                        )
                    }
                    if (valores.isEmpty()) Text("Sin datos", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun GraficoTendenciaSemanal(
    titulo: String,
    etiquetas: List<String>,
    valores: List<Int>,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val guideColor = MaterialTheme.colorScheme.outlineVariant
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            if (valores.isEmpty()) {
                Text("Sin cotizaciones para mostrar.", style = MaterialTheme.typography.bodySmall)
            } else {
                Canvas(Modifier.fillMaxWidth().height(148.dp)) {
                    val horizontalPadding = 12.dp.toPx()
                    val verticalPadding = 12.dp.toPx()
                    val plotWidth = (size.width - horizontalPadding * 2).coerceAtLeast(1f)
                    val plotHeight = (size.height - verticalPadding * 2).coerceAtLeast(1f)
                    for (step in 0..3) {
                        val y = verticalPadding + plotHeight * step / 3f
                        drawLine(guideColor, Offset(horizontalPadding, y), Offset(size.width - horizontalPadding, y), 1.dp.toPx())
                    }
                    val maxValue = valores.maxOrNull()?.coerceAtLeast(1)?.toFloat() ?: 1f
                    val points = valores.mapIndexed { index, value ->
                        val x = if (valores.size == 1) size.width / 2f else horizontalPadding + plotWidth * index / (valores.size - 1)
                        val y = verticalPadding + plotHeight * (1f - value / maxValue)
                        Offset(x, y)
                    }
                    if (points.size > 1) {
                        val path = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            points.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(path, lineColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                    }
                    points.forEach { drawCircle(lineColor, radius = 4.dp.toPx(), center = it) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    etiquetas.forEach { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                Text("Máximo diario: ${valores.maxOrNull() ?: 0}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun GraficoBarras(
    titulo: String,
    valores: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    val maximum = valores.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val fillColor = MaterialTheme.colorScheme.primary
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            if (valores.isEmpty()) Text("Sin datos para mostrar.", style = MaterialTheme.typography.bodySmall)
            valores.forEach { (label, value) ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(label, style = MaterialTheme.typography.bodySmall)
                        Text(value.toString(), style = MaterialTheme.typography.labelLarge)
                    }
                    Canvas(Modifier.fillMaxWidth().height(12.dp)) {
                        drawRoundRect(trackColor, cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2))
                        val fraction = value.coerceAtLeast(0).toFloat() / maximum
                        drawRoundRect(color = fillColor, size = androidx.compose.ui.geometry.Size(size.width * fraction, size.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2))
                    }
                }
            }
        }
    }
}
