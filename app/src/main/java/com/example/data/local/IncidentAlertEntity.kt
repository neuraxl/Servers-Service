package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AlertSeverity

@Entity(tableName = "incident_alerts")
data class IncidentAlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val serverId: Long?,
    val serverName: String,
    val title: String,
    val message: String,
    val severity: AlertSeverity,
    val timestamp: Long = System.currentTimeMillis(),
    val isAcknowledged: Boolean = false,
    val isResolved: Boolean = false,
    val recommendedFix: String = ""
)
