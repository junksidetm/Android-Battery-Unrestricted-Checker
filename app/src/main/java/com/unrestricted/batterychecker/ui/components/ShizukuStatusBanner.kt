package com.unrestricted.batterychecker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ShizukuStatusBanner(
    isShizukuConnected: Boolean,
    isPermissionGranted: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFullyReady = isShizukuConnected && isPermissionGranted

    val backgroundColor = if (isFullyReady) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }

    val contentColor = if (isFullyReady) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isFullyReady) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isFullyReady) "Shizuku Connected" else if (isShizukuConnected) "Permission Needed" else "Shizuku / Shevery Disconnected",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                if (!isFullyReady) {
                    Text(
                        text = if (isShizukuConnected) {
                            "Service running. Authorization required to modify battery modes"
                        } else {
                            "Start Shizuku or Shevery via Wireless ADB to manage battery optimization without root"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.85f)
                    )
                }
            }
        }

        if (isShizukuConnected && !isPermissionGranted) {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = contentColor,
                    contentColor = backgroundColor
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Grant Authorization", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
