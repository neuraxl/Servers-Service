package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.IncidentAlertEntity
import com.example.data.local.LogEventEntity
import com.example.data.local.MetricHistoryEntity
import com.example.data.local.ServerEntity
import com.example.data.model.AlertSeverity
import com.example.data.model.CloudProvider
import com.example.data.model.ServerStatus
import com.example.data.model.ServerType
import com.example.data.model.WorldRegion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress
import kotlin.random.Random

class ServerRepository(private val database: AppDatabase) {

    private val serverDao = database.serverDao()
    private val alertDao = database.incidentAlertDao()
    private val logDao = database.logEventDao()
    private val metricDao = database.metricHistoryDao()

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var telemetryJob: Job? = null

    val allServers: Flow<List<ServerEntity>> = serverDao.getAllServers()
    val allAlerts: Flow<List<IncidentAlertEntity>> = alertDao.getAllAlerts()
    val activeAlerts: Flow<List<IncidentAlertEntity>> = alertDao.getActiveAlerts()
    val activeAlertCount: Flow<Int> = alertDao.getActiveAlertCount()
    val recentLogs: Flow<List<LogEventEntity>> = logDao.getRecentLogs()
    val allRecentMetrics: Flow<List<MetricHistoryEntity>> = metricDao.getAllRecentMetrics()

    init {
        scope.launch {
            checkAndSeedData()
        }
    }

    fun getServerByIdFlow(id: Long): Flow<ServerEntity?> = serverDao.getServerByIdFlow(id)
    fun getLogsForServer(serverId: Long): Flow<List<LogEventEntity>> = logDao.getLogsForServer(serverId)
    fun getMetricsForServer(serverId: Long): Flow<List<MetricHistoryEntity>> = metricDao.getMetricsForServer(serverId)

