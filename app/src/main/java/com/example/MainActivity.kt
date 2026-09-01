package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddEditServerDialog
import com.example.ui.components.ConsoleOutputDialog
import com.example.ui.screens.DashboardOverviewScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.IncidentCenterScreen
import com.example.ui.screens.ServerDetailScreen
import com.example.ui.screens.ServerListScreen
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonGreen
import com.example.ui.viewmodel.ServerViewModel

enum class AppNavTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    DASHBOARD("Tableau de bord", Icons.Default.Dashboard),
    SERVERS("Serveurs", Icons.Default.Dns),
    INCIDENTS("Incidents", Icons.Default.NotificationsActive),
    DIAGNOSTICS("Diagnostics", Icons.Default.NetworkPing)
}

class MainActivity : ComponentActivity() {

    private val viewModel: ServerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ServerOpsApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ServerOpsApp(viewModel: ServerViewModel) {
    var currentTab by remember { mutableStateOf(AppNavTab.DASHBOARD) }
    var showAddServerDialog by remember { mutableStateOf(false) }
    var showConsoleDialog by remember { mutableStateOf(false) }

    val servers by viewModel.allServers.collectAsStateWithLifecycle()
    val filteredServers by viewModel.filteredServers.collectAsStateWithLifecycle()
    val activeAlerts by viewModel.activeAlerts.collectAsStateWithLifecycle()
    val allAlerts by viewModel.allAlerts.collectAsStateWithLifecycle()
    val activeAlertCount by viewModel.activeAlertCount.collectAsStateWithLifecycle()
    val recentLogs by viewModel.recentLogs.collectAsStateWithLifecycle()

    val selectedServerId by viewModel.selectedServerId.collectAsStateWithLifecycle()
    val selectedServer by viewModel.selectedServer.collectAsStateWithLifecycle()
    val selectedRegion by viewModel.selectedRegionFilter.collectAsStateWithLifecycle()
    val selectedType by viewModel.selectedTypeFilter.collectAsStateWithLifecycle()
    val selectedStatus by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val serverLogs by viewModel.serverLogs.collectAsStateWithLifecycle()
    val serverMetricsHistory by viewModel.serverMetricsHistory.collectAsStateWithLifecycle()

    // Dashboard Filter and Sort States
    val dashboardDisplayServers by viewModel.dashboardServers.collectAsStateWithLifecycle()
    val dashboardHealthFilter by viewModel.dashboardHealthFilter.collectAsStateWithLifecycle()
    val dashboardSortCriteria by viewModel.dashboardSortCriteria.collectAsStateWithLifecycle()
    val dashboardSortDirection by viewModel.dashboardSortDirection.collectAsStateWithLifecycle()
    val dashboardSearchQuery by viewModel.dashboardSearchQuery.collectAsStateWithLifecycle()

    // Dashboard Telemetry Chart States
    val allRecentMetrics by viewModel.allRecentMetrics.collectAsStateWithLifecycle()
    val dashboardChartMetric by viewModel.dashboardChartMetric.collectAsStateWithLifecycle()
    val dashboardChartTimeRange by viewModel.dashboardChartTimeRange.collectAsStateWithLifecycle()
    val dashboardChartDisplayMode by viewModel.dashboardChartDisplayMode.collectAsStateWithLifecycle()
    val dashboardChartFocusServerId by viewModel.dashboardChartFocusServerId.collectAsStateWithLifecycle()

    // Periodic Telemetry Simulation States
    val isAutoRefresh by viewModel.isAutoRefreshEnabled.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val refreshIntervalMs by viewModel.refreshIntervalMs.collectAsStateWithLifecycle()
    val telemetryTicksCount by viewModel.telemetryTicksCount.collectAsStateWithLifecycle()
    val lastRefreshTimestamp by viewModel.lastRefreshTimestamp.collectAsStateWithLifecycle()

    val consoleOutput by viewModel.consoleOutput.collectAsStateWithLifecycle()
    val isExecutingCommand by viewModel.isExecutingCommand.collectAsStateWithLifecycle()

    val probeResult by viewModel.probeResult.collectAsStateWithLifecycle()
    val isProbing by viewModel.isProbing.collectAsStateWithLifecycle()

    val aiDiagnosisResult by viewModel.aiDiagnosisResult.collectAsStateWithLifecycle()
    val isDiagnosing by viewModel.isDiagnosing.collectAsStateWithLifecycle()

    val userNotice by viewModel.userNotice.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userNotice) {
        userNotice?.let { notice ->
            snackbarHostState.showSnackbar(notice)
            viewModel.clearNotice()
        }
    }

