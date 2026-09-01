package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.IncidentAlertEntity
import com.example.data.local.LogEventEntity
import com.example.data.local.MetricHistoryEntity
import com.example.data.local.ServerEntity
import com.example.data.model.AlertSeverity
import com.example.data.model.ChartDisplayMode
import com.example.data.model.ChartMetricType
import com.example.data.model.ChartTimeRange
import com.example.data.model.CloudProvider
import com.example.data.model.HealthFilterOption
import com.example.data.model.ServerSortCriteria
import com.example.data.model.ServerStatus
import com.example.data.model.ServerType
import com.example.data.model.SortDirection
import com.example.data.model.WorldRegion
import com.example.data.repository.ProbeResult
import com.example.data.repository.ServerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive

class ServerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ServerRepository

    // Periodic Refresh & Telemetry State
    private val _isAutoRefreshEnabled = MutableStateFlow(true)
    val isAutoRefreshEnabled: StateFlow<Boolean> = _isAutoRefreshEnabled.asStateFlow()

    private val _refreshIntervalMs = MutableStateFlow(3000L) // Default: 3 seconds
    val refreshIntervalMs: StateFlow<Long> = _refreshIntervalMs.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _lastRefreshTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastRefreshTimestamp: StateFlow<Long> = _lastRefreshTimestamp.asStateFlow()

    private val _telemetryTicksCount = MutableStateFlow(0L)
    val telemetryTicksCount: StateFlow<Long> = _telemetryTicksCount.asStateFlow()

    private var telemetryJob: Job? = null

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ServerRepository(database)
        startPeriodicRefresh()
    }

    private fun startPeriodicRefresh() {
        telemetryJob?.cancel()
        telemetryJob = viewModelScope.launch {
            while (isActive) {
                val interval = _refreshIntervalMs.value
                delay(interval)
                if (_isAutoRefreshEnabled.value) {
                    _isRefreshing.value = true
                    repository.simulateTelemetryCycle(isSpike = false)
                    _lastRefreshTimestamp.value = System.currentTimeMillis()
                    _telemetryTicksCount.value += 1
                    delay(250L) // Brief UI tick animation
                    _isRefreshing.value = false
                }
            }
        }
    }

    fun toggleAutoRefresh() {
        _isAutoRefreshEnabled.value = !_isAutoRefreshEnabled.value
        if (_isAutoRefreshEnabled.value) {
            userNotice.value = "Rafraîchissement automatique activé (${_refreshIntervalMs.value / 1000}s)."
            startPeriodicRefresh()
        } else {
            userNotice.value = "Rafraîchissement automatique suspendu."
        }
    }

    fun setAutoRefreshEnabled(enabled: Boolean) {
        _isAutoRefreshEnabled.value = enabled
        if (enabled) {
            startPeriodicRefresh()
        }
    }

    fun setRefreshInterval(intervalMs: Long) {
        _refreshIntervalMs.value = intervalMs
        userNotice.value = "Intervalle de rafraîchissement: ${intervalMs / 1000}s"
        startPeriodicRefresh()
    }

    fun triggerManualRefresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.simulateTelemetryCycle(isSpike = false)
            _lastRefreshTimestamp.value = System.currentTimeMillis()
            _telemetryTicksCount.value += 1
            delay(350L)
            _isRefreshing.value = false
            userNotice.value = "Télémétrie actualisée avec succès."
        }
    }

    fun simulateTrafficSpike() {
        viewModelScope.launch {
            _isRefreshing.value = true
            userNotice.value = "⚡ Simulation d'un pic mondial de charge réseau..."
            repository.simulateTelemetryCycle(isSpike = true)
            _lastRefreshTimestamp.value = System.currentTimeMillis()
            _telemetryTicksCount.value += 1
            delay(400L)
            _isRefreshing.value = false
        }
    }

    val allServers: StateFlow<List<ServerEntity>> = repository.allServers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAlerts: StateFlow<List<IncidentAlertEntity>> = repository.activeAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAlerts: StateFlow<List<IncidentAlertEntity>> = repository.allAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAlertCount: StateFlow<Int> = repository.activeAlertCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentLogs: StateFlow<List<LogEventEntity>> = repository.recentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecentMetrics: StateFlow<List<MetricHistoryEntity>> = repository.allRecentMetrics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Telemetry Chart Customization State
    val dashboardChartMetric = MutableStateFlow(ChartMetricType.CPU)
    val dashboardChartTimeRange = MutableStateFlow(ChartTimeRange.MINUTES_15)
    val dashboardChartDisplayMode = MutableStateFlow(ChartDisplayMode.FLEET_TRENDS)
    val dashboardChartFocusServerId = MutableStateFlow<Long?>(null)

    fun setDashboardChartMetric(metric: ChartMetricType) {
        dashboardChartMetric.value = metric
    }

    fun setDashboardChartTimeRange(range: ChartTimeRange) {
        dashboardChartTimeRange.value = range
    }

    fun setDashboardChartDisplayMode(mode: ChartDisplayMode) {
        dashboardChartDisplayMode.value = mode
    }

    fun setDashboardChartFocusServerId(serverId: Long?) {
        dashboardChartFocusServerId.value = serverId
        if (serverId != null) {
            dashboardChartDisplayMode.value = ChartDisplayMode.FOCUSED_NODE
        }
    }

    // Filters & Search for Server Fleet List
    val searchQuery = MutableStateFlow("")
    val selectedRegionFilter = MutableStateFlow<WorldRegion?>(null)
    val selectedTypeFilter = MutableStateFlow<ServerType?>(null)
    val selectedStatusFilter = MutableStateFlow<ServerStatus?>(null)

    // Dashboard Specific Filtering & Sorting
    val dashboardHealthFilter = MutableStateFlow(HealthFilterOption.ALL)
    val dashboardSortCriteria = MutableStateFlow(ServerSortCriteria.CPU_USAGE)
    val dashboardSortDirection = MutableStateFlow(SortDirection.DESCENDING)
    val dashboardSearchQuery = MutableStateFlow("")

    fun setDashboardHealthFilter(filter: HealthFilterOption) {
        dashboardHealthFilter.value = filter
    }

    fun setDashboardSortCriteria(criteria: ServerSortCriteria) {
        dashboardSortCriteria.value = criteria
    }

    fun toggleDashboardSortDirection() {
        dashboardSortDirection.value = if (dashboardSortDirection.value == SortDirection.DESCENDING) {
            SortDirection.ASCENDING
        } else {
            SortDirection.DESCENDING
        }
    }

    fun setDashboardSortDirection(direction: SortDirection) {
        dashboardSortDirection.value = direction
    }

    fun setDashboardSearchQuery(query: String) {
        dashboardSearchQuery.value = query
    }

    fun resetDashboardFilters() {
        dashboardHealthFilter.value = HealthFilterOption.ALL
        dashboardSortCriteria.value = ServerSortCriteria.CPU_USAGE
        dashboardSortDirection.value = SortDirection.DESCENDING
        dashboardSearchQuery.value = ""
        selectedRegionFilter.value = null
    }

    private data class DashboardFilterSortState(
        val region: WorldRegion?,
        val healthFilter: HealthFilterOption,
        val sortCriteria: ServerSortCriteria,
        val sortDirection: SortDirection,
        val searchQuery: String
    )

    private val dashboardFilterOptions = combine(
        selectedRegionFilter,
        dashboardHealthFilter,
        dashboardSortCriteria,
        dashboardSortDirection,
        dashboardSearchQuery
    ) { region, healthFilter, sortBy, sortDirection, query ->
        DashboardFilterSortState(region, healthFilter, sortBy, sortDirection, query)
    }

    // Dashboard Filtered & Sorted Servers
    val dashboardServers: StateFlow<List<ServerEntity>> = combine(
        allServers,
        dashboardFilterOptions
    ) { servers, options ->
        val filtered = servers.filter { server ->
            val matchesRegion = options.region == null || server.region == options.region
            val matchesQuery = options.searchQuery.isBlank() ||
                    server.name.contains(options.searchQuery, ignoreCase = true) ||
                    server.hostname.contains(options.searchQuery, ignoreCase = true) ||
                    server.ipAddress.contains(options.searchQuery, ignoreCase = true) ||
                    server.region.city.contains(options.searchQuery, ignoreCase = true)

            val matchesHealth = when (options.healthFilter) {
                HealthFilterOption.ALL -> true
                HealthFilterOption.ONLINE_ONLY -> server.status == ServerStatus.ONLINE
                HealthFilterOption.OFFLINE_OR_DEGRADED -> server.status == ServerStatus.CRITICAL || server.status == ServerStatus.WARNING
                HealthFilterOption.CRITICAL_ONLY -> server.status == ServerStatus.CRITICAL
                HealthFilterOption.WARNING_ONLY -> server.status == ServerStatus.WARNING
                HealthFilterOption.MAINTENANCE_ONLY -> server.status == ServerStatus.MAINTENANCE
            }

            matchesRegion && matchesQuery && matchesHealth
        }

        val comparator = when (options.sortCriteria) {
            ServerSortCriteria.CPU_USAGE -> compareBy<ServerEntity> { it.cpuUsagePercent }
            ServerSortCriteria.HEALTH_STATUS -> compareBy<ServerEntity> {
                when (it.status) {
                    ServerStatus.CRITICAL -> 0
                    ServerStatus.WARNING -> 1
                    ServerStatus.ONLINE -> 2
                    ServerStatus.MAINTENANCE -> 3
                }
            }
            ServerSortCriteria.LATENCY -> compareBy<ServerEntity> { it.latencyMs }
            ServerSortCriteria.RAM_USAGE -> compareBy<ServerEntity> { it.ramUsagePercent }
            ServerSortCriteria.NAME -> compareBy<ServerEntity> { it.name.lowercase() }
        }

        if (options.sortDirection == SortDirection.DESCENDING) {
            if (options.sortCriteria == ServerSortCriteria.HEALTH_STATUS) {
                filtered.sortedWith(comparator) // Critical first
            } else {
                filtered.sortedWith(comparator.reversed())
            }
        } else {
            if (options.sortCriteria == ServerSortCriteria.HEALTH_STATUS) {
                filtered.sortedWith(comparator.reversed()) // Online/Maintenance first
            } else {
                filtered.sortedWith(comparator)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered servers for Fleet Screen
    val filteredServers: StateFlow<List<ServerEntity>> = combine(
        allServers,
        searchQuery,
        selectedRegionFilter,
        selectedTypeFilter,
        selectedStatusFilter
    ) { servers, query, region, type, status ->
        servers.filter { server ->
            val matchesQuery = query.isBlank() ||
                    server.name.contains(query, ignoreCase = true) ||
                    server.hostname.contains(query, ignoreCase = true) ||
                    server.ipAddress.contains(query, ignoreCase = true) ||
                    server.region.city.contains(query, ignoreCase = true)

            val matchesRegion = region == null || server.region == region
            val matchesType = type == null || server.serverType == type
            val matchesStatus = status == null || server.status == status

            matchesQuery && matchesRegion && matchesType && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected server for detail view
    private val _selectedServerId = MutableStateFlow<Long?>(null)
    val selectedServerId: StateFlow<Long?> = _selectedServerId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedServer: StateFlow<ServerEntity?> = _selectedServerId.flatMapLatest { id ->
        if (id != null) repository.getServerByIdFlow(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val serverLogs: StateFlow<List<LogEventEntity>> = _selectedServerId.flatMapLatest { id ->
        if (id != null) repository.getLogsForServer(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val serverMetricsHistory: StateFlow<List<MetricHistoryEntity>> = _selectedServerId.flatMapLatest { id ->
        if (id != null) repository.getMetricsForServer(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Terminal console state
    val consoleOutput = MutableStateFlow<String>("")
    val isExecutingCommand = MutableStateFlow(false)

    // Probe tester state
    val probeResult = MutableStateFlow<ProbeResult?>(null)
    val isProbing = MutableStateFlow(false)

    // AI Diagnostics state
    val aiDiagnosisResult = MutableStateFlow<String?>(null)
    val isDiagnosing = MutableStateFlow(false)

    // Toast/Snackbar message
    val userNotice = MutableStateFlow<String?>(null)

    fun selectServer(serverId: Long?) {
        _selectedServerId.value = serverId
        consoleOutput.value = ""
        aiDiagnosisResult.value = null
    }

    fun addServer(
        name: String,
        hostname: String,
        ipAddress: String,
        region: WorldRegion,
        provider: CloudProvider,
        serverType: ServerType,
        cpuCores: Int,
        ramGb: Int,
        diskGb: Int,
        sshPort: Int
    ) {
        viewModelScope.launch {
            val newServer = ServerEntity(
                name = name.trim(),
                hostname = hostname.trim(),
                ipAddress = ipAddress.trim(),
                region = region,
                provider = provider,
                serverType = serverType,
                status = ServerStatus.ONLINE,
                cpuUsagePercent = 25.0f,
                ramUsagePercent = 35.0f,
                diskUsagePercent = 20.0f,
                latencyMs = 20,
                bandwidthInMbps = 250.0f,
                bandwidthOutMbps = 300.0f,
                activeConnections = 120,
                uptimeSeconds = 60L,
                cpuCores = cpuCores,
                ramGb = ramGb,
                diskGb = diskGb,
                sshPort = sshPort
            )
            val id = repository.addServer(newServer)
            userNotice.value = "Serveur '$name' ajouté avec succès (ID #$id)"
        }
    }

    fun updateServer(server: ServerEntity) {
        viewModelScope.launch {
            repository.updateServer(server)
            userNotice.value = "Configuration de '${server.name}' mise à jour."
        }
    }

    fun deleteServer(serverId: Long) {
        viewModelScope.launch {
            repository.deleteServer(serverId)
            if (_selectedServerId.value == serverId) {
                _selectedServerId.value = null
            }
            userNotice.value = "Serveur supprimé du réseau."
        }
    }

    fun rebootServer(serverId: Long) {
        viewModelScope.launch {
            userNotice.value = "Redémarrage du serveur en cours..."
            repository.rebootServer(serverId)
            userNotice.value = "Serveur redémarré avec succès."
        }
    }

    fun toggleMaintenance(serverId: Long) {
        viewModelScope.launch {
            repository.toggleMaintenance(serverId)
            userNotice.value = "Statut de maintenance modifié."
        }
    }

    fun restartService(serverId: Long, serviceName: String) {
        viewModelScope.launch {
            userNotice.value = "Redémarrage du service $serviceName..."
            val result = repository.restartService(serverId, serviceName)
            userNotice.value = result
        }
    }

    fun executeTerminalCommand(serverId: Long, command: String) {
        viewModelScope.launch {
            isExecutingCommand.value = true
            consoleOutput.value += "\n$ $command\n"
            val output = repository.executeCommand(serverId, command)
            consoleOutput.value += output + "\n"
            isExecutingCommand.value = false
        }
    }

    fun probeTarget(hostOrIp: String) {
        viewModelScope.launch {
            isProbing.value = true
            probeResult.value = null
            val result = repository.runProbeTest(hostOrIp)
            probeResult.value = result
            isProbing.value = false
        }
    }

    fun runSmartAIDiagnosis(server: ServerEntity) {
        viewModelScope.launch {
            isDiagnosing.value = true
            aiDiagnosisResult.value = null
            delay(1500L) // Process telemetry & pattern matching

            val diagnosis = buildString {
                appendLine("=== RAPPORT DE DIAGNOSTIC TÉLÉMÉTRIQUE IA ===")
                appendLine("Nœud cible: ${server.name} (${server.hostname} / ${server.ipAddress})")
                appendLine("Région: ${server.region.city}, ${server.region.country} [${server.provider.label}]")
                appendLine("Horodatage: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.FRANCE).format(java.util.Date())}")
                appendLine()

                val healthScore = (100 - (server.cpuUsagePercent * 0.4f + server.ramUsagePercent * 0.4f + (server.latencyMs / 3f))).toInt().coerceIn(10, 99)
                appendLine("Indice de Santé Global: $healthScore / 100")
                appendLine()

                appendLine("ANALYSE DES COMPOSANTS:")
                if (server.cpuUsagePercent > 85f) {
                    appendLine("⚠️ CPU: CRITIQUE (${server.cpuUsagePercent.toInt()}%) - Détection de contention de threads sur les ${server.cpuCores} cœurs.")
                    appendLine("   -> Cause probable: Boucle infinie ou surcharge de calcul non indexée.")
                } else {
                    appendLine("✅ CPU: NOMINAL (${server.cpuUsagePercent.toInt()}%) - Charge équilibrée.")
                }

                if (server.ramUsagePercent > 88f) {
                    appendLine("⚠️ MÉMOIRE: ÉLEVÉE (${server.ramUsagePercent.toInt()}%) - Swap actif, risque d'OOM Killer (Out Of Memory).")
                    appendLine("   -> Recommandation: Purger les caches inactifs ou allouer +${server.ramGb} Go de RAM.")
                } else {
                    appendLine("✅ MÉMOIRE: OPTIMALE (${server.ramUsagePercent.toInt()}%) - ${server.ramGb} Go alloués.")
                }

                if (server.latencyMs > 100) {
                    appendLine("⚠️ RÉSEAU: LATENCE ANORMALE (${server.latencyMs}ms) - Goulot d'étranglement de routage WAN détecté.")
                    appendLine("   -> Recommandation: Basculer la passerelle BGP vers Anycast.")
                } else {
                    appendLine("✅ RÉSEAU: EXCELLENT (${server.latencyMs}ms) - Débit ${server.bandwidthInMbps.toInt()} Mbps IN / ${server.bandwidthOutMbps.toInt()} Mbps OUT.")
                }

                appendLine()
                appendLine("ACTIONS RECOMMANDÉES PAR L'IA:")
                appendLine("1. Optimisation du pool de sockets TCP (tcp_tw_reuse = 1)")
                appendLine("2. Exécution du playbook Ansible de maintenance préventive")
                appendLine("3. Auto-scaling pod horizontal (HPA) configuré sur seuil 75% CPU")
            }

            aiDiagnosisResult.value = diagnosis
            isDiagnosing.value = false
        }
    }

    fun acknowledgeAlert(alertId: Long) {
        viewModelScope.launch {
            repository.acknowledgeAlert(alertId)
            userNotice.value = "Alerte acquittée."
        }
    }

    fun resolveAlert(alertId: Long) {
        viewModelScope.launch {
            repository.resolveAlert(alertId)
            userNotice.value = "Incident résolu et archivé."
        }
    }

    fun clearResolvedAlerts() {
        viewModelScope.launch {
            repository.clearResolvedAlerts()
            userNotice.value = "Historique des alertes résolues nettoyé."
        }
    }

    fun clearNotice() {
        userNotice.value = null
    }
}
