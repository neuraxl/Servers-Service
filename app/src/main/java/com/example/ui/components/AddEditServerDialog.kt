package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.ServerEntity
import com.example.data.model.CloudProvider
import com.example.data.model.ServerType
import com.example.data.model.WorldRegion
import com.example.ui.theme.ElectricCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditServerDialog(
    initialServer: ServerEntity? = null,
    onSave: (
        name: String,
        hostname: String,
        ipAddress: String,
        region: WorldRegion,
        provider: CloudProvider,
        serverType: ServerType,
        cpuCores: Int,
        ramGb: Int,
        diskGb: Int,
        sshPort: Int
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialServer?.name ?: "") }
    var hostname by remember { mutableStateOf(initialServer?.hostname ?: "") }
    var ipAddress by remember { mutableStateOf(initialServer?.ipAddress ?: "") }
    var selectedRegion by remember { mutableStateOf(initialServer?.region ?: WorldRegion.EU_WEST) }
    var selectedProvider by remember { mutableStateOf(initialServer?.provider ?: CloudProvider.AWS) }
    var selectedType by remember { mutableStateOf(initialServer?.serverType ?: ServerType.WEB_SERVER) }

    var cpuCoresText by remember { mutableStateOf((initialServer?.cpuCores ?: 16).toString()) }
    var ramGbText by remember { mutableStateOf((initialServer?.ramGb ?: 64).toString()) }
    var diskGbText by remember { mutableStateOf((initialServer?.diskGb ?: 500).toString()) }
    var sshPortText by remember { mutableStateOf((initialServer?.sshPort ?: 22).toString()) }

    var regionExpanded by remember { mutableStateOf(false) }
    var providerExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 640.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, Color(0xFF243656), RoundedCornerShape(24.dp)),
            color = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(ElectricCyan.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (initialServer == null) Icons.Default.Add else Icons.Default.Dns,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (initialServer == null) "Ajouter un Serveur" else "Modifier le Serveur",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Form Fields
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Server Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nom du serveur") },
                        placeholder = { Text("ex: US-East-Web-Cluster") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_server_name"),
                        colors = fieldColors()
                    )

                    // Hostname
                    OutlinedTextField(
                        value = hostname,
                        onValueChange = { hostname = it },
                        label = { Text("Nom d'hôte (FQDN)") },
                        placeholder = { Text("ex: web01.virginia.netops.io") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_server_hostname"),
                        colors = fieldColors()
                    )

                    // IP Address
                    OutlinedTextField(
                        value = ipAddress,
                        onValueChange = { ipAddress = it },
                        label = { Text("Adresse IP Publique / Privée") },
                        placeholder = { Text("ex: 54.180.20.91") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_server_ip"),
                        colors = fieldColors()
                    )

                    // Cloud Provider Dropdown
                    ExposedDropdownMenuBox(
                        expanded = providerExpanded,
                        onExpandedChange = { providerExpanded = !providerExpanded }
                    ) {
                        OutlinedTextField(
                            value = "${selectedProvider.label} (${selectedProvider.code})",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Fournisseur Cloud") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = fieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = providerExpanded,
                            onDismissRequest = { providerExpanded = false },
                            modifier = Modifier.background(Color(0xFF1E293B))
                        ) {
                            CloudProvider.values().forEach { provider ->
                                DropdownMenuItem(
                                    text = { Text("${provider.label} (${provider.code})", color = Color.White) },
                                    onClick = {
                                        selectedProvider = provider
                                        providerExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // World Region Dropdown
                    ExposedDropdownMenuBox(
                        expanded = regionExpanded,
                        onExpandedChange = { regionExpanded = !regionExpanded }
                    ) {
                        OutlinedTextField(
                            value = "${selectedRegion.city} (${selectedRegion.country})",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Région Géographique / Datacenter") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = regionExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = fieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = regionExpanded,
                            onDismissRequest = { regionExpanded = false },
                            modifier = Modifier.background(Color(0xFF1E293B))
                        ) {
                            WorldRegion.values().forEach { region ->
                                DropdownMenuItem(
                                    text = { Text("${region.city} (${region.country})", color = Color.White) },
                                    onClick = {
                                        selectedRegion = region
                                        regionExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Server Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = typeExpanded,
                        onExpandedChange = { typeExpanded = !typeExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedType.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Type d'infrastructure / Rôle") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = fieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = typeExpanded,
                            onDismissRequest = { typeExpanded = false },
                            modifier = Modifier.background(Color(0xFF1E293B))
                        ) {
                            ServerType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.label, color = Color.White) },
                                    onClick = {
                                        selectedType = type
                                        typeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Hardware Specs Row: CPU Cores & RAM
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = cpuCoresText,
                            onValueChange = { cpuCoresText = it.filter { char -> char.isDigit() } },
                            label = { Text("Cœurs CPU") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = fieldColors()
                        )
                        OutlinedTextField(
                            value = ramGbText,
                            onValueChange = { ramGbText = it.filter { char -> char.isDigit() } },
                            label = { Text("RAM (Go)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = fieldColors()
                        )
                    }

                    // Storage Disk & SSH Port
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = diskGbText,
                            onValueChange = { diskGbText = it.filter { char -> char.isDigit() } },
                            label = { Text("Disque (Go)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = fieldColors()
                        )
                        OutlinedTextField(
                            value = sshPortText,
                            onValueChange = { sshPortText = it.filter { char -> char.isDigit() } },
                            label = { Text("Port SSH") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = fieldColors()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions (Cancel / Confirm)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler", color = Color(0xFF94A3B8))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank() && ipAddress.isNotBlank()) {
                                onSave(
                                    name,
                                    if (hostname.isNotBlank()) hostname else "$name.netops.io",
                                    ipAddress,
                                    selectedRegion,
                                    selectedProvider,
                                    selectedType,
                                    cpuCoresText.toIntOrNull() ?: 16,
                                    ramGbText.toIntOrNull() ?: 64,
                                    diskGbText.toIntOrNull() ?: 500,
                                    sshPortText.toIntOrNull() ?: 22
                                )
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = Color(0xFF0A0F1D)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("button_save_server")
                    ) {
                        Text(
                            text = if (initialServer == null) "Provisionner le Serveur" else "Enregistrer les modifications",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = ElectricCyan,
    unfocusedBorderColor = Color(0xFF334155),
    focusedLabelColor = ElectricCyan,
    unfocusedLabelColor = Color(0xFF94A3B8),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedContainerColor = Color(0xFF0A0F1D),
    unfocusedContainerColor = Color(0xFF0A0F1D)
)
