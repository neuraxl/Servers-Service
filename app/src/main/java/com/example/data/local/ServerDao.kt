package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {
    @Query("SELECT * FROM servers ORDER BY status ASC, name ASC")
    fun getAllServers(): Flow<List<ServerEntity>>

    @Query("SELECT * FROM servers WHERE id = :id")
    fun getServerByIdFlow(id: Long): Flow<ServerEntity?>

    @Query("SELECT * FROM servers WHERE id = :id")
    suspend fun getServerById(id: Long): ServerEntity?

    @Query("SELECT COUNT(*) FROM servers")
    suspend fun getServerCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: ServerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServers(servers: List<ServerEntity>)

    @Update
    suspend fun updateServer(server: ServerEntity)

    @Delete
    suspend fun deleteServer(server: ServerEntity)

    @Query("DELETE FROM servers WHERE id = :id")
    suspend fun deleteServerById(id: Long)
}

@Dao
interface IncidentAlertDao {
    @Query("SELECT * FROM incident_alerts ORDER BY isResolved ASC, timestamp DESC")
    fun getAllAlerts(): Flow<List<IncidentAlertEntity>>

    @Query("SELECT * FROM incident_alerts WHERE isResolved = 0 ORDER BY severity DESC, timestamp DESC")
    fun getActiveAlerts(): Flow<List<IncidentAlertEntity>>

    @Query("SELECT COUNT(*) FROM incident_alerts WHERE isResolved = 0")
    fun getActiveAlertCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: IncidentAlertEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<IncidentAlertEntity>)

    @Query("UPDATE incident_alerts SET isAcknowledged = 1 WHERE id = :id")
    suspend fun acknowledgeAlert(id: Long)

    @Query("UPDATE incident_alerts SET isResolved = 1, isAcknowledged = 1 WHERE id = :id")
    suspend fun resolveAlert(id: Long)

    @Query("DELETE FROM incident_alerts WHERE id = :id")
    suspend fun deleteAlert(id: Long)

    @Query("DELETE FROM incident_alerts WHERE isResolved = 1")
    suspend fun clearResolvedAlerts()
}

@Dao
interface LogEventDao {
    @Query("SELECT * FROM log_events ORDER BY timestamp DESC LIMIT 100")
    fun getRecentLogs(): Flow<List<LogEventEntity>>

    @Query("SELECT * FROM log_events WHERE serverId = :serverId ORDER BY timestamp DESC LIMIT 60")
    fun getLogsForServer(serverId: Long): Flow<List<LogEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: LogEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<LogEventEntity>)

    @Query("DELETE FROM log_events WHERE id NOT IN (SELECT id FROM log_events ORDER BY timestamp DESC LIMIT 200)")
    suspend fun pruneOldLogs()
}

@Dao
interface MetricHistoryDao {
    @Query("SELECT * FROM metric_history WHERE serverId = :serverId ORDER BY timestamp ASC LIMIT 60")
    fun getMetricsForServer(serverId: Long): Flow<List<MetricHistoryEntity>>

    @Query("SELECT * FROM metric_history ORDER BY timestamp ASC")
    fun getAllRecentMetrics(): Flow<List<MetricHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetric(metric: MetricHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetrics(metrics: List<MetricHistoryEntity>)

    @Query("DELETE FROM metric_history WHERE serverId = :serverId AND id NOT IN (SELECT id FROM metric_history WHERE serverId = :serverId ORDER BY timestamp DESC LIMIT 60)")
    suspend fun pruneMetricsForServer(serverId: Long)
}