    // Handle back button when inside Server Detail view
    BackHandler(enabled = selectedServerId != null) {
        viewModel.selectServer(null)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D)),
        containerColor = Color(0xFF0A0F1D),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            )
        },
        topBar = {
            // NOC Top App Bar Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .background(Color(0xFF0A0F1D))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                Brush.linearGradient(listOf(ElectricCyan, Color(0xFF0284C7))),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = Color(0xFF0A0F1D),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "NETGLOBAL OPS",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ElectricCyan.copy(alpha = 0.2f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text("NOC v2.4", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(if (isAutoRefresh) NeonGreen else Color(0xFFEAB308), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isAutoRefresh) {
                                    if (isRefreshing) "Actualisation Télémétrique..." else "Surveillance Active (${refreshIntervalMs / 1000}s) • Cycle #$telemetryTicksCount"
                                } else {
                                    "Flux Suspendu • En Pause"
                                },
                                fontSize = 11.sp,
                                color = if (isRefreshing) ElectricCyan else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (selectedServerId == null) {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    containerColor = Color(0xFF0E1626),
                    tonalElevation = 8.dp
                ) {
                    AppNavTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            icon = {
                                if (tab == AppNavTab.INCIDENTS && activeAlertCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = CriticalRed,
                                                contentColor = Color.White
                                            ) {
                                                Text(activeAlertCount.toString())
                                            }
                                        }
                                    ) {
                                        Icon(tab.icon, contentDescription = tab.label)
                                    }
                                } else {
                                    Icon(tab.icon, contentDescription = tab.label)
                                }
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF0A0F1D),
                                selectedTextColor = ElectricCyan,
                                indicatorColor = ElectricCyan,
                                unselectedIconColor = Color(0xFF64748B),
                                unselectedTextColor = Color(0xFF64748B)
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedServer != null) {
                // Server Detail View
                ServerDetailScreen(
                    server = selectedServer!!,
                    metricsHistory = serverMetricsHistory,
                    serverLogs = serverLogs,
                    aiDiagnosisResult = aiDiagnosisResult,
                    isDiagnosing = isDiagnosing,
                    onBack = { viewModel.selectServer(null) },
                    onOpenTerminal = { showConsoleDialog = true },
                    onReboot = { viewModel.rebootServer(selectedServer!!.id) },
                    onToggleMaintenance = { viewModel.toggleMaintenance(selectedServer!!.id) },
                    onRestartService = { srv -> viewModel.restartService(selectedServer!!.id, srv) },
                    onRunAiDiagnosis = { viewModel.runSmartAIDiagnosis(selectedServer!!) },
                    onDelete = { viewModel.deleteServer(selectedServer!!.id) },
                    onManualRefresh = { viewModel.triggerManualRefresh() }
                )
            } else {
                // Main Navigation Tabs
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_transition"
                ) { tab ->
                    when (tab) {
                        AppNavTab.DASHBOARD -> DashboardOverviewScreen(
                            servers = servers,
                            dashboardDisplayServers = dashboardDisplayServers,
                            activeAlerts = activeAlerts,
                            recentLogs = recentLogs,
                            isAutoRefresh = isAutoRefresh,
                            isRefreshing = isRefreshing,
                            refreshIntervalMs = refreshIntervalMs,
                            ticksCount = telemetryTicksCount,
                            lastRefreshTime = lastRefreshTimestamp,
                            onToggleAutoRefresh = { viewModel.toggleAutoRefresh() },
                            onSelectInterval = { viewModel.setRefreshInterval(it) },
                            onManualRefresh = { viewModel.triggerManualRefresh() },
                            onSimulateSpike = { viewModel.simulateTrafficSpike() },
                            healthFilter = dashboardHealthFilter,
                            onHealthFilterChange = { viewModel.setDashboardHealthFilter(it) },
                            sortCriteria = dashboardSortCriteria,
                            onSortCriteriaChange = { viewModel.setDashboardSortCriteria(it) },
                            sortDirection = dashboardSortDirection,
                            onToggleSortDirection = { viewModel.toggleDashboardSortDirection() },
                            dashboardSearchQuery = dashboardSearchQuery,
                            onDashboardSearchQueryChange = { viewModel.setDashboardSearchQuery(it) },
                            onResetFilters = { viewModel.resetDashboardFilters() },
                            selectedRegion = selectedRegion,
                            onSelectRegion = { viewModel.selectedRegionFilter.value = it },
                            metricsHistory = allRecentMetrics,
                            chartMetric = dashboardChartMetric,
                            onChartMetricChange = { viewModel.setDashboardChartMetric(it) },
                            chartTimeRange = dashboardChartTimeRange,
                            onChartTimeRangeChange = { viewModel.setDashboardChartTimeRange(it) },
                            chartDisplayMode = dashboardChartDisplayMode,
                            onChartDisplayModeChange = { viewModel.setDashboardChartDisplayMode(it) },
                            chartFocusServerId = dashboardChartFocusServerId,
                            onChartFocusServerChange = { viewModel.setDashboardChartFocusServerId(it) },
                            onSelectServer = { viewModel.selectServer(it) },
                            onAddServerClick = { showAddServerDialog = true },
                            onRebootServer = { viewModel.rebootServer(it) },
                            onToggleMaintenance = { viewModel.toggleMaintenance(it) },
                            onOpenTerminal = { id ->
                                viewModel.selectServer(id)
                                showConsoleDialog = true
                            },
                            onDeleteServer = { viewModel.deleteServer(it) },
                            onNavigateToAlerts = { currentTab = AppNavTab.INCIDENTS }
                        )

                        AppNavTab.SERVERS -> ServerListScreen(
                            servers = filteredServers,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.searchQuery.value = it },
                            selectedStatus = selectedStatus,
                            onSelectStatus = { viewModel.selectedStatusFilter.value = it },
                            selectedType = selectedType,
                            onSelectType = { viewModel.selectedTypeFilter.value = it },
                            onSelectServer = { viewModel.selectServer(it) },
                            onAddServer = { showAddServerDialog = true },
                            onRebootServer = { viewModel.rebootServer(it) },
                            onToggleMaintenance = { viewModel.toggleMaintenance(it) },
                            onOpenTerminal = { id ->
                                viewModel.selectServer(id)
                                showConsoleDialog = true
                            },
                            onDeleteServer = { viewModel.deleteServer(it) }
                        )

                        AppNavTab.INCIDENTS -> IncidentCenterScreen(
                            allAlerts = allAlerts,
                            onAcknowledgeAlert = { viewModel.acknowledgeAlert(it) },
                            onResolveAlert = { viewModel.resolveAlert(it) },
                            onClearResolved = { viewModel.clearResolvedAlerts() },
                            onSelectServer = { viewModel.selectServer(it) }
                        )

                        AppNavTab.DIAGNOSTICS -> DiagnosticsScreen(
                            probeResult = probeResult,
                            isProbing = isProbing,
                            onProbeTarget = { viewModel.probeTarget(it) }
                        )
                    }
                }
            }
        }
    }

    // Add Server Modal Dialog
    if (showAddServerDialog) {
        AddEditServerDialog(
            onSave = { name, host, ip, region, provider, type, cores, ram, disk, port ->
                viewModel.addServer(name, host, ip, region, provider, type, cores, ram, disk, port)
            },
            onDismiss = { showAddServerDialog = false }
        )
    }

    // SSH Console Terminal Dialog
    if (showConsoleDialog && selectedServer != null) {
        ConsoleOutputDialog(
            server = selectedServer!!,
            output = consoleOutput,
            isLoading = isExecutingCommand,
            onExecuteCommand = { cmd -> viewModel.executeTerminalCommand(selectedServer!!.id, cmd) },
            onDismiss = { showConsoleDialog = false }
        )
    }
}
