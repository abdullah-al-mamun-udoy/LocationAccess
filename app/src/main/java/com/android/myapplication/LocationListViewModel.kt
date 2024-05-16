package com.android.myapplication

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class LocationListViewModel :ViewModel() {

    val _locationList = mutableListOf<Location>()
    val locationList = _locationList

    fun addLocationList(location: Location) {
        _locationList.add(location)
        Log.d("location", "location size ${locationList.size}")
    }
}