package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "log_events")
data class LogEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val serverId: Long,
    val serverName: String,
    val level: String, // "INFO", "WARN", "ERROR", "SUCCESS"
    val source: String, // e.g. "nginx", "systemd", "sshd", "docker", "kernel"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
