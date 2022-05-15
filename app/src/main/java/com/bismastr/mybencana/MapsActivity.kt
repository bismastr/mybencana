package com.bismastr.mybencana

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bismastr.mybencana.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnCameraIdleListener {

    private lateinit var binding: ActivityMapsBinding

    private var map: GoogleMap? = null

    private lateinit var mHashMarker: HashMap<Marker?, String>

    // The entry point to the Fused Location Provider.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    private var locationPermissionGranted = false

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private var lastKnownLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        binding.btnSignout.setOnClickListener {
            signOut()
        }

        binding.btnLaporan.setOnClickListener {
            manualGeolocation()
        }

        mHashMarker = HashMap()


    }

    override fun onMapReady(map: GoogleMap) {
        val success = map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json))

        if (!success) {
            Log.e(TAG, "Style parsing failed.");
        }
        this.map = map

        // Prompt the user for permission.
        getLocationPermission()
        // [END_EXCLUDE]

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

        // Get the current location of the device and set the position of the map.
        getDeviceLocation()

        readFireStoreData()

        map.setOnMarkerClickListener {
            val pos = mHashMarker[it]
            Log.d("MARKER", pos + "")
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("EXTRA_MARKER_ID", pos)
            startActivity(intent)
            false
        }
    }

    // [START maps_current_place_get_device_location]
    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            map?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    ), DEFAULT_ZOOM.toFloat()
                                )
                            )
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        map?.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat())
                        )
                        map?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }
    // [END maps_current_place_get_device_location]

    // [START maps_current_place_location_permission]
    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }
    // [END maps_current_place_location_permission]

    // [START maps_current_place_on_request_permissions_result]
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
        updateLocationUI()
    }
    // [END maps_current_place_on_request_permissions_result]

    // [START maps_current_place_update_location_ui]
    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        if (map == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                getDeviceLocation()
                map?.isMyLocationEnabled = true
                map?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                map?.isMyLocationEnabled = false
                map?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }
    // [END maps_current_place_update_location_ui]

    companion object {
        private val TAG = MapsActivity::class.java.simpleName
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun readFireStoreData() {
        val db = FirebaseFirestore.getInstance()
        db.collection("laporan")
            .get()
            .addOnCompleteListener {
                for (document in it.result) {
                    val marker: Marker? = map?.addMarker(
                        MarkerOptions()
                            .position(
                                LatLng(
                                    document.data.getValue("latitude").toString().toDouble(),
                                    document.data.getValue("longitude").toString().toDouble()
                                )
                            )
                            .icon(
                                iconSelector(document.data.getValue("tipeBencana").toString())
                            )
                    )
                    Log.d("FIRESTORE", "${document.id} => ${document.data.getValue("latitude")}")

                    mHashMarker[marker] = document.id
                }
            }
    }

    private fun iconSelector(type: String): BitmapDescriptor? {
        Log.d("TYPE", type)
       when(type){
           "Banjir" -> return bitmapDescriptorFromVector(this, R.drawable.banjir_logo)
           "Gempa Bumi" -> return bitmapDescriptorFromVector(this, R.drawable.gempa_logo)
           "Angin Puting Beliung" -> return bitmapDescriptorFromVector(this, R.drawable.angin_logo)
           "Longsor" -> return bitmapDescriptorFromVector(this, R.drawable.longsor_logo)
           "Kebakaran Hutan" -> return bitmapDescriptorFromVector(this, R.drawable.kahutla_logo)
           "Gelombang Pasang" -> return bitmapDescriptorFromVector(this, R.drawable.gelombang_logo)
           "Kekeringan" -> return bitmapDescriptorFromVector(this, R.drawable.kekeringan_logo)
           else -> return bitmapDescriptorFromVector(this, R.drawable.ic_marker)
       }
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, 100, 100)
            val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    private fun manualGeolocation() {
        binding.imgMarker.visibility = View.VISIBLE
        binding.btnLaporan.visibility = View.GONE
        binding.btnSetlocation.visibility = View.VISIBLE
        binding.btnSignout.visibility = View.GONE

        if (lastKnownLocation != null) {
            map?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        lastKnownLocation!!.latitude,
                        lastKnownLocation!!.longitude
                    ), DEFAULT_ZOOM.toFloat()
                )
            )
        }

        map?.setOnCameraIdleListener(this@MapsActivity)
    }

    override fun onCameraIdle() {
        val center: LatLng? = map?.cameraPosition?.target
        Log.d("CENTER", "${center?.longitude} => ${center?.latitude}")

        binding.btnSetlocation.setOnClickListener {
            val intent = Intent(this, LaporActivity::class.java)
            intent.putExtra("EXTRA_CURRENT_LONG", center!!.longitude.toString())
            intent.putExtra("EXTRA_CURRENT_LAT", center.latitude.toString())
            startActivity(intent)
        }
    }

}