package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import com.example.data.local.IncidentAlertEntity
import com.example.data.model.AlertSeverity
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun IncidentCenterScreen(
    allAlerts: List<IncidentAlertEntity>,
    onAcknowledgeAlert: (Long) -> Unit,
    onResolveAlert: (Long) -> Unit,
    onClearResolved: () -> Unit,
    onSelectServer: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showOnlyActive by remember { mutableStateOf(true) }

    val displayedAlerts = remember(allAlerts, showOnlyActive) {
        if (showOnlyActive) allAlerts.filter { !it.isResolved } else allAlerts
    }

    val activeCount = allAlerts.count { !it.isResolved }
    val resolvedCount = allAlerts.count { it.isResolved }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(CriticalRed.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = CriticalRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Centre d'Incidents & Alertes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "$activeCount incident(s) en cours d'investigation",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                if (resolvedCount > 0) {
                    OutlinedButton(
                        onClick = onClearResolved,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_clear_resolved_alerts")
                    ) {
                        Icon(Icons.Default.ClearAll, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF94A3B8))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Purger résolus", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                }
            }
        }

        // Filter Tabs (Active vs All)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = showOnlyActive,
                    onClick = { showOnlyActive = true },
                    label = { Text("Actifs uniquement ($activeCount)", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (activeCount > 0) CriticalRed else ElectricCyan,
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFF94A3B8)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                FilterChip(
                    selected = !showOnlyActive,
                    onClick = { showOnlyActive = false },
                    label = { Text("Tous les incidents (${allAlerts.size})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF334155),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFF94A3B8)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // Empty state
        if (displayedAlerts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tous les systèmes sont nominaux.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Aucun incident ou anomalie réseau détectée.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        } else {
            items(displayedAlerts, key = { it.id }) { alert ->
                IncidentAlertCard(
                    alert = alert,
                    onAcknowledge = { onAcknowledgeAlert(alert.id) },
                    onResolve = { onResolveAlert(alert.id) },
                    onSelectServer = {
                        if (alert.serverId != null) {
                            onSelectServer(alert.serverId)
                        }
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun IncidentAlertCard(
    alert: IncidentAlertEntity,
    onAcknowledge: () -> Unit,
    onResolve: () -> Unit,
    onSelectServer: () -> Unit
) {
    val severityColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> CriticalRed
        AlertSeverity.WARNING -> WarningAmber
        AlertSeverity.INFO -> ElectricCyan
    }

    val timeFormatted = remember(alert.timestamp) {
        SimpleDateFormat("HH:mm:ss • dd MMM", Locale.FRANCE).format(Date(alert.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .testTag("incident_card_${alert.id}"),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF131D33)
        ),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (alert.isResolved) Color(0xFF1E293B)
                else severityColor.copy(alpha = 0.5f)
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row: Severity Icon, Title, Timestamp
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
                            .size(32.dp)
                            .background(severityColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (alert.severity) {
                                AlertSeverity.CRITICAL -> Icons.Default.Error
                                AlertSeverity.WARNING -> Icons.Default.Warning
                                AlertSeverity.INFO -> Icons.Default.Info
                            },
                            contentDescription = null,
                            tint = severityColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = alert.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${alert.serverName} • $timeFormatted",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                if (alert.isResolved) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("RÉSOLU", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                    }
                } else if (alert.isAcknowledged) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(WarningAmber.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("ACQUITTÉ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WarningAmber)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Message Description
            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFCBD5E1),
                lineHeight = 16.sp
            )

            // Recommended Remediation Playbook
            if (alert.recommendedFix.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0B1220))
                        .border(1.dp, PurpleAccent.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = PurpleAccent,
                            modifier = Modifier.size(16.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Action corrective recommandée par l'IA :",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PurpleAccent
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = alert.recommendedFix,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }
                }
            }

            // Action Buttons
            if (!alert.isResolved) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!alert.isAcknowledged) {
                        OutlinedButton(
                            onClick = onAcknowledge,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Acquitter", fontSize = 12.sp, color = WarningAmber)
                        }
                    }

                    Button(
                        onClick = onResolve,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGreen,
                            contentColor = Color(0xFF0A0F1D)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("btn_resolve_alert_${alert.id}")
                    ) {
                        Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Résoudre", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
