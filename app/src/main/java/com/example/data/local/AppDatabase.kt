package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.model.AlertSeverity
import com.example.data.model.CloudProvider
import com.example.data.model.ServerStatus
import com.example.data.model.ServerType
import com.example.data.model.WorldRegion

class Converters {
    @TypeConverter
    fun fromWorldRegion(region: WorldRegion): String = region.name

    @TypeConverter
    fun toWorldRegion(value: String): WorldRegion = runCatching { WorldRegion.valueOf(value) }.getOrDefault(WorldRegion.US_EAST)

    @TypeConverter
    fun fromCloudProvider(provider: CloudProvider): String = provider.name

    @TypeConverter
    fun toCloudProvider(value: String): CloudProvider = runCatching { CloudProvider.valueOf(value) }.getOrDefault(CloudProvider.AWS)

    @TypeConverter
    fun fromServerType(type: ServerType): String = type.name

    @TypeConverter
    fun toServerType(value: String): ServerType = runCatching { ServerType.valueOf(value) }.getOrDefault(ServerType.WEB_SERVER)

    @TypeConverter
    fun fromServerStatus(status: ServerStatus): String = status.name

    @TypeConverter
    fun toServerStatus(value: String): ServerStatus = runCatching { ServerStatus.valueOf(value) }.getOrDefault(ServerStatus.ONLINE)

    @TypeConverter
    fun fromAlertSeverity(severity: AlertSeverity): String = severity.name

    @TypeConverter
    fun toAlertSeverity(value: String): AlertSeverity = runCatching { AlertSeverity.valueOf(value) }.getOrDefault(AlertSeverity.INFO)
}

@Database(
    entities = [
        ServerEntity::class,
        IncidentAlertEntity::class,
        LogEventEntity::class,
        MetricHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serverDao(): ServerDao
    abstract fun incidentAlertDao(): IncidentAlertDao
    abstract fun logEventDao(): LogEventDao
    abstract fun metricHistoryDao(): MetricHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "netglobal_servers.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
