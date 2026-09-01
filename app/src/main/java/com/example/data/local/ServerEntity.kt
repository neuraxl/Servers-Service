package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.CloudProvider
import com.example.data.model.ServerStatus
import com.example.data.model.ServerType
import com.example.data.model.WorldRegion

@Entity(tableName = "servers")
data class ServerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val hostname: String,
    val ipAddress: String,
    val region: WorldRegion,
    val provider: CloudProvider,
    val serverType: ServerType,
    val status: ServerStatus,
    val cpuUsagePercent: Float, // 0..100
    val ramUsagePercent: Float, // 0..100
    val diskUsagePercent: Float, // 0..100
    val latencyMs: Int,
    val bandwidthInMbps: Float,
    val bandwidthOutMbps: Float,
    val activeConnections: Int,
    val uptimeSeconds: Long,
    val cpuCores: Int,
    val ramGb: Int,
    val diskGb: Int,
    val osName: String = "Ubuntu 24.04 LTS (Kernel 6.8.0)",
    val sshPort: Int = 22,
    val isAutoScaled: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)
