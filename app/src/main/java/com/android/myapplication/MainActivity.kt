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
            coroutineScope.launch {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            false
        } else {
            // Permissions are already granted, proceed with getting location
            coroutineScope.launch {
                while (isActive){
                    val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                    Log.d("permission", "gpsLocation: $gpsLocation")
                    Log.d("permission", "networkLocation: $networkLocation")

                    currentLocation = when {
                        gpsLocation != null && networkLocation != null -> {
                            if (gpsLocation.accuracy > networkLocation.accuracy) gpsLocation else networkLocation
                        }
                        gpsLocation != null -> gpsLocation
                        else -> networkLocation
                    }
                    currentLocation?.let {
                        locationViewModel._locationList.clear()
                        locationViewModel.addLocationList(it)
                        Log.d("permission", "viewModel size ${locationViewModel.locationList.size}")
                    }
                    delay(5000)
                    Log.d("permission", "delay completed")
                }
            }

            true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
        var bool = mutableStateOf(false)
        var locationViewModel: LocationListViewModel =
            ViewModelProvider(this).get(LocationListViewModel::class.java)


        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val permissionGranted = permissions.all { it.value }
            if (permissionGranted) {
                Toast.makeText(this, "Location permissions granted", Toast.LENGTH_SHORT).show()
                isLocationPermissionGranted(
                    this,
                    lifecycleScope,
                    requestPermissionLauncher,
                    locationViewModel
                )
            } else {
                Toast.makeText(this, "Location permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
        if (isLocationPermissionGranted(
                this,
                lifecycleScope,
                requestPermissionLauncher,
                locationViewModel
            )
        ) {
            bool.value = true
        } else {
            bool.value = false
        }
        setContent {
            MyApplicationTheme {
                MyApp(bool,locationViewModel)
            }
        }
    }
}


@Composable
@SuppressLint("CoroutineCreationDuringComposition")
private fun isLocationPermissionGranted(): Boolean {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val locationManager = getLocationManager(context)
    var currentLocation by remember {
        mutableStateOf<Location?>(null)
    }
    val locationViewModel = LocationListViewModel()


    // Request permissions launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val permissionGranted = permissions.all { it.value }
        if (permissionGranted) {

            Log.d("permission", "isPermissionGranted ${permissionGranted}")

            var gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            var networkLocation =
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            Log.d("permission", " gpsLocation $gpsLocation")
            Log.d("permission", " networkLocation $networkLocation")


            if (gpsLocation != null) {
                if (networkLocation != null) {
                    currentLocation =
                        if (gpsLocation.accuracy > networkLocation.accuracy) gpsLocation else networkLocation
                } else {
                    currentLocation = gpsLocation
                }
            } else {
                currentLocation = networkLocation
            }
            currentLocation?.let {
                locationViewModel.addLocationList(it)
                Log.d("permission", "viewModel size in network ${locationViewModel._locationList.size}")
            }
            Toast.makeText(context, "Location permissions granted", Toast.LENGTH_SHORT).show()

        } else {
            Log.d("permission", "isLocationPermissionGranted ${permissionGranted}")
            Toast.makeText(context, "Location permissions Denied", Toast.LENGTH_SHORT).show()

        }


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

@SuppressLint("MutableCollectionMutableState", "MissingPermission")
@Composable
fun MyApp(bool: MutableState<Boolean>, locationViewModel: LocationListViewModel) {
   if(bool.value){
       Screen(viewModel = locationViewModel)
   }
}

@Composable
fun Screen(viewModel: LocationListViewModel) {


    Surface(modifier = Modifier.fillMaxSize()) {
        // Display the list of latitude and longitude values
        Column {
            if (viewModel.locationList.isEmpty()) {
                Text(text = "No location found")
            } else {
                viewModel.locationList.forEach { location ->
                    Text(
                        text = "Latitude: ${location.latitude}",
                        modifier = Modifier.padding(8.dp),
                        color = Color.Red
                    )
                    Text(
                        text = "Longitude: ${location.longitude}",
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


