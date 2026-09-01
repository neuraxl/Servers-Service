package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "metric_history")
data class MetricHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val serverId: Long,
    val cpuPercent: Float,
    val ramPercent: Float,
    val latencyMs: Int,
    val bandwidthMbps: Float,
    val timestamp: Long = System.currentTimeMillis()
)
