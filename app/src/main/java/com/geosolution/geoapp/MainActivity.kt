package com.geosolution.geoapp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.geosolution.geoapp.ui.theme.GeoAppTheme
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng

class MainActivity : ComponentActivity() {

    private lateinit var locationReceiver: BroadcastReceiver

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            ),
            0
        )

        // Initialize MapLibre
        Mapbox.getInstance(this)

        setContent {
            GeoAppTheme {
                var currentLocation by remember { mutableStateOf(LatLng(-12.046374, -77.042793)) }

                // Register BroadcastReceiver
                LaunchedEffect(Unit) {
                    locationReceiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context?, intent: Intent?) {
                            val lat = intent?.getDoubleExtra("latitude", -12.046374) ?: -12.046374
                            val long = intent?.getDoubleExtra("longitude", -77.042793) ?: -77.042793
                            Log.v("APP_LOCATION_BROADCAST", "lat: ${lat} - long: ${long}")
                            currentLocation = LatLng(lat, long)
                        }
                    }
                    val filter = IntentFilter("LOCATION_UPDATE")
                    registerReceiver(locationReceiver, filter, RECEIVER_NOT_EXPORTED)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        MapCompose(
                            modifier = Modifier.fillMaxSize(),
                            latLng = currentLocation
                        )
                    }

                    // Botones en la parte inferior
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        Button(
                            onClick = {
                                Intent(applicationContext, LocationService::class.java).apply {
                                    action = LocationService.ACTION_START
                                    Log.v("LOCATION_APP", action.toString())
                                    startService(this)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Start")
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Button(
                            onClick = {
                                Intent(applicationContext, LocationService::class.java).apply {
                                    action = LocationService.ACTION_STOP
                                    Log.v("LOCATION_APP", action.toString())
                                    startService(this)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Stop")
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(locationReceiver)
    }
}
