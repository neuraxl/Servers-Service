package com.example

import com.example.data.local.ServerEntity
import com.example.data.model.CloudProvider
import com.example.data.model.HealthFilterOption
import com.example.data.model.ServerSortCriteria
import com.example.data.model.ServerStatus
import com.example.data.model.ServerType
import com.example.data.model.SortDirection
import com.example.data.model.WorldRegion
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    private val testServers = listOf(
        ServerEntity(
            id = 1,
            name = "Web-Prod-01",
            hostname = "web-01.us-east.net",
            ipAddress = "192.168.1.10",
            region = WorldRegion.US_EAST,
            provider = CloudProvider.AWS,
            serverType = ServerType.WEB_SERVER,
            status = ServerStatus.ONLINE,
            cpuUsagePercent = 45f,
            ramUsagePercent = 60f,
            diskUsagePercent = 50f,
            latencyMs = 18,
            uptimeSeconds = 1000L,
            activeConnections = 200,
            bandwidthInMbps = 50f,
            bandwidthOutMbps = 120f,
            installedServices = listOf("nginx", "ssh")
        ),
        ServerEntity(
            id = 2,
            name = "DB-Primary-01",
            hostname = "db-01.eu-west.net",
            ipAddress = "192.168.2.20",
            region = WorldRegion.EU_WEST,
            provider = CloudProvider.GCP,
            serverType = ServerType.DATABASE_CLUSTER,
            status = ServerStatus.CRITICAL,
            cpuUsagePercent = 94f,
            ramUsagePercent = 88f,
            diskUsagePercent = 91f,
            latencyMs = 85,
            uptimeSeconds = 5000L,
            activeConnections = 1200,
            bandwidthInMbps = 250f,
            bandwidthOutMbps = 400f,
            installedServices = listOf("postgresql", "ssh")
        ),
        ServerEntity(
            id = 3,
            name = "K8s-Worker-03",
            hostname = "k8s-03.ap-east.net",
            ipAddress = "192.168.3.30",
            region = WorldRegion.AP_EAST,
            provider = CloudProvider.AZURE,
            serverType = ServerType.KUBERNETES_NODE,
            status = ServerStatus.WARNING,
            cpuUsagePercent = 78f,
            ramUsagePercent = 75f,
            diskUsagePercent = 65f,
            latencyMs = 120,
            uptimeSeconds = 3000L,
            activeConnections = 650,
            bandwidthInMbps = 180f,
            bandwidthOutMbps = 220f,
            installedServices = listOf("docker", "k8s", "ssh")
        ),
        ServerEntity(
            id = 4,
            name = "Storage-SAN-01",
            hostname = "san-01.us-west.net",
            ipAddress = "192.168.4.40",
            region = WorldRegion.US_WEST,
            provider = CloudProvider.BARE_METAL,
            serverType = ServerType.STORAGE_SAN,
            status = ServerStatus.MAINTENANCE,
            cpuUsagePercent = 12f,
            ramUsagePercent = 20f,
            diskUsagePercent = 80f,
            latencyMs = 10,
            uptimeSeconds = 12000L,
            activeConnections = 50,
            bandwidthInMbps = 10f,
            bandwidthOutMbps = 15f,
            installedServices = listOf("nfs", "ssh")
        )
    )

    @Test
    fun testFilterByOnlineStatus() {
        val onlineServers = testServers.filter { it.status == ServerStatus.ONLINE }
        assertEquals(1, onlineServers.size)
        assertEquals("Web-Prod-01", onlineServers.first().name)
    }

    @Test
    fun testFilterByDegradedOrAlertStatus() {
        val alertServers = testServers.filter {
            it.status == ServerStatus.CRITICAL || it.status == ServerStatus.WARNING
        }
        assertEquals(2, alertServers.size)
        assertTrue(alertServers.any { it.name == "DB-Primary-01" })
        assertTrue(alertServers.any { it.name == "K8s-Worker-03" })
    }

    @Test
    fun testSortByCpuUsageDescending() {
        val sortedByCpu = testServers.sortedByDescending { it.cpuUsagePercent }
        assertEquals("DB-Primary-01", sortedByCpu[0].name) // 94%
        assertEquals("K8s-Worker-03", sortedByCpu[1].name) // 78%
        assertEquals("Web-Prod-01", sortedByCpu[2].name)   // 45%
        assertEquals("Storage-SAN-01", sortedByCpu[3].name) // 12%
    }

    @Test
    fun testSortByCpuUsageAscending() {
        val sortedByCpu = testServers.sortedBy { it.cpuUsagePercent }
        assertEquals("Storage-SAN-01", sortedByCpu[0].name) // 12%
        assertEquals("Web-Prod-01", sortedByCpu[1].name)   // 45%
        assertEquals("K8s-Worker-03", sortedByCpu[2].name) // 78%
        assertEquals("DB-Primary-01", sortedByCpu[3].name) // 94%
    }

    @Test
    fun testFleetAverageCpuCalculation() {
        val avgCpu = testServers.map { it.cpuUsagePercent }.average()
        // (45 + 94 + 78 + 12) / 4 = 229 / 4 = 57.25
        assertEquals(57.25, avgCpu, 0.01)
    }

    @Test
    fun testFleetPeakCpuCalculation() {
        val maxCpu = testServers.maxOf { it.cpuUsagePercent }
        assertEquals(94f, maxCpu, 0.01f)
    }
}

