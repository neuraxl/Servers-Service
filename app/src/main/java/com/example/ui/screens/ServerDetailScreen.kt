package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LogEventEntity
import com.example.data.local.MetricHistoryEntity
import com.example.data.local.ServerEntity
import com.example.data.model.ServerStatus
import com.example.ui.components.LiveMetricChart
import com.example.ui.components.TagChip
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServerDetailScreen(
    server: ServerEntity,
    metricsHistory: List<MetricHistoryEntity>,
    serverLogs: List<LogEventEntity>,
    aiDiagnosisResult: String?,
    isDiagnosing: Boolean,
    onBack: () -> Unit,
    onOpenTerminal: () -> Unit,
    onReboot: () -> Unit,
    onToggleMaintenance: () -> Unit,
    onRestartService: (String) -> Unit,
    onRunAiDiagnosis: () -> Unit,
    onDelete: () -> Unit,
    onManualRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var serviceMenuExpanded by remember { mutableStateOf(false) }

    val statusColor = when (server.status) {
        ServerStatus.ONLINE -> NeonGreen
        ServerStatus.WARNING -> WarningAmber
        ServerStatus.CRITICAL -> CriticalRed
        ServerStatus.MAINTENANCE -> PurpleAccent
    }

    val cpuHistory = remember(metricsHistory, server.cpuUsagePercent) {
        val list = metricsHistory.map { it.cpuPercent }.takeLast(20)
        if (list.size < 2) listOf(20f, server.cpuUsagePercent) else list
    }

    val ramHistory = remember(metricsHistory, server.ramUsagePercent) {
        val list = metricsHistory.map { it.ramPercent }.takeLast(20)
        if (list.size < 2) listOf(30f, server.ramUsagePercent) else list
    }

    val latencyHistory = remember(metricsHistory, server.latencyMs) {
        val list = metricsHistory.map { it.latencyMs.toFloat() }.takeLast(20)
        if (list.size < 2) listOf(15f, server.latencyMs.toFloat()) else list
    }

    val uptimeDays = server.uptimeSeconds / 86400
    val uptimeHours = (server.uptimeSeconds % 86400) / 3600
    val uptimeMinutes = (server.uptimeSeconds % 3600) / 60

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Navigation & Title Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${server.hostname} • ${server.ipAddress}",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF94A3B8)
                    )
                }

                // Refresh Button
                IconButton(
                    onClick = onManualRefresh,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .testTag("detail_btn_refresh")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Rafraîchir les métriques",
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusColor.copy(alpha = 0.2f))
                        .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(7.dp).background(statusColor, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = server.status.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }
        }

        // Tags bar
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TagChip(text = "Région: ${server.region.city}", color = ElectricCyan)
                TagChip(text = "Cloud: ${server.provider.label}", color = InfoBlue)
                TagChip(text = server.serverType.label, color = PurpleAccent)
                TagChip(text = "SSH Port: ${server.sshPort}", color = Color(0xFF64748B), isMono = true)
                TagChip(text = "Uptime: ${uptimeDays}j ${uptimeHours}h ${uptimeMinutes}m", color = NeonGreen)
            }
        }

        // Primary Action Controls Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Open SSH Terminal
                Button(
                    onClick = onOpenTerminal,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = Color(0xFF0A0F1D)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).testTag("btn_detail_ssh_terminal")
                ) {
                    Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SSH Console", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // AI Telemetry Diagnostics
                Button(
                    onClick = onRunAiDiagnosis,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurpleAccent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).testTag("btn_detail_ai_diagnosis")
                ) {
                    if (isDiagnosing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Diagnostic IA", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Secondary Action Controls Row (Restart Services, Reboot, Maintenance)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Restart Service Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { serviceMenuExpanded = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF334155))
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Services", color = ElectricCyan, fontSize = 12.sp)
                    }

                    DropdownMenu(
                        expanded = serviceMenuExpanded,
                        onDismissRequest = { serviceMenuExpanded = false },
                        modifier = Modifier.background(Color(0xFF1E293B))
                    ) {
                        listOf("nginx", "docker", "postgresql", "redis-server", "kubelet", "sshd").forEach { srv ->
                            DropdownMenuItem(
                                text = { Text("Relancer $srv", color = Color.White, fontFamily = FontFamily.Monospace) },
                                onClick = {
                                    serviceMenuExpanded = false
                                    onRestartService(srv)
                                }
                            )
                        }
                    }
                }

                // Reboot Server
                OutlinedButton(
                    onClick = onReboot,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(WarningAmber.copy(alpha = 0.4f))
                    )
                ) {
                    Text("Redémarrer", color = WarningAmber, fontSize = 12.sp)
                }

                // Maintenance
                OutlinedButton(
                    onClick = onToggleMaintenance,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(PurpleAccent.copy(alpha = 0.4f))
                    )
                ) {
                    Text(
                        if (server.status == ServerStatus.MAINTENANCE) "En Ligne" else "Maint.",
                        color = PurpleAccent,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // AI Diagnosis Report (if generated)
        if (aiDiagnosisResult != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF141936)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(
                            listOf(PurpleAccent, ElectricCyan, PurpleAccent)
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = PurpleAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Audit Télémétrique & Diagnostic IA",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = aiDiagnosisResult,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Live Telemetry Bezier Charts
        item {
            Text(
                text = "Télémétrie en Temps Réel",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // CPU Usage Chart
        item {
            LiveMetricChart(
                title = "Utilisation Processeur CPU (${server.cpuCores} Cœurs)",
                currentValue = "${server.cpuUsagePercent.toInt()}%",
                unit = "Charge globale",
                dataPoints = cpuHistory,
                lineColor = if (server.cpuUsagePercent > 85) CriticalRed else if (server.cpuUsagePercent > 70) WarningAmber else ElectricCyan,
                fillColor = ElectricCyan.copy(alpha = 0.25f),
                maxScale = 100f
            )
        }

        // RAM Usage Chart
        item {
            LiveMetricChart(
                title = "Mémoire Vive RAM (${server.ramGb} Go Alloués)",
                currentValue = "${server.ramUsagePercent.toInt()}%",
                unit = "${(server.ramGb * server.ramUsagePercent / 100).toInt()} Go utilisés",
                dataPoints = ramHistory,
                lineColor = if (server.ramUsagePercent > 88) CriticalRed else NeonGreen,
                fillColor = NeonGreen.copy(alpha = 0.25f),
                maxScale = 100f
            )
        }

        // Latency Chart
        item {
            LiveMetricChart(
                title = "Latence Réseau Aller-Retour",
                currentValue = "${server.latencyMs}",
                unit = "ms (RTT)",
                dataPoints = latencyHistory,
                lineColor = if (server.latencyMs > 120) WarningAmber else InfoBlue,
                fillColor = InfoBlue.copy(alpha = 0.25f),
                maxScale = 250f
            )
        }

        // Hardware Specifications Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111C30)),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1E293B))
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Spécifications & Matériel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    SpecRow(label = "Système d'exploitation", value = server.osName)
                    SpecRow(label = "Processeurs / VCPU", value = "${server.cpuCores} Cœurs x86_64")
                    SpecRow(label = "Mémoire Vive", value = "${server.ramGb} Go DDR5 ECC")
                    SpecRow(label = "Stockage NVMe SSD", value = "${server.diskGb} Go (${server.diskUsagePercent.toInt()}% occupé)")
                    SpecRow(label = "Bande passante Entrante", value = "${server.bandwidthInMbps.toInt()} Mbps")
                    SpecRow(label = "Bande passante Sortante", value = "${server.bandwidthOutMbps.toInt()} Mbps")
                    SpecRow(label = "Connexions Actives", value = "${server.activeConnections} TCP/UDP")
                }
            }
        }

        // Live System Event Logs for this Server
        item {
            Text(
                text = "Journal d'Événements du Serveur",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (serverLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aucun événement récent enregistré.", color = Color(0xFF64748B), fontSize = 12.sp)
                }
            }
        } else {
            items(serverLogs.take(6), key = { it.id }) { log ->
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
                    Box(modifier = Modifier.size(6.dp).background(levelColor, CircleShape))
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
        }

        // Delete server button at bottom
        item {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onDelete,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CriticalRed),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(CriticalRed.copy(alpha = 0.4f))
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("btn_delete_server")
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Décommissionner et supprimer le serveur", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF94A3B8))
        Text(text = value, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}
