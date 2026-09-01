package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonGreen

@Composable
fun LiveMetricChart(
    title: String,
    currentValue: String,
    unit: String,
    dataPoints: List<Float>,
    lineColor: Color = ElectricCyan,
    fillColor: Color = ElectricCyan.copy(alpha = 0.2f),
    maxScale: Float = 100f,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF111C30)
        ),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(Color(0xFF1E293B), lineColor.copy(alpha = 0.3f), Color(0xFF1E293B))
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Title & Current metric reading
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(lineColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFCBD5E1),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = currentValue,
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bezier Line Chart Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0A0F1D))
                    .border(0.5.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
            ) {
                // Handle fallback if data points are empty
                val points = remember(dataPoints) {
                    if (dataPoints.isEmpty()) listOf(20f, 25f, 22f, 30f, 28f, 35f, 32f) else dataPoints
                }

                Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 10.dp)) {
                    val w = size.width
                    val h = size.height

                    // Draw horizontal grid lines
                    val gridColor = Color(0xFF19243B)
                    for (i in 1..3) {
                        val y = h * (i / 4f)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                        )
                    }

                    if (points.size < 2) return@Canvas

                    val stepX = w / (points.size - 1)
                    val strokePath = Path()
                    val fillPath = Path()

                    val firstY = h - (points[0].coerceIn(0f, maxScale) / maxScale) * h
                    strokePath.moveTo(0f, firstY)
                    fillPath.moveTo(0f, h)
                    fillPath.lineTo(0f, firstY)

                    for (i in 0 until points.size - 1) {
                        val currentX = i * stepX
                        val currentY = h - (points[i].coerceIn(0f, maxScale) / maxScale) * h
                        val nextX = (i + 1) * stepX
                        val nextY = h - (points[i + 1].coerceIn(0f, maxScale) / maxScale) * h

                        val controlX1 = currentX + (nextX - currentX) / 2
                        val controlY1 = currentY
                        val controlX2 = currentX + (nextX - currentX) / 2
                        val controlY2 = nextY

                        strokePath.cubicTo(controlX1, controlY1, controlX2, controlY2, nextX, nextY)
                        fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, nextX, nextY)
                    }

                    val lastX = (points.size - 1) * stepX
                    val lastY = h - (points.last().coerceIn(0f, maxScale) / maxScale) * h

                    fillPath.lineTo(lastX, h)
                    fillPath.close()

                    // Draw gradient fill
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(lineColor.copy(alpha = 0.35f), Color.Transparent),
                            startY = 0f,
                            endY = h
                        )
                    )

                    // Draw stroke line
                    drawPath(
                        path = strokePath,
                        color = lineColor,
                        style = Stroke(width = 3f, cap = StrokeCap.Round)
                    )

                    // Draw glowing end dot on latest point
                    drawCircle(
                        color = lineColor.copy(alpha = 0.4f),
                        radius = 8f,
                        center = Offset(lastX, lastY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = Offset(lastX, lastY)
                    )
                }
            }
        }
    }
}
