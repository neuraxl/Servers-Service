package com.example.data.model

enum class ServerStatus(val label: String) {
    ONLINE("En Ligne"),
    WARNING("Avertissement"),
    CRITICAL("Critique"),
    MAINTENANCE("Maintenance")
}

enum class ServerType(val label: String, val iconName: String) {
    WEB_SERVER("Serveur Web Nginx/Apache", "web"),
    DATABASE_CLUSTER("Cluster Base de Données", "database"),
    KUBERNETES_NODE("Nœud Kubernetes", "k8s"),
    LOAD_BALANCER("Équilibreur de Charge", "load_balancer"),
    REDIS_CACHE("Cache Redis/Memcached", "cache"),
    AI_COMPUTE("Cluster Calcul IA GPU", "ai"),
    DNS_GATEWAY("Passerelle DNS & CDN", "dns"),
    STORAGE_SAN("Baie Stockage SAN/NAS", "storage")
}

enum class CloudProvider(val label: String, val code: String) {
    AWS("Amazon Web Services", "AWS"),
    GCP("Google Cloud Platform", "GCP"),
    AZURE("Microsoft Azure", "AZURE"),
    OVH("OVHcloud", "OVH"),
    DIGITAL_OCEAN("DigitalOcean", "DO"),
    BARE_METAL("Bare-Metal Privé", "ON-PREM")
}

enum class WorldRegion(
    val id: String,
    val city: String,
    val country: String,
    val latPercent: Float, // 0..1 for world map canvas
    val lonPercent: Float  // 0..1 for world map canvas
) {
    US_EAST("us-east", "Virginie (N. Virginia)", "USA", 0.36f, 0.28f),
    US_WEST("us-west", "Oregon (Silicon Valley)", "USA", 0.34f, 0.16f),
    EU_WEST("eu-west", "Paris / Londres", "Europe", 0.28f, 0.49f),
    EU_CENTRAL("eu-central", "Francfort (Germany)", "Europe", 0.27f, 0.53f),
    AP_EAST("ap-east", "Tokyo (Japon)", "Asie", 0.36f, 0.85f),
    AP_SOUTH("ap-south", "Singapour", "Asie", 0.55f, 0.77f),
    SA_EAST("sa-east", "São Paulo", "Amérique du Sud", 0.73f, 0.34f),
    AF_SOUTH("af-south", "Le Cap (Johannesburg)", "Afrique", 0.78f, 0.54f),
    AP_SOUTHEAST("ap-southeast", "Sydney", "Océanie", 0.76f, 0.89f)
}

enum class AlertSeverity(val label: String) {
    INFO("Info"),
    WARNING("Avertissement"),
    CRITICAL("Critique")
}

enum class HealthFilterOption(val label: String) {
    ALL("Tous"),
    ONLINE_ONLY("En Ligne (Sain)"),
    OFFLINE_OR_DEGRADED("Critique & Alertes"),
    CRITICAL_ONLY("Critique"),
    WARNING_ONLY("Avertissement"),
    MAINTENANCE_ONLY("Maintenance")
}

enum class ServerSortCriteria(val label: String, val shortLabel: String) {
    CPU_USAGE("Taux d'utilisation CPU", "Charge CPU"),
    HEALTH_STATUS("État de Santé (Santé / Alertes)", "Santé"),
    LATENCY("Latence Réseau WAN", "Latence"),
    RAM_USAGE("Utilisation Mémoire RAM", "RAM"),
    NAME("Nom du Serveur", "Nom")
}

enum class SortDirection(val label: String) {
    DESCENDING("Décroissant ↓"),
    ASCENDING("Croissant ↑")
}

enum class ChartMetricType(val label: String, val unit: String, val maxScale: Float) {
    CPU("Charge CPU", "%", 100f),
    RAM("Mémoire RAM", "%", 100f),
    LATENCY("Latence WAN", "ms", 250f),
    BANDWIDTH("Bande Passante", "Mbps", 500f)
}

enum class ChartTimeRange(val label: String, val pointsCount: Int) {
    MINUTES_5("5 min", 10),
    MINUTES_15("15 min", 20),
    MINUTES_30("30 min", 35),
    ALL("Tout", 60)
}

enum class ChartDisplayMode(val label: String) {
    FLEET_TRENDS("Moyenne & Pics Flotte"),
    MULTI_SERVER("Comparatif Multi-Nœuds"),
    FOCUSED_NODE("Nœud Isolé")
}

