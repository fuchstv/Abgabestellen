package com.example.abgabestellenberlin.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.abgabestellenberlin.R
import com.example.abgabestellenberlin.data.model.DropOffPoint
import com.example.abgabestellenberlin.ui.viewmodel.MainViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(),
    onSignInClick: () -> Unit
) {
    val navController = rememberNavController()
    val dropOffPoints by viewModel.dropOffPoints.collectAsState()
    val selectedPoint by viewModel.selectedPoint.collectAsState()
    val isCollaborator by viewModel.isCollaborator.collectAsState()
    val userAccount by viewModel.userAccount.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showSuggestionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    LaunchedEffect(selectedPoint) {
        if (selectedPoint != null) {
            showBottomSheet = true
        }
    }

    var selectedItem by remember { mutableIntStateOf(0) }
    val items = mutableListOf(
        stringResource(R.string.map_view),
        stringResource(R.string.list_view),
        stringResource(R.string.profile_view)
    )
    val icons = mutableListOf(Icons.Filled.LocationOn, Icons.AutoMirrored.Filled.List, Icons.Filled.Person)
    
    if (isCollaborator) {
        items.add(stringResource(R.string.admin_panel))
        icons.add(Icons.Filled.Settings)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            when (index) {
                                0 -> navController.navigate("map")
                                1 -> navController.navigate("list")
                                2 -> navController.navigate("profile")
                                3 -> navController.navigate("admin")
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "map",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("map") { MapScreen(dropOffPoints) { viewModel.selectPoint(it) } }
            composable("list") { ListScreen(dropOffPoints) { viewModel.selectPoint(it) } }
            composable("profile") { ProfileScreen(viewModel, onSignInClick) }
            composable("admin") { AdminScreen() }
        }

        if (showBottomSheet && selectedPoint != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                    viewModel.selectPoint(null)
                },
                sheetState = sheetState
            ) {
                PointDetailContent(selectedPoint!!) {
                    if (userAccount == null) {
                        onSignInClick()
                    } else {
                        showSuggestionDialog = true
                        showBottomSheet = false
                    }
                }
            }
        }

        if (showSuggestionDialog && selectedPoint != null) {
            SuggestionDialog(
                onDismiss = { showSuggestionDialog = false },
                onSubmit = { text ->
                    viewModel.submitSuggestion(selectedPoint!!, text)
                    showSuggestionDialog = false
                    viewModel.selectPoint(null)
                }
            )
        }
    }
}

@Composable
fun SuggestionDialog(onDismiss: () -> Unit, onSubmit: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Änderung vorschlagen") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Was möchtest du ändern?") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onSubmit(text) }) {
                Text("Absenden")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

@Composable
fun PointDetailContent(point: DropOffPoint, onSuggestClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = point.name, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = point.address, style = MaterialTheme.typography.bodyLarge)
        Text(text = "${point.zipCode} ${point.district}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Öffnungszeiten:", style = MaterialTheme.typography.titleMedium)
        Text(text = point.dropOffTimes)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Was wird angenommen?:", style = MaterialTheme.typography.titleMedium)
        Text(text = point.acceptedItems)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onSuggestClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.suggest_change))
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun MapScreen(points: List<DropOffPoint>, onPointClick: (DropOffPoint) -> Unit) {
    val berlin = LatLng(52.5200, 13.4050)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(berlin, 10f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        points.forEach { point ->
            if (point.latitude != null && point.longitude != null) {
                Marker(
                    state = MarkerState(position = LatLng(point.latitude, point.longitude)),
                    title = point.name,
                    snippet = point.address,
                    onClick = {
                        onPointClick(point)
                        true
                    }
                )
            }
        }
    }
}

@Composable
fun ListScreen(points: List<DropOffPoint>, onPointClick: (DropOffPoint) -> Unit) {
    LazyColumn {
        items(points) { point ->
            ListItem(
                headlineContent = { Text(point.name) },
                supportingContent = { Text(point.address) },
                overlineContent = { Text(point.neighborhood) },
                modifier = Modifier.clickable { onPointClick(point) }
            )
            HorizontalDivider()
        }
    }
}

@Composable
fun ProfileScreen(viewModel: MainViewModel, onSignInClick: () -> Unit) {
    val userAccount by viewModel.userAccount.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (userAccount == null) {
            Button(onClick = onSignInClick) {
                Text(stringResource(R.string.login))
            }
        } else {
            Text("Eingeloggt als: ${userAccount?.email}")
            Button(onClick = { viewModel.setUserAccount(null) }) {
                Text(stringResource(R.string.logout))
            }
        }
    }
}

@Composable
fun AdminScreen() {
    Text("Admin Bereich")
}
