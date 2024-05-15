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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            MyApplicationTheme {
                MyApp()
            }
        }
    }
}


@Composable
@SuppressLint("CoroutineCreationDuringComposition")
private fun isLocationPermissionGranted(): Boolean {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Request permissions launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val permissionGranted = permissions.all { it.value }
        if (permissionGranted) {
            Log.d("tag", "isLocationPermissionGranted ${permissionGranted}")

        } else {
            "Location permissions denied"
            Log.d("tag", "isLocationPermissionGranted 1${permissionGranted}")

        }
        // Show toast message
        Toast.makeText(context, "Location permissions granted", Toast.LENGTH_SHORT).show()
    }

    return if (
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
        scope.launch {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
        false
    } else {
        true
    }
}



fun getLocationManager(context: Context): LocationManager {
    return context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
}

// Usage

@SuppressLint("MutableCollectionMutableState", "MissingPermission")
@Composable
fun MyApp() {
    val context = LocalContext.current

    val locationManager = getLocationManager(context)
    var locationList by remember { mutableStateOf(mutableListOf<Location>()) }
    var locationByGps by remember { mutableStateOf<Location?>(null) }
    var locationByNetwork by remember { mutableStateOf<Location?>(null) }
    var currentLocation by remember {
        mutableStateOf<Location?>(null)
    }
    var isPermissionGranted by remember { mutableStateOf(false) }

    isPermissionGranted = isLocationPermissionGranted()


    Log.d("tag", "isPermissionGranted $isPermissionGranted")

    // Get the last known location from GPS provider
    if (isPermissionGranted) {

        var gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        // Get the last known location from network provider
        var networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        Log.d("tag", "gpsLocation $gpsLocation")
        Log.d("tag", "networkLocation $networkLocation")

        LaunchedEffect(key1 = Unit) {


            while (true) {
                Log.d("tag", "before delay")
                delay(5000) // Wait for 5 seconds
                Log.d("tag", "after delay")

                // Ensure location permissions are granted

                val lastKnownLocationByGps =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                lastKnownLocationByGps?.let {
                    gpsLocation = lastKnownLocationByGps
                }

                val lastKnownLocationByNetwork =
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                lastKnownLocationByNetwork?.let {
                    networkLocation = lastKnownLocationByNetwork
                }

                Log.d("tag", "locationByGps $gpsLocation")
                Log.d("tag", "locationByNetwork $networkLocation")

                // Determine which location is more accurate
                gpsLocation?.let { gpsLocation ->
                    networkLocation?.let { networkLocation ->
                        currentLocation =
                            if (gpsLocation.accuracy > networkLocation.accuracy) gpsLocation else networkLocation
                    } ?: run {
                        currentLocation = gpsLocation
                    }
                } ?: run {
                    locationByNetwork?.let { networkLocation ->
                        currentLocation = networkLocation
                    }
                }

                // Add the current location to the list
                currentLocation?.let {
                    locationList = (locationList + it).toMutableList() // Update list safely
                }
            }
        }

        Log.d("tag", locationList.size.toString())

        // Your UI code here
        Surface(modifier = Modifier.fillMaxSize()) {
            // Display the list of latitude and longitude values
            Column {
                if(locationList.isEmpty()){
                    Text(text = "location is empty ${locationList.size}")
                }
                locationList.forEach { location ->
                    Text(
                        text = "Latitude: ${location.latitude ?: 30.33}",
                        modifier = Modifier.padding(8.dp),
                        color = Color.Red
                    )
                    Text(
                        text = "Longitude: ${location.longitude ?: -97.74}",
                        modifier = Modifier.padding(8.dp),
                        color = Color.Red
                    )
                }
            }
        }
    }


}


@Composable
fun MyApplicationTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}