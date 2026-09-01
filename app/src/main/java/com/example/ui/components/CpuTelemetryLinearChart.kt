package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MetricHistoryEntity
import com.example.data.local.ServerEntity
import com.example.data.model.ChartDisplayMode
import com.example.data.model.ChartMetricType
import com.example.data.model.ChartTimeRange
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * High-performance, responsive linear visualization component for server CPU and telemetry history.
 * Supports smooth Bezier curves, interactive touch scrubbers, comparative multi-series, and live stat metrics.
 */
@Composable
fun CpuTelemetryLinearChart(
    servers: List<ServerEntity>,
    metricsHistory: List<MetricHistoryEntity>,
    selectedMetric: ChartMetricType,
    onMetricChange: (ChartMetricType) -> Unit,
    timeRange: ChartTimeRange,
    onTimeRangeChange: (ChartTimeRange) -> Unit,
    displayMode: ChartDisplayMode,
    onDisplayModeChange: (ChartDisplayMode) -> Unit,
    selectedFocusServerId: Long?,
    onFocusServerChange: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter points by time range and server focus
    val filteredMetrics = remember(metricsHistory, selectedFocusServerId, timeRange) {
        val serverFiltered = if (selectedFocusServerId != null) {
            metricsHistory.filter { it.serverId == selectedFocusServerId }
        } else {
            metricsHistory
        }
        if (serverFiltered.size > timeRange.pointsCount) {
            serverFiltered.takeLast(timeRange.pointsCount)
        } else {
            serverFiltered
        }
    }

    // Compute aggregated time series
    val timeSeriesData = remember(filteredMetrics, servers, selectedFocusServerId, selectedMetric) {
        if (selectedFocusServerId != null) {
            // Single server line
            val serverMetrics = filteredMetrics.filter { it.serverId == selectedFocusServerId }
            serverMetrics.map { m ->
                val value = when (selectedMetric) {
                    ChartMetricType.CPU -> m.cpuPercent
                    ChartMetricType.RAM -> m.ramPercent
                    ChartMetricType.LATENCY -> m.latencyMs.toFloat()
                    ChartMetricType.BANDWIDTH -> m.bandwidthMbps
                }
                TimeSeriesPoint(timestamp = m.timestamp, avgValue = value, maxValue = value)
            }
        } else {
            // Fleet aggregation: group by timestamp bucket (within ~5 seconds window)
            val grouped = filteredMetrics.groupBy { it.timestamp / 5000L }
            grouped.map { (_, points) ->
                val values = points.map { m ->
                    when (selectedMetric) {
                        ChartMetricType.CPU -> m.cpuPercent
                        ChartMetricType.RAM -> m.ramPercent
                        ChartMetricType.LATENCY -> m.latencyMs.toFloat()
                        ChartMetricType.BANDWIDTH -> m.bandwidthMbps
                    }
                }
                val avg = if (values.isNotEmpty()) values.average().toFloat() else 0f
                val max = values.maxOrNull() ?: avg
                val ts = points.firstOrNull()?.timestamp ?: System.currentTimeMillis()
                TimeSeriesPoint(timestamp = ts, avgValue = avg, maxValue = max)
            }.sortedBy { it.timestamp }
        }
    }

    // Multi-server series for comparative mode (top 3 loaded servers)
    val multiServerSeries = remember(metricsHistory, servers, selectedMetric, displayMode) {
        if (displayMode == ChartDisplayMode.MULTI_SERVER) {
            val topServers = servers.sortedByDescending { it.cpuUsagePercent }.take(3)
            val colorPalette = listOf(ElectricCyan, NeonGreen, PurpleAccent)
            topServers.mapIndexed { idx, srv ->
                val srvMetrics = metricsHistory.filter { it.serverId == srv.id }.takeLast(20)
                val points = srvMetrics.map { m ->
                    val v = when (selectedMetric) {
                        ChartMetricType.CPU -> m.cpuPercent
                        ChartMetricType.RAM -> m.ramPercent
                        ChartMetricType.LATENCY -> m.latencyMs.toFloat()
                        ChartMetricType.BANDWIDTH -> m.bandwidthMbps
                    }
                    TimeSeriesPoint(timestamp = m.timestamp, avgValue = v, maxValue = v)
                }
                ServerSeries(
                    server = srv,
                    color = colorPalette.getOrElse(idx) { ElectricCyan },
                    points = points
                )
            }
        } else {
            emptyList()
        }
    }

    // Calculate Summary Stats
    val currentReading = remember(timeSeriesData, servers, selectedFocusServerId, selectedMetric) {
        if (selectedFocusServerId != null) {
            val target = servers.find { it.id == selectedFocusServerId }
            when (selectedMetric) {
                ChartMetricType.CPU -> "${target?.cpuUsagePercent?.toInt() ?: 0}%"
                ChartMetricType.RAM -> "${target?.ramUsagePercent?.toInt() ?: 0}%"
                ChartMetricType.LATENCY -> "${target?.latencyMs ?: 0} ms"
                ChartMetricType.BANDWIDTH -> "${target?.bandwidthInMbps?.toInt() ?: 0} Mbps"
            }
        } else {
            val avg = if (servers.isNotEmpty()) servers.map { it.cpuUsagePercent }.average().toInt() else 0
            when (selectedMetric) {
                ChartMetricType.CPU -> "$avg%"
                ChartMetricType.RAM -> "${if (servers.isNotEmpty()) servers.map { it.ramUsagePercent }.average().toInt() else 0}%"
                ChartMetricType.LATENCY -> "${if (servers.isNotEmpty()) servers.map { it.latencyMs }.average().toInt() else 0} ms"
                ChartMetricType.BANDWIDTH -> "${if (servers.isNotEmpty()) servers.map { it.bandwidthInMbps }.average().toInt() else 0} Mbps"
            }
        }
    }

    val avgReading = remember(timeSeriesData) {
        if (timeSeriesData.isNotEmpty()) timeSeriesData.map { it.avgValue }.average().toFloat() else 0f
    }
    val maxReading = remember(timeSeriesData) {
        if (timeSeriesData.isNotEmpty()) timeSeriesData.map { it.maxValue }.maxOrNull() ?: 0f else 0f
    }
    val minReading = remember(timeSeriesData) {
        if (timeSeriesData.isNotEmpty()) timeSeriesData.map { it.avgValue }.minOrNull() ?: 0f else 0f
    }

    // Scrubber / Crosshair Touch State
    var touchXNormalized by remember { mutableFloatStateOf(-1f) }
    var isTouching by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A)
        ),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(Color(0xFF1E293B), ElectricCyan.copy(alpha = 0.35f), Color(0xFF1E293B))
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Title, Metric Icon & Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ElectricCyan.copy(alpha = 0.15f))
                            .border(1.dp, ElectricCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoGraph,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Télémétrie Graphique Linéaire",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (selectedFocusServerId != null) {
                                val s = servers.find { it.id == selectedFocusServerId }
                                "Nœud Isolé • ${s?.name ?: "Serveur"} (${s?.region?.city ?: ""})"
                            } else {
                                "Historique Flotte Globale • ${servers.size} nœuds surveillés"
                            },
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Instant Value Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E293B),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(ElectricCyan.copy(alpha = 0.5f), NeonGreen.copy(alpha = 0.5f)))
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Actuel: ",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = currentReading,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metric Type Chips Carousel (CPU, RAM, Latency, Bandwidth)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChartMetricType.values().forEach { metric ->
                    val isSelected = selectedMetric == metric
                    FilterChip(
                        selected = isSelected,
                        onClick = { onMetricChange(metric) },
                        label = {
                            Text(
                                text = "${metric.label} (${metric.unit})",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricCyan.copy(alpha = 0.2f),
                            selectedLabelColor = ElectricCyan,
                            containerColor = Color(0xFF131D33),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) ElectricCyan else Color(0xFF1E293B)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Display Mode & Time Window Toolbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Display Mode Selector
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF131D33))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    ChartDisplayMode.values().forEach { mode ->
                        val isSel = displayMode == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) Color(0xFF1E293B) else Color.Transparent)
                                .clickable { onDisplayModeChange(mode) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = mode.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) ElectricCyan else Color(0xFF64748B)
                            )
                        }
                    }
                }

                // Time Range Selector
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF131D33))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    ChartTimeRange.values().forEach { tr ->
                        val isSel = timeRange == tr
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) Color(0xFF1E293B) else Color.Transparent)
                                .clickable { onTimeRangeChange(tr) }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tr.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color.White else Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            // Server Focus Chips Carousel (when in Focus or Trend mode)
            AnimatedVisibility(visible = displayMode == ChartDisplayMode.FOCUSED_NODE || selectedFocusServerId != null) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = "Sélectionner le serveur à analyser:",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // All Servers (Reset focus)
                        val isAllSelected = selectedFocusServerId == null
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isAllSelected) ElectricCyan.copy(alpha = 0.2f) else Color(0xFF131D33))
                                .border(1.dp, if (isAllSelected) ElectricCyan else Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                .clickable { onFocusServerChange(null) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "🌐 Flotte Complète",
                                fontSize = 11.sp,
                                color = if (isAllSelected) ElectricCyan else Color(0xFFCBD5E1)
                            )
                        }

                        servers.forEach { server ->
                            val isSelected = selectedFocusServerId == server.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ElectricCyan.copy(alpha = 0.2f) else Color(0xFF131D33))
                                    .border(1.dp, if (isSelected) ElectricCyan else Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                    .clickable { onFocusServerChange(server.id) }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "${server.name} (${server.cpuUsagePercent.toInt()}%)",
                                    fontSize = 11.sp,
                                    color = if (isSelected) ElectricCyan else Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Canvas Linear Chart Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF080D1A))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = { offset ->
                                isTouching = true
                                touchXNormalized = (offset.x / size.width).coerceIn(0f, 1f)
                                tryAwaitRelease()
                                isTouching = false
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isTouching = true
                                touchXNormalized = (offset.x / size.width).coerceIn(0f, 1f)
                            },
                            onDragEnd = { isTouching = false },
                            onDragCancel = { isTouching = false },
                            onDrag = { change, _ ->
                                touchXNormalized = (change.position.x / size.width).coerceIn(0f, 1f)
                            }
                        )
                    }
            ) {
                val maxScale = selectedMetric.maxScale
                val primaryColor = ElectricCyan
                val peakColor = WarningAmber

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 32.dp, end = 12.dp, top = 16.dp, bottom = 24.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    // Draw Threshold Lines (80% Warning, 90% Critical) for CPU / RAM
                    if (selectedMetric == ChartMetricType.CPU || selectedMetric == ChartMetricType.RAM) {
                        val warnY = h - (80f / maxScale) * h
                        val critY = h - (90f / maxScale) * h

                        // Warning line 80%
                        drawLine(
                            color = WarningAmber.copy(alpha = 0.4f),
                            start = Offset(0f, warnY),
                            end = Offset(w, warnY),
                            strokeWidth = 1.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                        )

                        // Critical line 90%
                        drawLine(
                            color = CriticalRed.copy(alpha = 0.5f),
                            start = Offset(0f, critY),
                            end = Offset(w, critY),
                            strokeWidth = 1.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                        )
                    }

                    // Draw Horizontal Background Gridlines (0%, 25%, 50%, 75%, 100%)
                    val gridSteps = 4
                    for (i in 0..gridSteps) {
                        val y = h * (i / gridSteps.toFloat())
                        drawLine(
                            color = Color(0xFF162035),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                    }

                    // Multi-Server Mode Drawing
                    if (displayMode == ChartDisplayMode.MULTI_SERVER && multiServerSeries.isNotEmpty()) {
                        multiServerSeries.forEach { series ->
                            val pts = if (series.points.size >= 2) series.points else generateFallbackPoints(maxScale)
                            drawLinearPath(pts, maxScale, series.color, w, h, fillAlpha = 0.08f)
                        }
                    } else {
                        // Standard Mode: Average & Peak Line
                        val points = if (timeSeriesData.size >= 2) timeSeriesData else generateFallbackPoints(maxScale)

                        // Draw Peak Spikes Line if fleet mode
                        if (selectedFocusServerId == null && displayMode == ChartDisplayMode.FLEET_TRENDS) {
                            val peakPoints = points.map { TimeSeriesPoint(it.timestamp, it.maxValue, it.maxValue) }
                            drawLinearPath(peakPoints, maxScale, peakColor.copy(alpha = 0.7f), w, h, fillAlpha = 0.05f, strokeWidth = 2f)
                        }

                        // Draw Primary Average / Focused Server Curve
                        drawLinearPath(points, maxScale, primaryColor, w, h, fillAlpha = 0.25f, strokeWidth = 3.5f)
                    }

                    // Draw Scrubber / Crosshair when user touches canvas
                    if (isTouching && touchXNormalized in 0f..1f) {
                        val touchX = touchXNormalized * w
                        drawLine(
                            color = Color.White.copy(alpha = 0.7f),
                            start = Offset(touchX, 0f),
                            end = Offset(touchX, h),
                            strokeWidth = 1.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                        )

                        // Intersect dot
                        val pts = if (timeSeriesData.size >= 2) timeSeriesData else generateFallbackPoints(maxScale)
                        val idx = (touchXNormalized * (pts.size - 1)).toInt().coerceIn(0, pts.size - 1)
                        val targetPt = pts[idx]
                        val dotY = h - (targetPt.avgValue.coerceIn(0f, maxScale) / maxScale) * h

                        drawCircle(
                            color = primaryColor,
                            radius = 6f,
                            center = Offset(touchX, dotY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3f,
                            center = Offset(touchX, dotY)
                        )
                    }
                }

                // Y-Axis Scale Labels Overlay (Left margin)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 6.dp, top = 8.dp, bottom = 26.dp)
                        .height(146.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("100", fontSize = 8.sp, color = Color(0xFF64748B), fontFamily = FontFamily.Monospace)
                    Text("75", fontSize = 8.sp, color = Color(0xFF64748B), fontFamily = FontFamily.Monospace)
                    Text("50", fontSize = 8.sp, color = Color(0xFF64748B), fontFamily = FontFamily.Monospace)
                    Text("25", fontSize = 8.sp, color = Color(0xFF64748B), fontFamily = FontFamily.Monospace)
                    Text("0", fontSize = 8.sp, color = Color(0xFF64748B), fontFamily = FontFamily.Monospace)
                }

                // X-Axis Time Labels Overlay (Bottom margin)
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(start = 36.dp, end = 12.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("-${timeRange.label}", fontSize = 9.sp, color = Color(0xFF64748B))
                    Text("Télémétrie continue", fontSize = 9.sp, color = Color(0xFF475569))
                    Text("En Direct (Live)", fontSize = 9.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                }

                // Floating HUD Tooltip when scrubbing
                if (isTouching && touchXNormalized in 0f..1f) {
                    val pts = if (timeSeriesData.size >= 2) timeSeriesData else generateFallbackPoints(maxScale)
                    val idx = (touchXNormalized * (pts.size - 1)).toInt().coerceIn(0, pts.size - 1)
                    val currentPoint = pts[idx]
                    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
                    val timeStr = timeFormat.format(Date(currentPoint.timestamp))

                    Surface(
                        modifier = Modifier
                            .align(if (touchXNormalized > 0.6f) Alignment.TopStart else Alignment.TopEnd)
                            .padding(top = 10.dp, start = 40.dp, end = 16.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B).copy(alpha = 0.95f),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(ElectricCyan, NeonGreen)))
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                            Text(text = "Temps: $timeStr", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = "Valeur: ${currentPoint.avgValue.toInt()} ${selectedMetric.unit}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                            if (selectedFocusServerId == null) {
                                Text(
                                    text = "Pic Max: ${currentPoint.maxValue.toInt()} ${selectedMetric.unit}",
                                    fontSize = 10.sp,
                                    color = WarningAmber
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Multi-Server Legend (when in Multi-Server mode)
            if (displayMode == ChartDisplayMode.MULTI_SERVER && multiServerSeries.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    multiServerSeries.forEach { series ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(series.color, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${series.server.name}: ${series.server.cpuUsagePercent.toInt()}%",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Realtime Statistics Footer Row (Min / Avg / Max / Health Status)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF131D33))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(label = "Minimum", value = "${minReading.toInt()}${selectedMetric.unit}", color = NeonGreen)
                StatItem(label = "Moyenne", value = "${avgReading.toInt()}${selectedMetric.unit}", color = ElectricCyan)
                StatItem(label = "Pic Max", value = "${maxReading.toInt()}${selectedMetric.unit}", color = if (maxReading > 80f) CriticalRed else WarningAmber)

                // Fleet Saturation Status Tag
                val saturationText = when {
                    maxReading >= 90f -> "Critique ⚠️"
                    maxReading >= 75f -> "Élevée ⚡"
                    else -> "Nominale ✓"
                }
                val saturationColor = when {
                    maxReading >= 90f -> CriticalRed
                    maxReading >= 75f -> WarningAmber
                    else -> NeonGreen
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("État de Charge", fontSize = 9.sp, color = Color(0xFF64748B))
                    Text(
                        text = saturationText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = saturationColor
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column {
        Text(label, fontSize = 9.sp, color = Color(0xFF64748B))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = color
        )
    }
}

// Canvas Drawing Helper for Smooth Linear Path & Gradient Fill
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLinearPath(
    points: List<TimeSeriesPoint>,
    maxScale: Float,
    lineColor: Color,
    w: Float,
    h: Float,
    fillAlpha: Float = 0.2f,
    strokeWidth: Float = 3f
) {
    if (points.size < 2) return

    val stepX = w / (points.size - 1)
    val strokePath = Path()
    val fillPath = Path()

    val firstY = h - (points[0].avgValue.coerceIn(0f, maxScale) / maxScale) * h
    strokePath.moveTo(0f, firstY)
    fillPath.moveTo(0f, h)
    fillPath.lineTo(0f, firstY)

    for (i in 0 until points.size - 1) {
        val currentX = i * stepX
        val currentY = h - (points[i].avgValue.coerceIn(0f, maxScale) / maxScale) * h
        val nextX = (i + 1) * stepX
        val nextY = h - (points[i + 1].avgValue.coerceIn(0f, maxScale) / maxScale) * h

        val controlX1 = currentX + (nextX - currentX) / 2
        val controlY1 = currentY
        val controlX2 = currentX + (nextX - currentX) / 2
        val controlY2 = nextY

        strokePath.cubicTo(controlX1, controlY1, controlX2, controlY2, nextX, nextY)
        fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, nextX, nextY)
    }

    val lastX = (points.size - 1) * stepX
    val lastY = h - (points.last().avgValue.coerceIn(0f, maxScale) / maxScale) * h

    fillPath.lineTo(lastX, h)
    fillPath.close()

    // Draw Gradient Background Fill
    drawPath(
        path = fillPath,
        brush = Brush.verticalGradient(
            colors = listOf(lineColor.copy(alpha = fillAlpha), Color.Transparent),
            startY = 0f,
            endY = h
        )
    )

    // Draw Stroke Line
    drawPath(
        path = strokePath,
        color = lineColor,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )

    // Draw Glowing Head Circle at last live point
    drawCircle(
        color = lineColor.copy(alpha = 0.35f),
        radius = 8f,
        center = Offset(lastX, lastY)
    )
    drawCircle(
        color = Color.White,
        radius = 4f,
        center = Offset(lastX, lastY)
    )
}

private fun generateFallbackPoints(maxScale: Float): List<TimeSeriesPoint> {
    val now = System.currentTimeMillis()
    val base = listOf(35f, 42f, 38f, 55f, 48f, 62f, 58f, 74f, 65f, 50f, 58f)
    return base.mapIndexed { idx, v ->
        TimeSeriesPoint(
            timestamp = now - (base.size - idx) * 10_000L,
            avgValue = v.coerceIn(0f, maxScale),
            maxValue = (v + 15f).coerceIn(0f, maxScale)
        )
    }
}

data class TimeSeriesPoint(
    val timestamp: Long,
    val avgValue: Float,
    val maxValue: Float
)

data class ServerSeries(
    val server: ServerEntity,
    val color: Color,
    val points: List<TimeSeriesPoint>
)
