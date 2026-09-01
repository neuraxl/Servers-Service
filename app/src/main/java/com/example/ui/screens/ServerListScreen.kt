package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ServerEntity
import com.example.data.model.ServerStatus
import com.example.data.model.ServerType
import com.example.data.model.WorldRegion
import com.example.ui.components.ServerCard
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServerListScreen(
    servers: List<ServerEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedStatus: ServerStatus?,
    onSelectStatus: (ServerStatus?) -> Unit,
    selectedType: ServerType?,
    onSelectType: (ServerType?) -> Unit,
    onSelectServer: (Long) -> Unit,
    onAddServer: () -> Unit,
    onRebootServer: (Long) -> Unit,
    onToggleMaintenance: (Long) -> Unit,
    onOpenTerminal: (Long) -> Unit,
    onDeleteServer: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddServer,
                containerColor = ElectricCyan,
                contentColor = Color(0xFF0A0F1D),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 70.dp).testTag("fab_add_server")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Ajouter un Serveur",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Rechercher par nom, IP, hôte ou ville...", color = Color(0xFF64748B), fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Rechercher",
                        tint = ElectricCyan
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Effacer",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_server_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = Color(0xFF1E293B),
                    focusedContainerColor = Color(0xFF111C30),
                    unfocusedContainerColor = Color(0xFF111C30),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Status Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedStatus == null,
                    onClick = { onSelectStatus(null) },
                    label = { Text("Tous les états (${servers.size})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricCyan,
                        selectedLabelColor = Color(0xFF0A0F1D),
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFF94A3B8)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                ServerStatus.values().forEach { status ->
                    val count = servers.count { it.status == status }
                    val color = when (status) {
                        ServerStatus.ONLINE -> NeonGreen
                        ServerStatus.WARNING -> WarningAmber
                        ServerStatus.CRITICAL -> CriticalRed
                        ServerStatus.MAINTENANCE -> PurpleAccent
                    }
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { onSelectStatus(if (selectedStatus == status) null else status) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(color, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${status.label} ($count)", fontSize = 12.sp)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color,
                            selectedLabelColor = if (status == ServerStatus.ONLINE || status == ServerStatus.WARNING) Color(0xFF0A0F1D) else Color.White,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Server Type Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedType == null,
                    onClick = { onSelectType(null) },
                    label = { Text("Tous types", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF334155),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF0F172A),
                        labelColor = Color(0xFF64748B)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                ServerType.values().forEach { type ->
                    val isSelected = selectedType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectType(if (isSelected) null else type) },
                        label = { Text(type.label.split(" ").take(2).joinToString(" "), fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricCyan.copy(alpha = 0.2f),
                            selectedLabelColor = ElectricCyan,
                            containerColor = Color(0xFF0F172A),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Server List
            if (servers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = null,
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Aucun serveur ne correspond aux filtres.",
                            color = Color(0xFF94A3B8),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(servers, key = { it.id }) { server ->
                        ServerCard(
                            server = server,
                            onClick = { onSelectServer(server.id) },
                            onReboot = { onRebootServer(server.id) },
                            onToggleMaintenance = { onToggleMaintenance(server.id) },
                            onOpenTerminal = { onOpenTerminal(server.id) },
                            onDelete = { onDeleteServer(server.id) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}
