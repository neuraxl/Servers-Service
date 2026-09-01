package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.ServerEntity
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConsoleOutputDialog(
    server: ServerEntity,
    output: String,
    isLoading: Boolean,
    onExecuteCommand: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var inputCommand by remember { mutableStateOf("") }
    val verticalScroll = rememberScrollState()

    val quickCommands = listOf(
        "top -b -n 1",
        "df -h",
        "docker ps",
        "netstat -tulnp",
        "uptime",
        "uname -a",
        "ping -c 3 8.8.8.8"
    )

    LaunchedEffect(output) {
        if (output.isNotEmpty()) {
            verticalScroll.animateScrollTo(verticalScroll.maxValue)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
            color = Color(0xFF080D1A)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header (Terminal Title + Close)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(ElectricCyan.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Terminal SSH • ${server.name}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "root@${server.hostname}:${server.sshPort}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = NeonGreen
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Terminal Output Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF020617))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(verticalScroll)
                    ) {
                        Text(
                            text = "Connexion établie avec ${server.ipAddress} [SSH-2.0-OpenSSH_9.6p1].\n" +
                                    "Authentifié via clé ed25519.\n" +
                                    "Tapez une commande Linux ou choisissez un raccourci ci-dessous.\n----------------------------------------",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF64748B)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        if (output.isNotEmpty()) {
                            Text(
                                text = output,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFE2E8F0)
                            )
                        }

                        if (isLoading) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = ElectricCyan
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Exécution en cours...",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = ElectricCyan
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Command Shortcuts
                Text(
                    text = "Commandes Rapides :",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    quickCommands.forEach { cmd ->
                        AssistChip(
                            onClick = {
                                inputCommand = cmd
                                onExecuteCommand(cmd)
                            },
                            label = { Text(cmd, fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFF1E293B),
                                labelColor = ElectricCyan
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Input Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputCommand,
                        onValueChange = { inputCommand = it },
                        placeholder = { Text("Ex: systemctl status, htop, df -h", fontSize = 12.sp, color = Color(0xFF64748B)) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("terminal_input_field"),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (inputCommand.isNotBlank()) {
                                onExecuteCommand(inputCommand)
                                inputCommand = ""
                            }
                        }),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = Color(0xFF1E293B),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A)
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputCommand.isNotBlank()) {
                                onExecuteCommand(inputCommand)
                                inputCommand = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(ElectricCyan, RoundedCornerShape(12.dp))
                            .testTag("terminal_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Exécuter",
                            tint = Color(0xFF0A0F1D)
                        )
                    }
                }
            }
        }
    }
}
