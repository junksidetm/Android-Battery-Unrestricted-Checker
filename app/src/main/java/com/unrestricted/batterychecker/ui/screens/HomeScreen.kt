package com.unrestricted.batterychecker.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.CheckBoxOutlineBlank
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SortByAlpha
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unrestricted.batterychecker.model.SortOrder
import com.unrestricted.batterychecker.ui.components.AppBatteryCard
import com.unrestricted.batterychecker.ui.components.BatteryDistributionCanvas
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
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
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
            TopAppBar(
                title = {
                    Text(
                        text = "Battery Mode Checker",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp, end = 4.dp)
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.BatteryChargingFull,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                actions = {
                    // 3-dots overflow menu
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "Options Menu"
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            shape = RoundedCornerShape(20.dp),
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            // 1. Refresh
                            DropdownMenuItem(
                                text = { Text("Refresh", style = MaterialTheme.typography.bodyMedium) },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                                },
                                onClick = {
                                    showMenu = false
                                    viewModel.loadApps()
                                }
                            )

                            // 2. Open System Battery Settings
                            DropdownMenuItem(
                                text = { Text("System Battery Settings", style = MaterialTheme.typography.bodyMedium) },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Settings, contentDescription = null, modifier = Modifier.size(20.dp))
                                },
                                onClick = {
                                    showMenu = false
                                    viewModel.openNativeBatterySettings(context)
                                }
                            )

                            // 3. Sort Order Options
                            DropdownMenuItem(
                                text = { Text("Sort: Name (A-Z)", style = MaterialTheme.typography.bodyMedium) },
                                leadingIcon = {
                                    Icon(Icons.Rounded.SortByAlpha, contentDescription = null, modifier = Modifier.size(20.dp))
                                },
                                trailingIcon = {
                                    if (sortOrder == SortOrder.NAME_ASC) {
                                        Icon(Icons.Rounded.Check, contentDescription = "Active", tint = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    viewModel.setSortOrder(SortOrder.NAME_ASC)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Sort: State Priority", style = MaterialTheme.typography.bodyMedium) },
                                leadingIcon = {
                                    Icon(Icons.Rounded.BatteryChargingFull, contentDescription = null, modifier = Modifier.size(20.dp))
                                },
                                trailingIcon = {
                                    if (sortOrder == SortOrder.STATE_PRIORITY) {
                                        Icon(Icons.Rounded.Check, contentDescription = "Active", tint = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    viewModel.setSortOrder(SortOrder.STATE_PRIORITY)
                                }
                            )

                            // 4. Show system apps toggle
                            DropdownMenuItem(
                                text = { Text("Show system apps", style = MaterialTheme.typography.bodyMedium) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (showSystemApps) Icons.Rounded.CheckBox else Icons.Rounded.CheckBoxOutlineBlank,
                                        contentDescription = null,
                                        tint = if (showSystemApps) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                onClick = {
                                    viewModel.toggleShowSystemApps()
                                }
                            )

                            // 5. Guide
                            DropdownMenuItem(
                                text = { Text("Guide", style = MaterialTheme.typography.bodyMedium) },
                                leadingIcon = {
                                    Icon(Icons.Rounded.MenuBook, contentDescription = null, modifier = Modifier.size(20.dp))
                                },
                                onClick = {
                                    showMenu = false
                                    showGuideDialog = true
                                }
                            )

                            // 6. About
                            DropdownMenuItem(
                                text = { Text("About", style = MaterialTheme.typography.bodyMedium) },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Info, contentDescription = null, modifier = Modifier.size(20.dp))
                                },
                                onClick = {
                                    showMenu = false
                                    onNavigateToAbout()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Shizuku / Shevery Connection Banner
            ShizukuStatusBanner(
                isShizukuConnected = isShizukuConnected,
                isPermissionGranted = isPermissionGranted,
                onRequestPermission = { viewModel.requestShizukuPermission() }
            )

            // 2. Native Canvas Battery Distribution Overview Hero Card
            BatteryDistributionCanvas(stats = stats)

            // 3. Expressive Pill Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                placeholder = {
                    Text(
                        "Search apps or package...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Clear Search")
                        }
                    }
                },
                shape = RoundedCornerShape(28.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            // 4. Expressive Filter Chips Bar
            ExpressiveFilterBar(
                selectedFilter = selectedFilter,
                stats = stats,
                onFilterSelected = { viewModel.onFilterSelected(it) }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 5. App List or Empty / Loading State
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                }
            } else if (apps.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SearchOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (!showSystemApps) "No third-party apps found.\nEnable 'Show system apps' from the 3-dots menu." else "No matching applications found.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
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
