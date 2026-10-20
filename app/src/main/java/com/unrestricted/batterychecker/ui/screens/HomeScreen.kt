package com.unrestricted.batterychecker.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unrestricted.batterychecker.R
import com.unrestricted.batterychecker.model.FilterMode
import com.unrestricted.batterychecker.ui.components.AppBatteryCard
import com.unrestricted.batterychecker.ui.components.ExpressiveFilterBar
import com.unrestricted.batterychecker.ui.components.ShizukuStatusBanner
import com.unrestricted.batterychecker.viewmodel.BatteryCheckerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: BatteryCheckerViewModel,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val apps by viewModel.filteredApps.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val showSystemApps by viewModel.showSystemApps.collectAsStateWithLifecycle()
    val isShizukuConnected by viewModel.isShizukuConnected.collectAsStateWithLifecycle()
    val isPermissionGranted by viewModel.isShizukuPermissionGranted.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val shizukuReady = isShizukuConnected && isPermissionGranted

    var showMenu by remember { mutableStateOf(false) }
    var showGuideDialog by remember { mutableStateOf(false) }

    if (showGuideDialog) {
        GuideDialog(onDismiss = { showGuideDialog = false })
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Battery Mode Checker",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "App Icon",
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(36.dp)
                    )
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "Options Menu"
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Show system apps") },
                            trailingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (showSystemApps) MaterialTheme.colorScheme.primary
                                            else Color.Transparent
                                        )
                                        .border(
                                            width = 1.5.dp,
                                            color = if (showSystemApps) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                            shape = RoundedCornerShape(4.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (showSystemApps) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Checked",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                viewModel.toggleShowSystemApps()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("System Battery Settings") },
                            leadingIcon = {
                                Icon(Icons.Rounded.Settings, contentDescription = null, modifier = Modifier.size(20.dp))
                            },
                            onClick = {
                                showMenu = false
                                viewModel.openNativeBatterySettings(context)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Refresh") },
                            leadingIcon = {
                                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                            },
                            onClick = {
                                showMenu = false
                                viewModel.loadApps()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Guide") },
                            leadingIcon = {
                                Icon(Icons.Rounded.MenuBook, contentDescription = null, modifier = Modifier.size(20.dp))
                            },
                            onClick = {
                                showMenu = false
                                showGuideDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("About") },
                            leadingIcon = {
                                Icon(Icons.Rounded.Info, contentDescription = null, modifier = Modifier.size(20.dp))
                            },
                            onClick = {
                                showMenu = false
                                onNavigateToAbout()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Content Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Banner about Shizuku / Shevery
                ShizukuStatusBanner(
                    isShizukuConnected = isShizukuConnected,
                    isPermissionGranted = isPermissionGranted,
                    onRequestPermission = { viewModel.requestShizukuPermission() },
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Brief paragraph explaining battery optimization modes
                Text(
                    text = "Battery Mode Checker audits and manages application battery optimization states. Apps in Unrestricted mode run autonomously without background restrictions or Doze limits. Apps in Restricted mode are strictly limited from background execution.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Expressive Equalized Pill Filters (All, Unre, Optim, Restr)
                ExpressiveFilterBar(
                    selectedFilter = selectedFilter,
                    stats = stats,
                    onFilterSelected = { viewModel.onFilterSelected(it) }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                thickness = 1.dp
            )

            // Table / List View of Installed Apps
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                AnimatedContent(
                    targetState = selectedFilter,
                    transitionSpec = {
                        if (targetState.ordinal > initialState.ordinal) {
                            (slideInHorizontally(animationSpec = tween(220)) { width -> width } + fadeIn(animationSpec = tween(220)))
                                .togetherWith(slideOutHorizontally(animationSpec = tween(220)) { width -> -width } + fadeOut(animationSpec = tween(220)))
                        } else {
                            (slideInHorizontally(animationSpec = tween(220)) { width -> -width } + fadeIn(animationSpec = tween(220)))
                                .togetherWith(slideOutHorizontally(animationSpec = tween(220)) { width -> width } + fadeOut(animationSpec = tween(220)))
                        }
                    },
                    label = "battery_tab_swipe",
                    modifier = Modifier.fillMaxSize()
                ) { _ ->
                    if (apps.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (!showSystemApps) "No third-party apps found in this category.\nEnable 'Show system apps' from the 3-dots menu." else "No matching applications found.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(
                                items = apps,
                                key = { it.packageName }
                            ) { app ->
                                AppBatteryCard(
                                    app = app,
                                    shizukuReady = shizukuReady,
                                    onStateChange = { newState ->
                                        if (shizukuReady) {
                                            viewModel.setAppOptimizationMode(app, newState)
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Connect Shizuku or Shevery to modify battery modes",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
