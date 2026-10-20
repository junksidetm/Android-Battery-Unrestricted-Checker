package com.unrestricted.batterychecker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unrestricted.batterychecker.model.FilterMode
import com.unrestricted.batterychecker.model.OptimizationStats
import com.unrestricted.batterychecker.ui.theme.ColorOptimized
import com.unrestricted.batterychecker.ui.theme.ColorRestricted
import com.unrestricted.batterychecker.ui.theme.ColorUnrestricted

@Composable
fun ExpressiveFilterBar(
    selectedFilter: FilterMode,
    stats: OptimizationStats,
    onFilterSelected: (FilterMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        PillItem(
            title = "All",
            count = stats.totalApps,
            isSelected = selectedFilter == FilterMode.ALL,
            activeColor = MaterialTheme.colorScheme.primary,
            onClick = { onFilterSelected(FilterMode.ALL) },
            modifier = Modifier.weight(1f)
        )
        PillItem(
            title = "Unre",
            count = stats.unrestrictedCount,
            isSelected = selectedFilter == FilterMode.UNRESTRICTED,
            activeColor = ColorUnrestricted,
            onClick = { onFilterSelected(FilterMode.UNRESTRICTED) },
            modifier = Modifier.weight(1f)
        )
        PillItem(
            title = "Optim",
            count = stats.optimizedCount,
            isSelected = selectedFilter == FilterMode.OPTIMIZED,
            activeColor = ColorOptimized,
            onClick = { onFilterSelected(FilterMode.OPTIMIZED) },
            modifier = Modifier.weight(1f)
        )
        PillItem(
            title = "Restr",
            count = stats.restrictedCount,
            isSelected = selectedFilter == FilterMode.RESTRICTED,
            activeColor = ColorRestricted,
            onClick = { onFilterSelected(FilterMode.RESTRICTED) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PillItem(
    title: String,
    count: Int,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            activeColor
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        label = "pill_bg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            Color.White
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "pill_fg"
    )

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                maxLines = 1,
                softWrap = false
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "($count)",
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = contentColor.copy(alpha = if (isSelected) 0.95f else 0.8f),
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