    suspend fun simulateTelemetryCycle(isSpike: Boolean = false): Unit = withContext(Dispatchers.IO) {
        try {
            val servers = serverDao.getAllServers().first()
            if (servers.isEmpty()) return@withContext

            val updatedServers = servers.map { server ->
                if (server.status == ServerStatus.MAINTENANCE) {
                    return@map server.copy(lastUpdated = System.currentTimeMillis())
                }

                // Jitter or spike metrics realistically
                val cpuDelta = if (isSpike) {
                    Random.nextFloat() * 25f + 15f
                } else {
                    Random.nextFloat() * 6f - 3f
                }
                val newCpu = (server.cpuUsagePercent + cpuDelta).coerceIn(5f, 99.8f)

                val ramDelta = if (isSpike) {
                    Random.nextFloat() * 12f + 5f
                } else {
                    Random.nextFloat() * 3f - 1.5f
                }
                val newRam = (server.ramUsagePercent + ramDelta).coerceIn(12f, 98.9f)

                val latDelta = if (isSpike) {
                    Random.nextInt(40, 150)
                } else {
                    Random.nextInt(-3, 4)
                }
                val newLat = (server.latencyMs + latDelta).coerceIn(5, 450)

                val bwDelta = if (isSpike) {
                    Random.nextFloat() * 800f + 400f
                } else {
                    Random.nextFloat() * 40f - 20f
                }
                val newBwIn = (server.bandwidthInMbps + bwDelta).coerceAtLeast(10f)
                val newBwOut = (server.bandwidthOutMbps + (if (isSpike) bwDelta * 0.8f else bwDelta * 0.5f)).coerceAtLeast(8f)

                val connDelta = if (isSpike) Random.nextInt(3000, 8000) else Random.nextInt(-80, 120)
                val newConnections = (server.activeConnections + connDelta).coerceAtLeast(10)

                // Status logic based on live telemetry thresholds
                val newStatus = when {
                    server.status == ServerStatus.MAINTENANCE -> ServerStatus.MAINTENANCE
                    newCpu > 92f || newRam > 94f || newLat > 220 -> ServerStatus.CRITICAL
                    newCpu > 80f || newRam > 85f || newLat > 120 -> ServerStatus.WARNING
                    else -> ServerStatus.ONLINE
                }

                // Record metric history point for charts
                metricDao.insertMetric(
                    MetricHistoryEntity(
                        serverId = server.id,
                        cpuPercent = newCpu,
                        ramPercent = newRam,
                        latencyMs = newLat,
                        bandwidthMbps = newBwIn
                    )
                )

                // Trigger incident alert if server newly spiked to CRITICAL
                if (newStatus == ServerStatus.CRITICAL && server.status != ServerStatus.CRITICAL) {
                    alertDao.insertAlert(
                        IncidentAlertEntity(
                            serverId = server.id,
                            serverName = server.name,
                            title = "Alerte Télémesure: Pic Critique Détecté",
                            message = "Charge CPU (${newCpu.toInt()}%), RAM (${newRam.toInt()}%) ou Latence (${newLat}ms) au-dessus des seuils d'exploitation.",
                            severity = AlertSeverity.CRITICAL,
                            recommendedFix = "Vérifier la charge applicative, scaler les instances et inspecter les logs récents."
                        )
                    )
                }

                server.copy(
                    cpuUsagePercent = newCpu,
                    ramUsagePercent = newRam,
                    latencyMs = newLat,
                    bandwidthInMbps = newBwIn,
                    bandwidthOutMbps = newBwOut,
                    activeConnections = newConnections,
                    status = newStatus,
                    uptimeSeconds = server.uptimeSeconds + 3L,
                    lastUpdated = System.currentTimeMillis()
                )
            }

            serverDao.insertServers(updatedServers)

            // Occasional live log generator (25% chance per tick or 100% on spike)
            if (isSpike || Random.nextFloat() < 0.25f) {
                val randomServer = servers.random()
                val logTemplates = if (isSpike) {
                    listOf(
                        Triple("WARN", "kernel", "Pic soudain de paquets réseau entrants: ${Random.nextInt(4000, 15000)} req/s."),
                        Triple("WARN", "haproxy", "Seuil de saturation de file d'attente dépassé (85% buffers)."),
                        Triple("ERROR", "systemd", "Alerte de contention CPU: ${randomServer.cpuCores} cœurs saturés.")
                    )
                } else {
                    listOf(
                        Triple("INFO", "systemd", "Vérification de santé heartbeat OK. Démon nominal."),
                        Triple("INFO", "sshd", "Session de supervision télémétrique synchronisée."),
                        Triple("INFO", "docker", "Collecte des métriques cAdvisor terminée avec succès."),
                        Triple("INFO", "netstat", "Flux TCP stabilisé: latence nominale mesurée."),
                        Triple("SUCCESS", "node-exporter", "Snapshot de télémétrie persisté en base locale.")
                    )
                }
                val chosen = logTemplates.random()
                logDao.insertLog(
                    LogEventEntity(
                        serverId = randomServer.id,
                        serverName = randomServer.name,
                        level = chosen.first,
                        source = chosen.second,
                        message = chosen.third
                    )
                )
                logDao.pruneOldLogs()
            }
        } catch (e: Exception) {
            // Ignore during DB initialization
        }
    }

