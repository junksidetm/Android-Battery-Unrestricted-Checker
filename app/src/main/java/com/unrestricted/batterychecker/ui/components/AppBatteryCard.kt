package com.unrestricted.batterychecker.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unrestricted.batterychecker.model.AppBatteryInfo
import com.unrestricted.batterychecker.model.BatteryOptimizationState
import com.unrestricted.batterychecker.ui.theme.ColorOptimized
import com.unrestricted.batterychecker.ui.theme.ColorRestricted
import com.unrestricted.batterychecker.ui.theme.ColorUnknown
import com.unrestricted.batterychecker.ui.theme.ColorUnrestricted

@Composable
fun AppBatteryCard(
    app: AppBatteryInfo,
    shizukuReady: Boolean,
    onStateChange: (BatteryOptimizationState) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val stateColor by animateColorAsState(
        targetValue = when (app.state) {
            BatteryOptimizationState.UNRESTRICTED -> ColorUnrestricted
            BatteryOptimizationState.OPTIMIZED -> ColorOptimized
            BatteryOptimizationState.RESTRICTED -> ColorRestricted
            BatteryOptimizationState.UNKNOWN -> ColorUnknown
        },
        label = "stateColor"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App icon
            AppIcon(
                packageName = app.packageName,
                modifier = Modifier.size(44.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            // App Name and Status
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = app.state.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = stateColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Mode Switching Segmented Pills
            if (shizukuReady) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MiniStatePill(
                        label = "Unrest",
                        isSelected = app.state == BatteryOptimizationState.UNRESTRICTED,
                        activeColor = ColorUnrestricted,
                        onClick = { onStateChange(BatteryOptimizationState.UNRESTRICTED) }
                    )
                    MiniStatePill(
                        label = "Optim",
                        isSelected = app.state == BatteryOptimizationState.OPTIMIZED,
                        activeColor = ColorOptimized,
                        onClick = { onStateChange(BatteryOptimizationState.OPTIMIZED) }
                    )
                    MiniStatePill(
                        label = "Restr",
                        isSelected = app.state == BatteryOptimizationState.RESTRICTED,
                        activeColor = ColorRestricted,
                        onClick = { onStateChange(BatteryOptimizationState.RESTRICTED) }
                    )
                }
            } else {
                IconButton(
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", app.packageName, null)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val fallbackIntent = Intent(Settings.ACTION_SETTINGS)
                            context.startActivity(fallbackIntent)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.OpenInNew,
                        contentDescription = "Open System Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
            thickness = 0.5.dp,
            modifier = Modifier.padding(start = 74.dp)
        )
    }
}

@Composable
private fun MiniStatePill(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val bg = if (isSelected) activeColor else MaterialTheme.colorScheme.surfaceContainerHigh
    val fg = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .height(30.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = fg,
            maxLines = 1
        )
    }
}
