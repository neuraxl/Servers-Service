package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.IncidentAlertEntity
import com.example.data.local.LogEventEntity
import com.example.data.local.MetricHistoryEntity
import com.example.data.local.ServerEntity
import com.example.data.model.AlertSeverity
import com.example.data.model.ChartDisplayMode
import com.example.data.model.ChartMetricType
import com.example.data.model.ChartTimeRange
import com.example.data.model.HealthFilterOption
import com.example.data.model.ServerSortCriteria
import com.example.data.model.ServerStatus
import com.example.data.model.SortDirection
import com.example.data.model.WorldRegion
import com.example.ui.components.CpuTelemetryLinearChart
import com.example.ui.components.DashboardFilterSortBar
import com.example.ui.components.ServerCard
import com.example.ui.components.TelemetryControlBar
import com.example.ui.components.WorldNetworkMap
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.WarningAmber

@Composable
fun DashboardOverviewScreen(
    servers: List<ServerEntity>,
    dashboardDisplayServers: List<ServerEntity>,
    activeAlerts: List<IncidentAlertEntity>,
    recentLogs: List<LogEventEntity>,
    isAutoRefresh: Boolean,
    isRefreshing: Boolean,
    refreshIntervalMs: Long,
    ticksCount: Long,
    lastRefreshTime: Long,
    onToggleAutoRefresh: () -> Unit,
    onSelectInterval: (Long) -> Unit,
    onManualRefresh: () -> Unit,
    onSimulateSpike: () -> Unit,
    healthFilter: HealthFilterOption,
    onHealthFilterChange: (HealthFilterOption) -> Unit,
    sortCriteria: ServerSortCriteria,
    onSortCriteriaChange: (ServerSortCriteria) -> Unit,
    sortDirection: SortDirection,
    onToggleSortDirection: () -> Unit,
    dashboardSearchQuery: String,
    onDashboardSearchQueryChange: (String) -> Unit,
    onResetFilters: () -> Unit,
    selectedRegion: WorldRegion?,
    onSelectRegion: (WorldRegion?) -> Unit,
    metricsHistory: List<MetricHistoryEntity>,
    chartMetric: ChartMetricType,
    onChartMetricChange: (ChartMetricType) -> Unit,
    chartTimeRange: ChartTimeRange,
    onChartTimeRangeChange: (ChartTimeRange) -> Unit,
    chartDisplayMode: ChartDisplayMode,
    onChartDisplayModeChange: (ChartDisplayMode) -> Unit,
    chartFocusServerId: Long?,
    onChartFocusServerChange: (Long?) -> Unit,
    onSelectServer: (Long) -> Unit,
    onAddServerClick: () -> Unit,
    onRebootServer: (Long) -> Unit,
    onToggleMaintenance: (Long) -> Unit,
    onOpenTerminal: (Long) -> Unit,
    onDeleteServer: (Long) -> Unit,
    onNavigateToAlerts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val onlineCount = servers.count { it.status == ServerStatus.ONLINE }
    val warningCount = servers.count { it.status == ServerStatus.WARNING }
    val criticalCount = servers.count { it.status == ServerStatus.CRITICAL }
    val maintenanceCount = servers.count { it.status == ServerStatus.MAINTENANCE }

    val avgLatency = if (servers.isNotEmpty()) servers.map { it.latencyMs }.average().toInt() else 0
    val totalBwIn = servers.sumOf { it.bandwidthInMbps.toDouble() } / 1000.0 // in Gbps
    val totalBwOut = servers.sumOf { it.bandwidthOutMbps.toDouble() } / 1000.0 // in Gbps

    val topLoadedServers = remember(servers) {
        servers.sortedByDescending { it.cpuUsagePercent + it.ramUsagePercent }.take(3)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Periodic Telemetry Refresh Control Bar (ViewModel driven)
        item {
            Spacer(modifier = Modifier.height(4.dp))
            TelemetryControlBar(
                isAutoRefresh = isAutoRefresh,
                isRefreshing = isRefreshing,
                refreshIntervalMs = refreshIntervalMs,
                ticksCount = ticksCount,
                lastRefreshTime = lastRefreshTime,
                onToggleAutoRefresh = onToggleAutoRefresh,
                onSelectInterval = onSelectInterval,
                onManualRefresh = onManualRefresh,
                onSimulateSpike = onSimulateSpike
            )
        }

        // Global Network Summary KPI Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    title = "Nœuds Actifs",
                    value = "$onlineCount / ${servers.size}",
                    subtitle = "${((onlineCount.toFloat() / servers.size.coerceAtLeast(1)) * 100).toInt()}% Dispo",
                    icon = Icons.Default.CheckCircle,
                    accentColor = NeonGreen,
                    modifier = Modifier.weight(1f)
                )

                KpiCard(
                    title = "Latence Moyenne",
                    value = "$avgLatency ms",
                    subtitle = "Réseau WAN Global",
                    icon = Icons.Default.Speed,
                    accentColor = if (avgLatency > 80) WarningAmber else ElectricCyan,
                    modifier = Modifier.weight(1f)
                )

                KpiCard(
                    title = "Trafic Réseau",
                    value = String.format("%.1f G", totalBwIn + totalBwOut),
                    subtitle = "IN / OUT (Gbps)",
                    icon = Icons.Default.NetworkCheck,
                    accentColor = InfoBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Active Incident Banner (if any critical or warnings)
        if (activeAlerts.isNotEmpty()) {
            item {
                val topAlert = activeAlerts.first()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onNavigateToAlerts)
                        .testTag("incident_banner"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (topAlert.severity == AlertSeverity.CRITICAL) CriticalRed.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(
                                if (topAlert.severity == AlertSeverity.CRITICAL) CriticalRed else WarningAmber,
                                Color.Transparent
                            )
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (topAlert.severity == AlertSeverity.CRITICAL) CriticalRed else WarningAmber,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${activeAlerts.size} ALERTE(S) ACTIVE(S)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (topAlert.severity == AlertSeverity.CRITICAL) CriticalRed else WarningAmber
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ${topAlert.serverName}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Text(
                                text = topAlert.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                        }

                        Text(
                            text = "Gérer →",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }
                }
            }
        }

        // Interactive World Map Component
        item {
            WorldNetworkMap(
                servers = servers,
                selectedRegion = selectedRegion,
                onRegionSelected = onSelectRegion,
                modifier = Modifier.testTag("world_network_map")
            )
        }

        // Data Visualization: Server CPU & Telemetry Linear Charts
        item {
            CpuTelemetryLinearChart(
                servers = servers,
                metricsHistory = metricsHistory,
                selectedMetric = chartMetric,
                onMetricChange = onChartMetricChange,
                timeRange = chartTimeRange,
                onTimeRangeChange = onChartTimeRangeChange,
                displayMode = chartDisplayMode,
                onDisplayModeChange = onChartDisplayModeChange,
                selectedFocusServerId = chartFocusServerId,
                onFocusServerChange = onChartFocusServerChange,
                modifier = Modifier.testTag("cpu_telemetry_linear_chart")
            )
        }

        // Fleet Health Status Breakdown Row (Interactive filter shortcuts)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusPill(
                    label = "En Ligne",
                    count = onlineCount,
                    color = NeonGreen,
                    isSelected = healthFilter == HealthFilterOption.ONLINE_ONLY,
                    onClick = {
                        onHealthFilterChange(
                            if (healthFilter == HealthFilterOption.ONLINE_ONLY) HealthFilterOption.ALL else HealthFilterOption.ONLINE_ONLY
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
                StatusPill(
                    label = "Avertissement",
                    count = warningCount,
                    color = WarningAmber,
                    isSelected = healthFilter == HealthFilterOption.WARNING_ONLY,
                    onClick = {
                        onHealthFilterChange(
                            if (healthFilter == HealthFilterOption.WARNING_ONLY) HealthFilterOption.ALL else HealthFilterOption.WARNING_ONLY
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
                StatusPill(
                    label = "Critique",
                    count = criticalCount,
                    color = CriticalRed,
                    isSelected = healthFilter == HealthFilterOption.CRITICAL_ONLY,
                    onClick = {
                        onHealthFilterChange(
                            if (healthFilter == HealthFilterOption.CRITICAL_ONLY) HealthFilterOption.ALL else HealthFilterOption.CRITICAL_ONLY
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
                StatusPill(
                    label = "Maintenance",
                    count = maintenanceCount,
                    color = PurpleAccent,
                    isSelected = healthFilter == HealthFilterOption.MAINTENANCE_ONLY,
                    onClick = {
                        onHealthFilterChange(
                            if (healthFilter == HealthFilterOption.MAINTENANCE_ONLY) HealthFilterOption.ALL else HealthFilterOption.MAINTENANCE_ONLY
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Filtering & Sorting Control Bar at top of server fleet
        item {
            DashboardFilterSortBar(
                servers = servers,
                healthFilter = healthFilter,
                onHealthFilterChange = onHealthFilterChange,
                sortCriteria = sortCriteria,
                onSortCriteriaChange = onSortCriteriaChange,
                sortDirection = sortDirection,
                onToggleSortDirection = onToggleSortDirection,
                searchQuery = dashboardSearchQuery,
                onSearchQueryChange = onDashboardSearchQueryChange,
                onResetFilters = onResetFilters,
                selectedRegion = selectedRegion,
                onClearRegion = { onSelectRegion(null) }
            )
        }

        // Section Title: Filtered Server Nodes
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (selectedRegion != null) "Serveurs • ${selectedRegion.city}" else "Nœuds du Réseau Mondial",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${dashboardDisplayServers.size} serveur(s) affiché(s) • Tri: ${sortCriteria.shortLabel}",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Button(
                    onClick = onAddServerClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = Color(0xFF0A0F1D)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_add_server_dashboard")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ajouter", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Server Cards List (dynamically filtered and sorted)
        if (dashboardDisplayServers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF10192C))
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Aucun serveur ne correspond aux critères sélectionnés.",
                            color = Color(0xFF94A3B8),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = onResetFilters,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E293B),
                                contentColor = ElectricCyan
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Réinitialiser les filtres", fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            items(dashboardDisplayServers, key = { it.id }) { server ->
                ServerCard(
                    server = server,
                    onClick = { onSelectServer(server.id) },
                    onReboot = { onRebootServer(server.id) },
                    onToggleMaintenance = { onToggleMaintenance(server.id) },
                    onOpenTerminal = { onOpenTerminal(server.id) },
                    onDelete = { onDeleteServer(server.id) }
                )
            }
        }

        // Section: Live System Event Feed
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Flux Télémétrique & Journal en Direct",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        items(recentLogs.take(5), key = { it.id }) { log ->
            LogEventRow(log)
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF131D33)
        ),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1E293B))
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(accentColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color.White
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = accentColor
            )
        }
    }
}

@Composable
fun StatusPill(
    label: String,
    count: Int,
    color: Color,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) color.copy(alpha = 0.2f) else Color(0xFF131D33)
            )
            .border(
                if (isSelected) 1.5.dp else 1.dp,
                if (isSelected) color else color.copy(alpha = 0.3f),
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = count.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = color
            )
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                maxLines = 1
            )
        }
    }
}

@Composable
fun LogEventRow(log: LogEventEntity) {
    val levelColor = when (log.level) {
        "SUCCESS" -> NeonGreen
        "ERROR" -> CriticalRed
        "WARN" -> WarningAmber
        else -> ElectricCyan
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(levelColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "[${log.source}]",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = levelColor
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = log.message,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFCBD5E1),
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
    }
}
