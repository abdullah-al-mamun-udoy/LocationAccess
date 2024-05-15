package com.android.myapplication

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
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


//@Composable
//@SuppressLint("CoroutineCreationDuringComposition")
//private fun isLocationPermissionGranted(): Boolean {
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//
//    // Request permissions launcher
//    val requestPermissionLauncher = rememberLauncherForActivityResult(
//        ActivityResultContracts.RequestMultiplePermissions()
//    ) { permissions ->
//        val permissionGranted = permissions.all { it.value }
//        val message = if (permissionGranted) {
//
//            Log.d("tag", "permission granted $permissionGranted" )
//        } else {
//            "Location permissions denied"
//            Log.d("tag", "permission denied $permissionGranted" )
//        }
//        // Show toast message
//        Toast.makeText(context, "Location permissions granted", Toast.LENGTH_SHORT).show()
//    }
//
//    return if (
//        ActivityCompat.checkSelfPermission(
//            context,
//            Manifest.permission.ACCESS_COARSE_LOCATION
//        ) != PackageManager.PERMISSION_GRANTED &&
//        ActivityCompat.checkSelfPermission(
//            context,
//            Manifest.permission.ACCESS_FINE_LOCATION
//        ) != PackageManager.PERMISSION_GRANTED
//    ) {
//        // Request permissions if not granted
//        scope.launch {
//            requestPermissionLauncher.launch(
//                arrayOf(
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                )
//            )
//        }
//        false
//    } else {
//        true
//    }
//}

//@Composable
//private fun isLocationPermissionGranted(): Boolean {
//
//    val context = LocalContext.current
//    return if (
//        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
//        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
//        != PackageManager.PERMISSION_GRANTED
//    ) {
//        ActivityCompat.requestPermissions(
//            context as Activity, arrayOf(
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ), 100
//        )
//        false
//    } else {
//        true
//
//    }
//}


fun getLocationManager(context: Context): LocationManager {
    return context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
}

// Usage

@Composable
fun MyApp() {
    val context = LocalContext.current
    val locationManager = getLocationManager(context)

    // State variables for location and permissions
    var locationList by remember { mutableStateOf(mutableListOf<Location>()) }
    var isPermissionGranted by remember { mutableStateOf(false) }

    // Check and request location permissions
    isPermissionGranted = isLocationPermissionGranted()

    // LaunchedEffect to start location updates coroutine when permission is granted
    LaunchedEffect(isPermissionGranted) {
        if (isPermissionGranted) {
            startLocationUpdates(locationManager, locationList,context)
        }
    }

    // UI to display location list
    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            if(locationList.isEmpty()){
                Text(text = "no item ${locationList.size}")
            }
            locationList.forEach { location ->
                Text(
                    text = "Latitude: ${location.latitude ?: 0.0}",
                    modifier = Modifier.padding(8.dp),
                    color = Color.Red
                )
                Text(
                    text = "Longitude: ${location.longitude ?: 0.0}",
                    modifier = Modifier.padding(8.dp),
                    color = Color.Red
                )
            }
        }
    }
}

// Function to start the coroutine for location updates
fun startLocationUpdates(
    locationManager: LocationManager,
    locationList: MutableList<Location>,
    context: Context
) {
    val coroutineScope = CoroutineScope(Dispatchers.Default) // or Dispatchers.Main, depending on where it's used
    coroutineScope.launch {
        while (isActive) {
            delay(5000) // Wait for 5 seconds
            val lastKnownLocation = if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permissions not granted, handle this scenario
                // For example, you can request permissions here
                // or perform some other action
                return@launch
            } else {
                locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }

            if (lastKnownLocation != null) {
                locationList.add(lastKnownLocation)
            }



        }
    }
}
@Composable
fun isLocationPermissionGranted(): Boolean {
    val context = LocalContext.current

    val permissionGranted = remember { mutableStateOf(false) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allPermissionsGranted = permissions.all { it.value }
        permissionGranted.value = allPermissionsGranted
        if (allPermissionsGranted) {
            Toast.makeText(context, "Location permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Location permissions denied", Toast.LENGTH_SHORT).show()
        }
    }

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
        LaunchedEffect(Unit) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    return permissionGranted.value
}


@Composable
fun MyApplicationTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}


