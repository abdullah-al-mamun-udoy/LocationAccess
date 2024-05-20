package com.android.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private fun isLocationPermissionGranted(
        context: Context,
        coroutineScope: CoroutineScope,
        requestPermissionLauncher: ActivityResultLauncher<Array<String>>,
        locationViewModel: LocationListViewModel
    ): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var currentLocation: Location? = null

        if (
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permissions if not granted
            coroutineScope.launch {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            return false
        } else {
            // Permissions are already granted, proceed with getting location
            fetchLocation(coroutineScope, locationManager, locationViewModel)
            return true
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocation(
        coroutineScope: CoroutineScope,
        locationManager: LocationManager,
        locationViewModel: LocationListViewModel
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            while (isActive) {
                val gpsLocation =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val networkLocation =
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                Log.d("permission", "gpsLocation: $gpsLocation")
                Log.d("permission", "networkLocation: $networkLocation")

                val currentLocation = when {
                    gpsLocation != null && networkLocation != null -> {
                        if (gpsLocation.accuracy > networkLocation.accuracy) gpsLocation else networkLocation
                    }

                    gpsLocation != null -> gpsLocation
                    else -> networkLocation
                }

                currentLocation?.let {
                    locationViewModel.addLocationList(it)
                    Log.d("permission", "viewModel size ${locationViewModel.locationList.size}")
                }

                delay(5000)
                Log.d("permission", "delay completed")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
        val bool = mutableStateOf(false)
        val locationViewModel: LocationListViewModel =
            ViewModelProvider(this).get(LocationListViewModel::class.java)

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val permissionGranted = permissions.all { it.value }
            if (permissionGranted) {
                Toast.makeText(this, "Location permissions granted", Toast.LENGTH_SHORT).show()
                fetchLocation(lifecycleScope, getSystemService(Context.LOCATION_SERVICE) as LocationManager, locationViewModel)
                bool.value = true
            } else {
                Toast.makeText(this, "Location permissions denied", Toast.LENGTH_SHORT).show()
            }
        }

        bool.value = isLocationPermissionGranted(
            this,
            lifecycleScope,
            requestPermissionLauncher,
            locationViewModel
        )

        setContent {
            MyApplicationTheme {
                MyApp(bool, locationViewModel)
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun MyApp(bool: MutableState<Boolean>, locationViewModel: LocationListViewModel) {
    if (bool.value) {
        Screen(viewModel = locationViewModel)
    }
}

@Composable
fun Screen(viewModel: LocationListViewModel) {
    val locationList = viewModel.locationList

    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            if (locationList.isEmpty()) {
                Text(text = "No location found")
            } else {
                LazyColumn {
                    itemsIndexed(locationList) { index, location ->
                        Pagee(location.latitude, location.longitude)
                    }
                }
            }
        }
    }
}

@Composable
fun Pagee(latitude: Double, longitude: Double) {
    Text(
        text = "Latitude: $latitude",
        modifier = Modifier.padding(8.dp),
        color = Color.Red
    )
    Text(
        text = "Longitude: $longitude",
        modifier = Modifier.padding(8.dp),
        color = Color.Red
    )
}

@Composable
fun MyApplicationTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}