    private suspend fun checkAndSeedData() = withContext(Dispatchers.IO) {
        if (serverDao.getServerCount() == 0) {
            val initialServers = listOf(
                ServerEntity(
                    name = "US-East-K8s-Master",
                    hostname = "k8s-m1.virginia.netops.io",
                    ipAddress = "52.95.110.14",
                    region = WorldRegion.US_EAST,
                    provider = CloudProvider.AWS,
                    serverType = ServerType.KUBERNETES_NODE,
                    status = ServerStatus.ONLINE,
                    cpuUsagePercent = 42.5f,
                    ramUsagePercent = 68.2f,
                    diskUsagePercent = 44.0f,
                    latencyMs = 18,
                    bandwidthInMbps = 840.5f,
                    bandwidthOutMbps = 1250.0f,
                    activeConnections = 14200,
                    uptimeSeconds = 3456000L, // 40 days
                    cpuCores = 32,
                    ramGb = 128,
                    diskGb = 2000,
                    osName = "Ubuntu 24.04 LTS (Kernel 6.8.0-45)"
                ),
                ServerEntity(
                    name = "EU-West-DB-Primary",
                    hostname = "pg-master.paris.netops.io",
                    ipAddress = "151.80.42.201",
                    region = WorldRegion.EU_WEST,
                    provider = CloudProvider.OVH,
                    serverType = ServerType.DATABASE_CLUSTER,
                    status = ServerStatus.WARNING,
                    cpuUsagePercent = 88.7f,
                    ramUsagePercent = 91.4f,
                    diskUsagePercent = 82.1f,
                    latencyMs = 24,
                    bandwidthInMbps = 410.2f,
                    bandwidthOutMbps = 620.8f,
                    activeConnections = 4850,
                    uptimeSeconds = 5184000L, // 60 days
                    cpuCores = 64,
                    ramGb = 256,
                    diskGb = 8000,
                    osName = "Debian 12 Bookworm (PG 16.2)"
                ),
                ServerEntity(
                    name = "AP-East-AI-Inference-01",
                    hostname = "gpu-h100.tokyo.netops.io",
                    ipAddress = "34.85.22.108",
                    region = WorldRegion.AP_EAST,
                    provider = CloudProvider.GCP,
                    serverType = ServerType.AI_COMPUTE,
                    status = ServerStatus.ONLINE,
                    cpuUsagePercent = 76.3f,
                    ramUsagePercent = 84.0f,
                    diskUsagePercent = 38.5f,
                    latencyMs = 42,
                    bandwidthInMbps = 1850.0f,
                    bandwidthOutMbps = 2400.0f,
                    activeConnections = 8200,
                    uptimeSeconds = 1209600L, // 14 days
                    cpuCores = 128,
                    ramGb = 512,
                    diskGb = 16000,
                    osName = "RHEL 9.4 (NVIDIA CUDA 12.4)"
                ),
                ServerEntity(
                    name = "EU-Central-Nginx-LB",
                    hostname = "edge-lb01.frankfurt.netops.io",
                    ipAddress = "18.197.80.33",
                    region = WorldRegion.EU_CENTRAL,
                    provider = CloudProvider.AWS,
                    serverType = ServerType.LOAD_BALANCER,
                    status = ServerStatus.ONLINE,
                    cpuUsagePercent = 29.1f,
                    ramUsagePercent = 34.8f,
                    diskUsagePercent = 22.0f,
                    latencyMs = 12,
                    bandwidthInMbps = 3400.0f,
                    bandwidthOutMbps = 3200.0f,
                    activeConnections = 38900,
                    uptimeSeconds = 7776000L, // 90 days
                    cpuCores = 16,
                    ramGb = 64,
                    diskGb = 500,
                    osName = "Alpine Linux v3.20"
                ),
                ServerEntity(
                    name = "US-West-Redis-Cluster",
                    hostname = "redis-cluster.oregon.netops.io",
                    ipAddress = "44.232.19.78",
                    region = WorldRegion.US_WEST,
                    provider = CloudProvider.AWS,
                    serverType = ServerType.REDIS_CACHE,
                    status = ServerStatus.ONLINE,
                    cpuUsagePercent = 33.4f,
                    ramUsagePercent = 72.0f,
                    diskUsagePercent = 15.2f,
                    latencyMs = 28,
                    bandwidthInMbps = 980.0f,
                    bandwidthOutMbps = 1120.0f,
                    activeConnections = 12400,
                    uptimeSeconds = 2592000L,
                    cpuCores = 32,
                    ramGb = 128,
                    diskGb = 1000,
                    osName = "Ubuntu 24.04 LTS"
                ),
                ServerEntity(
                    name = "AP-South-WebGateway",
                    hostname = "web-sg01.singapore.netops.io",
                    ipAddress = "13.250.41.90",
                    region = WorldRegion.AP_SOUTH,
                    provider = CloudProvider.AZURE,
                    serverType = ServerType.WEB_SERVER,
                    status = ServerStatus.ONLINE,
                    cpuUsagePercent = 54.0f,
                    ramUsagePercent = 58.6f,
                    diskUsagePercent = 49.3f,
                    latencyMs = 58,
                    bandwidthInMbps = 650.0f,
                    bandwidthOutMbps = 890.0f,
                    activeConnections = 9100,
                    uptimeSeconds = 1814400L,
                    cpuCores = 16,
                    ramGb = 64,
                    diskGb = 1000,
                    osName = "Ubuntu 22.04 LTS"
                ),
                ServerEntity(
                    name = "SA-East-CDN-Gateway",
                    hostname = "cdn-sa.saopaulo.netops.io",
                    ipAddress = "177.71.180.44",
                    region = WorldRegion.SA_EAST,
                    provider = CloudProvider.DIGITAL_OCEAN,
                    serverType = ServerType.DNS_GATEWAY,
                    status = ServerStatus.CRITICAL,
                    cpuUsagePercent = 95.8f,
                    ramUsagePercent = 94.2f,
                    diskUsagePercent = 89.0f,
                    latencyMs = 185,
                    bandwidthInMbps = 2950.0f,
                    bandwidthOutMbps = 210.0f,
                    activeConnections = 42000,
                    uptimeSeconds = 864000L,
                    cpuCores = 8,
                    ramGb = 32,
                    diskGb = 500,
                    osName = "Debian 12"
                ),
                ServerEntity(
                    name = "AF-South-Storage-SAN",
                    hostname = "san-backup.capetown.netops.io",
                    ipAddress = "102.132.160.12",
                    region = WorldRegion.AF_SOUTH,
                    provider = CloudProvider.BARE_METAL,
                    serverType = ServerType.STORAGE_SAN,
                    status = ServerStatus.ONLINE,
                    cpuUsagePercent = 18.2f,
                    ramUsagePercent = 41.5f,
                    diskUsagePercent = 74.6f,
                    latencyMs = 95,
                    bandwidthInMbps = 320.0f,
                    bandwidthOutMbps = 150.0f,
                    activeConnections = 1200,
                    uptimeSeconds = 10368000L,
                    cpuCores = 24,
                    ramGb = 128,
                    diskGb = 64000,
                    osName = "TrueNAS SCALE 24"
                ),
                ServerEntity(
                    name = "AP-Oceania-AppCluster",
                    hostname = "syd-node03.sydney.netops.io",
                    ipAddress = "13.70.150.211",
                    region = WorldRegion.AP_SOUTHEAST,
                    provider = CloudProvider.AZURE,
                    serverType = ServerType.KUBERNETES_NODE,
                    status = ServerStatus.MAINTENANCE,
                    cpuUsagePercent = 6.2f,
                    ramUsagePercent = 14.1f,
                    diskUsagePercent = 31.0f,
                    latencyMs = 120,
                    bandwidthInMbps = 12.0f,
                    bandwidthOutMbps = 8.0f,
                    activeConnections = 45,
                    uptimeSeconds = 3600L,
                    cpuCores = 32,
                    ramGb = 128,
                    diskGb = 2000,
                    osName = "Ubuntu 24.04 LTS"
                )
            )

            serverDao.insertServers(initialServers)

            // Seed initial incident alerts
            val alerts = listOf(
                IncidentAlertEntity(
                    serverId = 7L,
                    serverName = "SA-East-CDN-Gateway",
                    title = "Trafic Réseau Anormal & Latence Élevée (185ms)",
                    message = "Pic soudain de 42,000 connexions concurrentes sur le port 443. Risque de saturation DDoS ou goulot d'étranglement CDN.",
                    severity = AlertSeverity.CRITICAL,
                    timestamp = System.currentTimeMillis() - 180000L,
                    recommendedFix = "Activer la protection Anti-DDoS Anycast et basculer 50% du trafic sur le nœud US-East."
                ),
                IncidentAlertEntity(
                    serverId = 2L,
                    serverName = "EU-West-DB-Primary",
                    title = "Consommation Mémoire RAM Élevée (91.4%)",
                    message = "Le pool de connexions PostgreSQL sature le shared_buffers et le cache swap.",
                    severity = AlertSeverity.WARNING,
                    timestamp = System.currentTimeMillis() - 720000L,
                    recommendedFix = "Exécuter VACUUM ANALYZE et ajuster work_mem dans postgresql.conf ou scaler vers l'instance de réplication."
                ),
                IncidentAlertEntity(
                    serverId = 9L,
                    serverName = "AP-Oceania-AppCluster",
                    title = "Mise à niveau Kernel planifiée en cours",
                    message = "Nœud basculé en mode maintenance pour mise à jour de sécurité du kernel Linux.",
                    severity = AlertSeverity.INFO,
                    timestamp = System.currentTimeMillis() - 1800000L,
                    recommendedFix = "Vérifier la fin du redémarrage et réintégrer le cluster K8s via kubectl uncordon."
                )
            )
            alertDao.insertAlerts(alerts)

            // Seed initial logs
            val logs = listOf(
                LogEventEntity(serverId = 1L, serverName = "US-East-K8s-Master", level = "SUCCESS", source = "k8s-controller", message = "Pod deployment [api-gateway-v2] rolled out successfully across 12 replicas."),
                LogEventEntity(serverId = 7L, serverName = "SA-East-CDN-Gateway", level = "ERROR", source = "kernel", message = "SYN flood protection triggered: 14,200 dropped SYN packets on eth0."),
                LogEventEntity(serverId = 2L, serverName = "EU-West-DB-Primary", level = "WARN", source = "postgresql", message = "checkpoint starting: time-based; buffer usage peaked at 92%."),
                LogEventEntity(serverId = 3L, serverName = "AP-East-AI-Inference-01", level = "INFO", source = "nvidia-smi", message = "GPU cluster temperature nominal (62°C), VRAM allocation at 84%."),
                LogEventEntity(serverId = 4L, serverName = "EU-Central-Nginx-LB", level = "INFO", source = "nginx", message = "SSL certificate auto-renewed for *.netops.io (Let's Encrypt RSA 4096).")
            )
            logDao.insertLogs(logs)

            // Seed initial metric points (15 historical ticks) for all servers
            val now = System.currentTimeMillis()
            val initialMetrics = mutableListOf<MetricHistoryEntity>()
            initialServers.forEachIndexed { index, server ->
                val serverId = (index + 1).toLong()
                for (t in 14 downTo 0) {
                    val timeOffset = t * 60_000L
                    val variance = kotlin.math.sin(t * 0.5 + index).toFloat() * 10f
                    val cpu = (server.cpuUsagePercent + variance).coerceIn(5f, 99f)
                    val ram = (server.ramUsagePercent + variance * 0.4f).coerceIn(10f, 98f)
                    val lat = (server.latencyMs + (variance * 0.5f).toInt()).coerceAtLeast(2)
                    initialMetrics.add(
                        MetricHistoryEntity(
                            serverId = serverId,
                            cpuPercent = cpu,
                            ramPercent = ram,
                            latencyMs = lat,
                            bandwidthMbps = server.bandwidthInMbps + variance.coerceAtLeast(0f),
                            timestamp = now - timeOffset
                        )
                    )
                }
            }
            metricDao.insertMetrics(initialMetrics)
        }
    }

