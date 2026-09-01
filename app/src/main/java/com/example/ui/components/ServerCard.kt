package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ServerEntity
import com.example.data.model.ServerStatus
import com.example.data.model.ServerType
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServerCard(
    server: ServerEntity,
    onClick: () -> Unit,
    onReboot: () -> Unit,
    onToggleMaintenance: () -> Unit,
    onOpenTerminal: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val statusColor = when (server.status) {
        ServerStatus.ONLINE -> NeonGreen
        ServerStatus.WARNING -> WarningAmber
        ServerStatus.CRITICAL -> CriticalRed
        ServerStatus.MAINTENANCE -> PurpleAccent
    }

    val typeIcon: ImageVector = when (server.serverType) {
        ServerType.WEB_SERVER -> Icons.Default.Dns
        ServerType.DATABASE_CLUSTER -> Icons.Default.Storage
        ServerType.KUBERNETES_NODE -> Icons.Default.Router
        ServerType.LOAD_BALANCER -> Icons.Default.Speed
        ServerType.REDIS_CACHE -> Icons.Default.Memory
        ServerType.AI_COMPUTE -> Icons.Default.Computer
        ServerType.DNS_GATEWAY -> Icons.Default.Cloud
        ServerType.STORAGE_SAN -> Icons.Default.Storage
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("server_card_${server.id}"),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF131D33)
        ),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (server.status == ServerStatus.CRITICAL) CriticalRed.copy(alpha = 0.6f)
                else Color(0xFF223252)
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header Row: Type Icon, Server Name & Hostname, Status & Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = server.serverType.label,
                            tint = statusColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = server.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                        Text(
                            text = server.hostname,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF94A3B8),
                            maxLines = 1
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(statusColor.copy(alpha = 0.18f))
                            .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(statusColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = server.status.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }

                    // Context Menu
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(36.dp).testTag("server_menu_button_${server.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Actions",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(Color(0xFF1E293B))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Console SSH", color = ElectricCyan) },
                                leadingIcon = { Icon(Icons.Default.Terminal, contentDescription = null, tint = ElectricCyan) },
                                onClick = {
                                    menuExpanded = false
                                    onOpenTerminal()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Redémarrer", color = WarningAmber) },
                                leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null, tint = WarningAmber) },
                                onClick = {
                                    menuExpanded = false
                                    onReboot()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (server.status == ServerStatus.MAINTENANCE) "Quitter Maintenance" else "Mode Maintenance",
                                        color = PurpleAccent
                                    )
                                },
                                leadingIcon = { Icon(Icons.Default.Build, contentDescription = null, tint = PurpleAccent) },
                                onClick = {
                                    menuExpanded = false
                                    onToggleMaintenance()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Supprimer", color = CriticalRed) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = CriticalRed) },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // IP, Provider, Region tags
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TagChip(text = server.ipAddress, color = Color(0xFF64748B), isMono = true)
                TagChip(text = server.provider.code, color = ElectricCyan)
                TagChip(text = server.region.city.split(" ").first(), color = Color(0xFF38BDF8))
                TagChip(text = "${server.latencyMs} ms", color = if (server.latencyMs > 100) WarningAmber else NeonGreen)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Telemetry Progress: CPU & RAM
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // CPU Metric
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "CPU", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
                        Text(
                            text = "${server.cpuUsagePercent.toInt()}%",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (server.cpuUsagePercent > 85) CriticalRed else Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { server.cpuUsagePercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (server.cpuUsagePercent > 85) CriticalRed else if (server.cpuUsagePercent > 70) WarningAmber else ElectricCyan,
                        trackColor = Color(0xFF1E293B)
                    )
                }

                // RAM Metric
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "RAM (${server.ramGb}G)", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
                        Text(
                            text = "${server.ramUsagePercent.toInt()}%",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (server.ramUsagePercent > 88) CriticalRed else Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { server.ramUsagePercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (server.ramUsagePercent > 88) CriticalRed else if (server.ramUsagePercent > 75) WarningAmber else NeonGreen,
                        trackColor = Color(0xFF1E293B)
                    )
                }
            }
        }
    }
}

@Composable
fun TagChip(
    text: String,
    color: Color,
    isMono: Boolean = false
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .border(0.5.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontFamily = if (isMono) FontFamily.Monospace else FontFamily.Default,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}
