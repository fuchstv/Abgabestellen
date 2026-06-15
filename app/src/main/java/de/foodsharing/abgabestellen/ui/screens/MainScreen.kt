package de.foodsharing.abgabestellen.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.foodsharing.abgabestellen.R
import de.foodsharing.abgabestellen.data.model.DropOffPoint
import de.foodsharing.abgabestellen.ui.utils.sendSuggestionEmail
import de.foodsharing.abgabestellen.ui.viewmodel.MainViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val navController = rememberNavController()
    val dropOffPoints by viewModel.dropOffPoints.collectAsState()
    val selectedPoint by viewModel.selectedPoint.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

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
    val items = listOf(
        stringResource(R.string.map_view),
        stringResource(R.string.list_view)
    )
    val icons = listOf(Icons.Filled.LocationOn, Icons.AutoMirrored.Filled.List)
    
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
        }

        if (showBottomSheet && selectedPoint != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                    viewModel.selectPoint(null)
                },
                sheetState = sheetState
            ) {
                PointDetailContent(selectedPoint!!)
            }
        }
    }
}

@Composable
fun PointDetailContent(point: DropOffPoint) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = point.name, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = point.anschrift, style = MaterialTheme.typography.bodyLarge)
        Text(text = "${point.plz} ${point.ortsteil}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(R.string.opening_hours), style = MaterialTheme.typography.titleMedium)
        Text(text = point.annahmezeiten)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = stringResource(R.string.accepted_items), style = MaterialTheme.typography.titleMedium)
        Text(text = point.akzeptiert)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { sendSuggestionEmail(context, point.name) },
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
            Marker(
                state = MarkerState(position = LatLng(point.latitude, point.longitude)),
                title = point.name,
                snippet = point.anschrift,
                onClick = {
                    onPointClick(point)
                    true
                }
            )
        }
    }
}

@Composable
fun ListScreen(points: List<DropOffPoint>, onPointClick: (DropOffPoint) -> Unit) {
    LazyColumn {
        items(points) { point ->
            ListItem(
                headlineContent = { Text(point.name) },
                supportingContent = { Text(point.anschrift) },
                overlineContent = { Text(point.ortsteil) },
                modifier = Modifier.clickable { onPointClick(point) }
            )
            HorizontalDivider()
        }
    }
}