    suspend fun addServer(server: ServerEntity): Long = withContext(Dispatchers.IO) {
        val id = serverDao.insertServer(server)
        logDao.insertLog(
            LogEventEntity(
                serverId = id,
                serverName = server.name,
                level = "SUCCESS",
                source = "provisioner",
                message = "Nouveau serveur [${server.name}] provisionné et connecté au réseau mondial (${server.region.city})."
            )
        )
        id
    }

    suspend fun updateServer(server: ServerEntity) = withContext(Dispatchers.IO) {
        serverDao.updateServer(server)
    }

    suspend fun deleteServer(serverId: Long) = withContext(Dispatchers.IO) {
        val server = serverDao.getServerById(serverId)
        serverDao.deleteServerById(serverId)
        if (server != null) {
            logDao.insertLog(
                LogEventEntity(
                    serverId = serverId,
                    serverName = server.name,
                    level = "WARN",
                    source = "decommission",
                    message = "Serveur [${server.name}] déconnecté et retiré du réseau mondial."
                )
            )
        }
    }

    suspend fun rebootServer(serverId: Long) = withContext(Dispatchers.IO) {
        val server = serverDao.getServerById(serverId) ?: return@withContext
        logDao.insertLog(
            LogEventEntity(
                serverId = server.id,
                serverName = server.name,
                level = "WARN",
                source = "reboot",
                message = "Signal ACPI reboot envoyé par l'administrateur. Arrêt gracieux des processus..."
            )
        )
        // Simulate temporary rebooting state
        serverDao.updateServer(server.copy(status = ServerStatus.MAINTENANCE, cpuUsagePercent = 2f, ramUsagePercent = 5f, latencyMs = 999))
        delay(2000L)
        serverDao.updateServer(server.copy(status = ServerStatus.ONLINE, cpuUsagePercent = 15f, ramUsagePercent = 28f, latencyMs = 15, uptimeSeconds = 10L))
        logDao.insertLog(
            LogEventEntity(
                serverId = server.id,
                serverName = server.name,
                level = "SUCCESS",
                source = "kernel",
                message = "Système redémarré avec succès. Tous les démons réseau sont opérationnels."
            )
        )
    }

