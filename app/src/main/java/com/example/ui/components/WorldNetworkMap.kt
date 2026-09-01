package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ServerEntity
import com.example.data.model.ServerStatus
import com.example.data.model.WorldRegion
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorldNetworkMap(
    servers: List<ServerEntity>,
    selectedRegion: WorldRegion?,
    onRegionSelected: (WorldRegion?) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mapPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    // Compute region server aggregations
    val serversByRegion = remember(servers) {
        servers.groupBy { it.region }
    }

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
                listOf(Color(0xFF1E293B), ElectricCyan.copy(alpha = 0.3f), Color(0xFF1E293B))
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(ElectricCyan.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "Carte Mondiale",
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Réseau Mondial des Datacenters",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${WorldRegion.values().size} Régions actives • ${servers.size} Nœuds surveillés",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                if (selectedRegion != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(ElectricCyan.copy(alpha = 0.2f))
                            .clickable { onRegionSelected(null) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Tout voir ✕",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ElectricCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas World Map with Nodes & Interconnects
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF070B14))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { tapOffset ->
                                val w = size.width
                                val h = size.height
                                var tappedRegion: WorldRegion? = null
                                var minDistance = Float.MAX_VALUE

                                WorldRegion.values().forEach { region ->
                                    val nx = region.lonPercent * w
                                    val ny = region.latPercent * h
                                    val dist = kotlin.math.hypot(tapOffset.x - nx, tapOffset.y - ny)
                                    if (dist < 40f && dist < minDistance) {
                                        minDistance = dist
                                        tappedRegion = region
                                    }
                                }

                                if (tappedRegion != null) {
                                    onRegionSelected(if (selectedRegion == tappedRegion) null else tappedRegion)
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height

                    // Draw Latitude / Longitude cyber grid lines
                    val gridPaint = Color(0xFF132035)
                    for (i in 1..5) {
                        val y = h * (i / 6f)
                        drawLine(
                            color = gridPaint,
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                        )
                    }
                    for (i in 1..7) {
                        val x = w * (i / 8f)
                        drawLine(
                            color = gridPaint,
                            start = Offset(x, 0f),
                            end = Offset(x, h),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                        )
                    }

                    // Stylized Continent shapes/silhouettes in background
                    val continentColor = Color(0xFF111E33)
                    // North America
                    drawCircle(continentColor, radius = w * 0.14f, center = Offset(w * 0.22f, h * 0.35f))
                    // South America
                    drawCircle(continentColor, radius = w * 0.09f, center = Offset(w * 0.34f, h * 0.73f))
                    // Europe
                    drawCircle(continentColor, radius = w * 0.10f, center = Offset(w * 0.51f, h * 0.28f))
                    // Africa
                    drawCircle(continentColor, radius = w * 0.11f, center = Offset(w * 0.54f, h * 0.62f))
                    // Asia
                    drawCircle(continentColor, radius = w * 0.18f, center = Offset(w * 0.77f, h * 0.38f))
                    // Australia
                    drawCircle(continentColor, radius = w * 0.08f, center = Offset(w * 0.88f, h * 0.76f))

                    // Draw Interconnect Arcs between key Datacenters
                    val connections = listOf(
                        WorldRegion.US_WEST to WorldRegion.US_EAST,
                        WorldRegion.US_EAST to WorldRegion.EU_WEST,
                        WorldRegion.EU_WEST to WorldRegion.EU_CENTRAL,
                        WorldRegion.EU_CENTRAL to WorldRegion.AP_EAST,
                        WorldRegion.AP_EAST to WorldRegion.AP_SOUTH,
                        WorldRegion.AP_SOUTH to WorldRegion.AP_SOUTHEAST,
                        WorldRegion.US_EAST to WorldRegion.SA_EAST,
                        WorldRegion.EU_WEST to WorldRegion.AF_SOUTH
                    )

                    connections.forEach { (from, to) ->
                        val fx = from.lonPercent * w
                        val fy = from.latPercent * h
                        val tx = to.lonPercent * w
                        val ty = to.latPercent * h

                        val path = Path().apply {
                            moveTo(fx, fy)
                            val midX = (fx + tx) / 2
                            val midY = (fy + ty) / 2 - 25f
                            quadraticBezierTo(midX, midY, tx, ty)
                        }

                        val isHighlighted = selectedRegion == null || selectedRegion == from || selectedRegion == to
                        val arcColor = if (isHighlighted) ElectricCyan.copy(alpha = 0.45f) else Color(0xFF1E293B)

                        drawPath(
                            path = path,
                            color = arcColor,
                            style = Stroke(
                                width = if (isHighlighted) 2f else 1f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f),
                                cap = StrokeCap.Round
                            )
                        )
                    }

                    // Draw Region Datacenter Nodes
                    WorldRegion.values().forEach { region ->
                        val nx = region.lonPercent * w
                        val ny = region.latPercent * h
                        val regionServers = serversByRegion[region] ?: emptyList()

                        val isSelected = selectedRegion == region
                        val hasCritical = regionServers.any { it.status == ServerStatus.CRITICAL }
                        val hasWarning = regionServers.any { it.status == ServerStatus.WARNING }

                        val nodeColor = when {
                            hasCritical -> CriticalRed
                            hasWarning -> WarningAmber
                            regionServers.isNotEmpty() -> NeonGreen
                            else -> Color(0xFF64748B)
                        }

                        // Pulsing Beacon around active node
                        if (regionServers.isNotEmpty()) {
                            drawCircle(
                                color = nodeColor.copy(alpha = pulseAlpha),
                                radius = pulseRadius + if (isSelected) 6f else 0f,
                                center = Offset(nx, ny),
                                style = Stroke(width = 2f)
                            )
                        }

                        // Outer Glow
                        drawCircle(
                            color = nodeColor.copy(alpha = if (isSelected) 0.6f else 0.25f),
                            radius = if (isSelected) 12f else 8f,
                            center = Offset(nx, ny)
                        )

                        // Core Node
                        drawCircle(
                            color = if (isSelected) Color.White else nodeColor,
                            radius = if (isSelected) 6.5f else 4.5f,
                            center = Offset(nx, ny)
                        )

                        // If selected, draw indicator ring
                        if (isSelected) {
                            drawCircle(
                                color = ElectricCyan,
                                radius = 16f,
                                center = Offset(nx, ny),
                                style = Stroke(width = 2.5f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Region Filter Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterChip(
                    selected = selectedRegion == null,
                    onClick = { onRegionSelected(null) },
                    label = { Text("Tous (${servers.size})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricCyan,
                        selectedLabelColor = Color(0xFF0A0F1D),
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFF94A3B8)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                WorldRegion.values().forEach { region ->
                    val regionServers = serversByRegion[region] ?: emptyList()
                    val hasCritical = regionServers.any { it.status == ServerStatus.CRITICAL }
                    val isSelected = selectedRegion == region

                    FilterChip(
                        selected = isSelected,
                        onClick = { onRegionSelected(if (isSelected) null else region) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(
                                            if (hasCritical) CriticalRed else if (regionServers.isNotEmpty()) NeonGreen else Color.Gray,
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "${region.city.split(" ").first()} (${regionServers.size})",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricCyan,
                            selectedLabelColor = Color(0xFF0A0F1D),
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }
    }
}
