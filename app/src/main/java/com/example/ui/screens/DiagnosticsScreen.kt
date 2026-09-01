package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ProbeResult
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiagnosticsScreen(
    probeResult: ProbeResult?,
    isProbing: Boolean,
    onProbeTarget: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var targetInput by remember { mutableStateOf("1.1.1.1") }

    val presetTargets = listOf("8.8.8.8", "1.1.1.1", "google.com", "github.com", "ovh.com")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(ElectricCyan.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NetworkPing,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Diagnostics & Sonde Réseau Mondiale",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Test de joignabilité, latence ICMP/DNS et audit WAN",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        // Real-Time Ping / Endpoint Prober Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111C30)),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1E293B))
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Sonde de Joignabilité Réseau (Ping / DNS)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Entrez une adresse IP ou un nom d'hôte pour mesurer la latence et la résolution DNS.",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset Chips
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        presetTargets.forEach { preset ->
                            AssistChip(
                                onClick = {
                                    targetInput = preset
                                    onProbeTarget(preset)
                                },
                                label = { Text(preset, fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = Color(0xFF1E293B),
                                    labelColor = ElectricCyan
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Input & Run Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = targetInput,
                            onValueChange = { targetInput = it },
                            placeholder = { Text("IP ou domaine (ex: 8.8.8.8)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("probe_input_field"),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                color = Color.White
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(onGo = {
                                if (targetInput.isNotBlank()) onProbeTarget(targetInput)
                            }),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = Color(0xFF1E293B),
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A)
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (targetInput.isNotBlank()) onProbeTarget(targetInput)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricCyan,
                                contentColor = Color(0xFF0A0F1D)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("probe_run_button")
                        ) {
                            if (isProbing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xFF0A0F1D))
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tester", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Probe Result Display
                    if (probeResult != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0A0F1D))
                                .border(
                                    1.dp,
                                    if (probeResult.isReachable) NeonGreen.copy(alpha = 0.4f) else CriticalRed.copy(alpha = 0.4f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(if (probeResult.isReachable) NeonGreen else CriticalRed, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = probeResult.target,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color.White
                                        )
                                    }

                                    Text(
                                        text = "${probeResult.responseTimeMs} ms",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = if (probeResult.responseTimeMs < 80) NeonGreen else WarningAmber
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = probeResult.message,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Global Latency Matrix
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111C30)),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1E293B))
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Matrice de Latence Inter-Datacenters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Temps de transit moyen WAN entre les zones de disponibilité mondiales",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LatencyMatrixRow(from = "us-east-1 (Virginie)", to = "eu-west-1 (Paris)", latency = "74 ms", status = NeonGreen)
                    LatencyMatrixRow(from = "eu-west-1 (Paris)", to = "eu-central (Francfort)", latency = "14 ms", status = NeonGreen)
                    LatencyMatrixRow(from = "us-east-1 (Virginie)", to = "ap-east (Tokyo)", latency = "148 ms", status = WarningAmber)
                    LatencyMatrixRow(from = "ap-east (Tokyo)", to = "ap-south (Singapour)", latency = "62 ms", status = NeonGreen)
                    LatencyMatrixRow(from = "us-east-1 (Virginie)", to = "sa-east (São Paulo)", latency = "118 ms", status = WarningAmber)
                    LatencyMatrixRow(from = "eu-west-1 (Paris)", to = "af-south (Le Cap)", latency = "135 ms", status = WarningAmber)
                }
            }
        }

        // Security & Infrastructure Health Audit Cards
        item {
            Text(
                text = "État de Santé des Protocoles & Sécurité",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        item {
            AuditStatusCard(
                title = "Routage BGP & Anycast Global",
                status = "Nominal (0 route flap)",
                description = "Toutes les routes BGP mondiales sont synchronisées sur l'ASN 13335.",
                icon = Icons.Default.Lan,
                color = NeonGreen
            )
        }

        item {
            AuditStatusCard(
                title = "Certificats SSL/TLS Wildcard",
                status = "Valides (Renouvellement auto)",
                description = "9 serveurs protégés par chiffrement TLS 1.3 / ChaCha20-Poly1305.",
                icon = Icons.Default.Lock,
                color = NeonGreen
            )
        }

        item {
            AuditStatusCard(
                title = "Protection Anti-DDoS & Pare-feu",
                status = "Actif • Mode Anycast Edge",
                description = "Filtrage actif des paquets volumétriques et protection SYN Flood.",
                icon = Icons.Default.Security,
                color = ElectricCyan
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun LatencyMatrixRow(
    from: String,
    to: String,
    latency: String,
    status: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = from, fontSize = 11.sp, color = Color(0xFFE2E8F0))
            Text(text = " ⇄ ", fontSize = 11.sp, color = Color(0xFF64748B))
            Text(text = to, fontSize = 11.sp, color = Color(0xFFE2E8F0))
        }

        Text(
            text = latency,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = status
        )
    }
}

@Composable
fun AuditStatusCard(
    title: String,
    status: String,
    description: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D33)),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1E293B))
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    Text(text = status, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = color)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = description, fontSize = 11.sp, color = Color(0xFF94A3B8))
            }
        }
    }
}
