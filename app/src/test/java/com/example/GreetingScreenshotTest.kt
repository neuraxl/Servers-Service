package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.local.ServerEntity
import com.example.data.model.CloudProvider
import com.example.data.model.ServerStatus
import com.example.data.model.ServerType
import com.example.data.model.WorldRegion
import com.example.ui.components.ServerCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleServer = ServerEntity(
        id = 1L,
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
        uptimeSeconds = 3456000L,
        cpuCores = 32,
        ramGb = 128,
        diskGb = 2000
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        ServerCard(
            server = sampleServer,
            onClick = {},
            onReboot = {},
            onToggleMaintenance = {},
            onOpenTerminal = {},
            onDelete = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