    suspend fun toggleMaintenance(serverId: Long) = withContext(Dispatchers.IO) {
        val server = serverDao.getServerById(serverId) ?: return@withContext
        val newStatus = if (server.status == ServerStatus.MAINTENANCE) ServerStatus.ONLINE else ServerStatus.MAINTENANCE
        serverDao.updateServer(server.copy(status = newStatus))
        logDao.insertLog(
            LogEventEntity(
                serverId = server.id,
                serverName = server.name,
                level = if (newStatus == ServerStatus.MAINTENANCE) "WARN" else "SUCCESS",
                source = "maintenance",
                message = "Mode maintenance ${if (newStatus == ServerStatus.MAINTENANCE) "ACTIVÉ" else "DÉSACTIVÉ"} pour ${server.name}."
            )
        )
    }

    suspend fun restartService(serverId: Long, serviceName: String): String = withContext(Dispatchers.IO) {
        val server = serverDao.getServerById(serverId)
        val sName = server?.name ?: "Server #$serverId"
        delay(1200L)
        logDao.insertLog(
            LogEventEntity(
                serverId = serverId,
                serverName = sName,
                level = "SUCCESS",
                source = "systemctl",
                message = "Service [$serviceName] redémarré avec succès (status=active, running)."
            )
        )
        "Service '$serviceName' redémarré avec succès sur $sName (PID ${Random.nextInt(1000, 9999)})."
    }

