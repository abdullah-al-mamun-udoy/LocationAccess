package com.android.myapplication

import android.location.Location
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class LocationListViewModel :ViewModel() {

    private val _locationList = mutableStateListOf<Location>()
    val locationList: List<Location> get() = _locationList

    fun addLocationList(location: Location) {
        _locationList.add(location)
        Log.d("location", "location size ${locationList.size}")
    }
}