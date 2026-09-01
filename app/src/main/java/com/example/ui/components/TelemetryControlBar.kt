package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.WarningAmber

@Composable
fun TelemetryControlBar(
    isAutoRefresh: Boolean,
    isRefreshing: Boolean,
    refreshIntervalMs: Long,
    ticksCount: Long,
    lastRefreshTime: Long,
    onToggleAutoRefresh: () -> Unit,
    onSelectInterval: (Long) -> Unit,
    onManualRefresh: () -> Unit,
    onSimulateSpike: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_and_spin")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    val intervals = listOf(1000L to "1s", 2000L to "2s", 3000L to "3s", 5000L to "5s", 10000L to "10s")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .testTag("telemetry_control_bar"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1829)),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(
                    ElectricCyan.copy(alpha = 0.35f),
                    Color(0xFF1E293B),
                    if (isAutoRefresh) NeonGreen.copy(alpha = 0.3f) else WarningAmber.copy(alpha = 0.3f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Live Status + Quick Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isAutoRefresh) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .scale(pulseScale)
                                    .background(NeonGreen.copy(alpha = 0.25f), CircleShape)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (isAutoRefresh) NeonGreen else WarningAmber, CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isAutoRefresh) "Télémétrie en Direct" else "Télémétrie en Pause",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isAutoRefresh) Color.White else Color(0xFFCBD5E1)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF1E293B))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "#$ticksCount",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    color = ElectricCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = if (isAutoRefresh) {
                                "Mise à jour toutes les ${refreshIntervalMs / 1000}s via ViewModel"
                            } else {
                                "Rafraîchissement automatique désactivé"
                            },
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Action Buttons: Spike, Manual Refresh, Play/Pause
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Simulate Traffic Spike Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CriticalRed.copy(alpha = 0.15f))
                            .border(0.8.dp, CriticalRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable(onClick = onSimulateSpike)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("btn_simulate_spike"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Simuler Pic de Charge",
                                tint = CriticalRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Pic Trafic",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CriticalRed
                            )
                        }
                    }

                    // Manual Refresh Button
                    IconButton(
                        onClick = onManualRefresh,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E293B))
                            .testTag("btn_manual_refresh")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Rafraîchir maintenant",
                            tint = ElectricCyan,
                            modifier = Modifier
                                .size(18.dp)
                                .rotate(if (isRefreshing) spinAngle else 0f)
                        )
                    }

                    // Play/Pause Auto Refresh Button
                    IconButton(
                        onClick = onToggleAutoRefresh,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isAutoRefresh) NeonGreen.copy(alpha = 0.15f) else Color(0xFF1E293B))
                            .border(
                                0.8.dp,
                                if (isAutoRefresh) NeonGreen.copy(alpha = 0.5f) else Color(0xFF334155),
                                RoundedCornerShape(8.dp)
                            )
                            .testTag("btn_toggle_auto_refresh")
                    ) {
                        Icon(
                            imageVector = if (isAutoRefresh) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isAutoRefresh) "Mettre en pause" else "Démarrer le flux",
                            tint = if (isAutoRefresh) NeonGreen else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Interval Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cadence:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(intervals) { (ms, label) ->
                        val isSelected = refreshIntervalMs == ms
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isSelected) ElectricCyan.copy(alpha = 0.2f) else Color(0xFF162032)
                                )
                                .border(
                                    0.8.dp,
                                    if (isSelected) ElectricCyan else Color(0xFF24344D),
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { onSelectInterval(ms) }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("interval_chip_$label"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ElectricCyan else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }
    }
}