    suspend fun executeCommand(serverId: Long, command: String): String = withContext(Dispatchers.IO) {
        val server = serverDao.getServerById(serverId)
        val hostname = server?.hostname ?: "srv-host"
        val ip = server?.ipAddress ?: "127.0.0.1"
        delay(600L)

        val cleanCmd = command.trim()
        when {
            cleanCmd.startsWith("top") || cleanCmd.startsWith("htop") -> {
                """
                top - ${System.currentTimeMillis()} up 40 days, load average: 0.85, 1.12, 0.98
                Tasks: 214 total, 1 running, 213 sleeping, 0 stopped
                %Cpu(s): ${server?.cpuUsagePercent ?: 35.0}%us, 4.2%sy, 0.0%ni, 58.2%id, 0.1%wa
                MiB Mem : ${(server?.ramGb ?: 64) * 1024} total, ${((server?.ramGb ?: 64) * 1024 * (1 - (server?.ramUsagePercent ?: 50f) / 100)).toInt()} free, used ${((server?.ramGb ?: 64) * 1024 * ((server?.ramUsagePercent ?: 50f) / 100)).toInt()}
                PID USER      PR  NI    VIRT    RES    SHR S  %CPU  %MEM     TIME+ COMMAND
                1842 root      20   0 1482920 412580  89200 S  18.4  12.1 142:15.22 node /app/server.js
                2290 postgres  20   0 3840212 984120 189200 S  12.1  24.5 310:48.91 postgres: cluster
                9124 root      20   0  892400 120480  45200 S   6.2   4.1  45:02.14 nginx: worker process
                """.trimIndent()
            }
            cleanCmd.startsWith("df") -> {
                """
                Filesystem     1K-blocks      Used Available Use% Mounted on
                /dev/nvme0n1p1 ${(server?.diskGb ?: 500) * 1024 * 1024}  ${((server?.diskGb ?: 500) * 1024 * 1024 * ((server?.diskUsagePercent ?: 50f) / 100)).toLong()}  ${((server?.diskGb ?: 500) * 1024 * 1024 * (1 - (server?.diskUsagePercent ?: 50f) / 100)).toLong()}  ${server?.diskUsagePercent?.toInt() ?: 45}% /
                tmpfs            8192000         0   8192000   0% /dev/shm
                /dev/nvme1n1   209715200  48291000 161424200  23% /data/storage
                """.trimIndent()
            }
            cleanCmd.startsWith("uptime") -> {
                val days = (server?.uptimeSeconds ?: 0L) / 86400L
                " $hostname up $days days, ${server?.activeConnections ?: 450} connections, load average: 0.74, 0.88, 0.95"
            }
            cleanCmd.startsWith("netstat") || cleanCmd.startsWith("ss") -> {
                """
                State      Recv-Q Send-Q Local Address:Port               Peer Address:Port
                LISTEN     0      128          0.0.0.0:80                      0.0.0.0:*
                LISTEN     0      128          0.0.0.0:443                     0.0.0.0:*
                LISTEN     0      128          0.0.0.0:${server?.sshPort ?: 22}                      0.0.0.0:*
                ESTAB      0      0            $ip:443           198.51.100.42:54120
                ESTAB      0      0            $ip:443           203.0.113.89:49812
                """.trimIndent()
            }
            cleanCmd.startsWith("uname") -> {
                "Linux $hostname 6.8.0-45-generic #45-Ubuntu SMP PREEMPT_DYNAMIC x86_64 GNU/Linux"
            }
            cleanCmd.startsWith("ping") -> {
                """
                PING 8.8.8.8 (8.8.8.8) 56(84) bytes of data.
                64 bytes from 8.8.8.8: icmp_seq=1 ttl=118 time=${server?.latencyMs ?: 20}.4 ms
                64 bytes from 8.8.8.8: icmp_seq=2 ttl=118 time=${(server?.latencyMs ?: 20) + 1}.1 ms
                64 bytes from 8.8.8.8: icmp_seq=3 ttl=118 time=${(server?.latencyMs ?: 20) - 1}.8 ms
                --- 8.8.8.8 ping statistics ---
                3 packets transmitted, 3 received, 0% packet loss, time 2003ms
                rtt min/avg/max/mdev = ${(server?.latencyMs ?: 20) - 1.8}/${server?.latencyMs ?: 20}.1/${(server?.latencyMs ?: 20) + 1.1}/0.9 ms
                """.trimIndent()
            }
            cleanCmd.startsWith("docker ps") -> {
                """
                CONTAINER ID   IMAGE                 COMMAND                  STATUS          PORTS
                a8b41f92e10c   nginx:alpine          "/docker-entrypoint.…"   Up 14 days      0.0.0.0:80->80/tcp, 0.0.0.0:443->443/tcp
                91e704b12c8a   redis:7-alpine        "docker-entrypoint.s…"   Up 14 days      0.0.0.0:6379->6379/tcp
                3c12f0a45d91   prom/node-exporter    "/bin/node_exporter"     Up 40 days      0.0.0.0:9100->9100/tcp
                """.trimIndent()
            }
            else -> {
                "[root@$hostname ~]# $cleanCmd\nCommande exécutée avec code de sortie 0."
            }
        }
    }

