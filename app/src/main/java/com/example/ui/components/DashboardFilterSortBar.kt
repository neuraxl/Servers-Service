package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ServerEntity
import com.example.data.model.HealthFilterOption
import com.example.data.model.ServerSortCriteria
import com.example.data.model.ServerStatus
import com.example.data.model.SortDirection
import com.example.data.model.WorldRegion
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.WarningAmber

@Composable
fun DashboardFilterSortBar(
    servers: List<ServerEntity>,
    healthFilter: HealthFilterOption,
    onHealthFilterChange: (HealthFilterOption) -> Unit,
    sortCriteria: ServerSortCriteria,
    onSortCriteriaChange: (ServerSortCriteria) -> Unit,
    sortDirection: SortDirection,
    onToggleSortDirection: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onResetFilters: () -> Unit,
    selectedRegion: WorldRegion?,
    onClearRegion: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchExpanded by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    // Calculate server counts for dynamic badge counts
    val onlineCount = servers.count { it.status == ServerStatus.ONLINE }
    val degradedCount = servers.count { it.status == ServerStatus.CRITICAL || it.status == ServerStatus.WARNING }
    val maintenanceCount = servers.count { it.status == ServerStatus.MAINTENANCE }

    val hasActiveCustomFilters = healthFilter != HealthFilterOption.ALL ||
            sortCriteria != ServerSortCriteria.CPU_USAGE ||
            sortDirection != SortDirection.DESCENDING ||
            searchQuery.isNotBlank() ||
            selectedRegion != null

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .testTag("dashboard_filter_sort_bar"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF10192C)),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(
                    ElectricCyan.copy(alpha = 0.3f),
                    Color(0xFF1E293B),
                    if (healthFilter != HealthFilterOption.ALL) ElectricCyan.copy(alpha = 0.5f) else Color.Transparent
                )
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Title, Search toggle, Sort Dropdown & Sort Direction Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterAlt,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "Filtres & Classement",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    if (hasActiveCustomFilters) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(ElectricCyan, CircleShape)
                        )
                    }
                }

                // Action Controls: Search, Sort Criterion Dropdown, Sort Direction, Reset
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Search toggle button
                    IconButton(
                        onClick = {
                            searchExpanded = !searchExpanded
                            if (!searchExpanded) onSearchQueryChange("")
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (searchExpanded || searchQuery.isNotEmpty()) ElectricCyan.copy(alpha = 0.2f) else Color(0xFF1E293B))
                            .testTag("btn_toggle_dashboard_search")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Rechercher",
                            tint = if (searchExpanded || searchQuery.isNotEmpty()) ElectricCyan else Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Sort Criteria Dropdown Trigger
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                                .border(0.8.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .clickable { sortMenuExpanded = true }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("btn_sort_criteria_menu"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = getSortIcon(sortCriteria),
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = sortCriteria.shortLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false },
                            modifier = Modifier
                                .background(Color(0xFF0F172A))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                        ) {
                            ServerSortCriteria.values().forEach { criteria ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = getSortIcon(criteria),
                                                contentDescription = null,
                                                tint = if (sortCriteria == criteria) ElectricCyan else Color(0xFF94A3B8),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = criteria.label,
                                                fontSize = 12.sp,
                                                fontWeight = if (sortCriteria == criteria) FontWeight.Bold else FontWeight.Normal,
                                                color = if (sortCriteria == criteria) ElectricCyan else Color.White
                                            )
                                        }
                                    },
                                    onClick = {
                                        onSortCriteriaChange(criteria)
                                        sortMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Sort Direction Toggle Button (Max->Min or Min->Max)
                    IconButton(
                        onClick = onToggleSortDirection,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E293B))
                            .border(0.8.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                            .testTag("btn_toggle_sort_direction")
                    ) {
                        Icon(
                            imageVector = if (sortDirection == SortDirection.DESCENDING) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = sortDirection.label,
                            tint = if (sortDirection == SortDirection.DESCENDING) WarningAmber else ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Reset button if custom filters are applied
                    if (hasActiveCustomFilters) {
                        IconButton(
                            onClick = {
                                onResetFilters()
                                searchExpanded = false
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CriticalRed.copy(alpha = 0.15f))
                                .border(0.8.dp, CriticalRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .testTag("btn_reset_dashboard_filters")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Réinitialiser les filtres",
                                tint = CriticalRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Search input (Animated expand/collapse)
            AnimatedVisibility(
                visible = searchExpanded || searchQuery.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Filtrer par nom, IP, hôte...", color = Color(0xFF64748B), fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Effacer",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("dashboard_search_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = Color(0xFF24344D),
                        focusedContainerColor = Color(0xFF0B1220),
                        unfocusedContainerColor = Color(0xFF0B1220),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }

            // Health Status Filtering Chips Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    HealthFilterChip(
                        label = "Tous (${servers.size})",
                        isSelected = healthFilter == HealthFilterOption.ALL,
                        accentColor = ElectricCyan,
                        icon = Icons.Default.Sort,
                        onClick = { onHealthFilterChange(HealthFilterOption.ALL) }
                    )
                }

                item {
                    HealthFilterChip(
                        label = "En Ligne ($onlineCount)",
                        isSelected = healthFilter == HealthFilterOption.ONLINE_ONLY,
                        accentColor = NeonGreen,
                        icon = Icons.Default.CheckCircle,
                        onClick = { onHealthFilterChange(HealthFilterOption.ONLINE_ONLY) }
                    )
                }

                item {
                    HealthFilterChip(
                        label = "Critique / Alertes ($degradedCount)",
                        isSelected = healthFilter == HealthFilterOption.OFFLINE_OR_DEGRADED,
                        accentColor = if (degradedCount > 0) CriticalRed else WarningAmber,
                        icon = Icons.Default.Warning,
                        onClick = { onHealthFilterChange(HealthFilterOption.OFFLINE_OR_DEGRADED) }
                    )
                }

                item {
                    HealthFilterChip(
                        label = "Maintenance ($maintenanceCount)",
                        isSelected = healthFilter == HealthFilterOption.MAINTENANCE_ONLY,
                        accentColor = PurpleAccent,
                        icon = Icons.Default.Storage,
                        onClick = { onHealthFilterChange(HealthFilterOption.MAINTENANCE_ONLY) }
                    )
                }
            }

            // Active Filters & Sorting Status Pill Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Classement actif:",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF162238))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${sortCriteria.label} (${if (sortDirection == SortDirection.DESCENDING) "Max → Min" else "Min → Max"})",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }
                }

                // Region filter active badge
                if (selectedRegion != null) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(InfoBlue.copy(alpha = 0.15f))
                            .border(0.8.dp, InfoBlue.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .clickable(onClick = onClearRegion)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "Région: ${selectedRegion.city}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = InfoBlue
                        )
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Retirer région",
                            tint = InfoBlue,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthFilterChip(
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) accentColor.copy(alpha = 0.2f) else Color(0xFF131D31)
            )
            .border(
                1.dp,
                if (isSelected) accentColor else Color(0xFF22324C),
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 6.dp)
            .testTag("health_filter_chip_${label.take(8).trim()}"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(if (isSelected) accentColor else Color(0xFF64748B), CircleShape)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color(0xFF94A3B8)
            )
        }
    }
}

private fun getSortIcon(criteria: ServerSortCriteria): ImageVector {
    return when (criteria) {
        ServerSortCriteria.CPU_USAGE -> Icons.Default.Speed
        ServerSortCriteria.HEALTH_STATUS -> Icons.Default.CheckCircle
        ServerSortCriteria.LATENCY -> Icons.Default.Speed
        ServerSortCriteria.RAM_USAGE -> Icons.Default.Memory
        ServerSortCriteria.NAME -> Icons.Default.SortByAlpha
    }
}