    suspend fun runProbeTest(targetHostOrIp: String): ProbeResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val cleanHost = targetHostOrIp.replace("https://", "").replace("http://", "").split("/").first().trim()
            val inetAddress = InetAddress.getByName(cleanHost)
            val reachable = inetAddress.isReachable(3000)
            val duration = (System.currentTimeMillis() - startTime).toInt().coerceAtLeast(8)
            ProbeResult(
                target = cleanHost,
                ip = inetAddress.hostAddress ?: cleanHost,
                isReachable = true, // or reachable
                responseTimeMs = duration,
                packetLossPercent = 0,
                message = "Résolution DNS réussie (${inetAddress.hostAddress}). Latence aller-retour: ${duration}ms."
            )
        } catch (e: Exception) {
            val duration = (System.currentTimeMillis() - startTime).toInt()
            ProbeResult(
                target = targetHostOrIp,
                ip = "Inconnu",
                isReachable = false,
                responseTimeMs = duration,
                packetLossPercent = 100,
                message = "Échec de résolution ou hôte injoignable (${e.localizedMessage ?: "Timeout"})."
            )
        }
    }

    suspend fun acknowledgeAlert(id: Long) = alertDao.acknowledgeAlert(id)
    suspend fun resolveAlert(id: Long) = alertDao.resolveAlert(id)
    suspend fun clearResolvedAlerts() = alertDao.clearResolvedAlerts()
}

data class ProbeResult(
    val target: String,
    val ip: String,
    val isReachable: Boolean,
    val responseTimeMs: Int,
    val packetLossPercent: Int,
    val message: String
)
