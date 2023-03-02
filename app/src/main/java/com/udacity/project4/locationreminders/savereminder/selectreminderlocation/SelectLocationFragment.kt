package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

private const val REQUEST_LOCATION_PERMISSION = 1

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private var mLatLng: LatLng? = null
    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }
    private lateinit var locationCallback: LocationCallback

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                super.onLocationResult(locationResult)
                val lastLocation = locationResult.lastLocation
                if (mLatLng == null) {
                    mLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                    addCurrentLocationMarker(mLatLng!!)
                }
            }
        }

        binding.btnSave.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    private fun onLocationSelected() {
        if (mLatLng == null) {
            _viewModel.showSnackBarInt.value = R.string.err_select_location
            return
        }
        mLatLng?.let {
            _viewModel.navigationCommand.value =
                NavigationCommand.Back
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(p0: GoogleMap?) {
        p0?.let {
            map = it
            map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )
            enableMyLocation()
            setPoiClick(map)
            setOnClick(map)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            } else {
                showErrorDialog()
            }
        }
    }

    private fun showErrorDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Location Permission")
            .setMessage(context?.resources?.getString(R.string.permission_denied_explanation))
            .setPositiveButton("OK") { _, _ ->
                requestPermission()
            }
            .create()
            .show()
    }

    private fun requestPermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            REQUEST_LOCATION_PERMISSION
        )
    }

    private fun enableMyLocation() {
        if ((ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
                    )
        ) {
            map.isMyLocationEnabled = true
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    addCurrentLocationMarker(currentLatLng)
                }
            }
            val locationRequest = LocationRequest.create().apply {
                interval = 10000
                fastestInterval = 5000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            requestPermission()
        }
    }

    private fun addCurrentLocationMarker(currentLatLng: LatLng) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
        mLatLng = currentLatLng
        // add marker to current location
        val markerOptions = MarkerOptions()
            .position(currentLatLng)
            .title(getString(R.string.str_current_location))
        map.clear()
        val poiMarker = map.addMarker(markerOptions)
        poiMarker?.showInfoWindow()
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            mLatLng = poi.latLng
            map.clear()
            _viewModel.saveLatLng(poi.latLng, poi.name)
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker.showInfoWindow()
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(poi.latLng, 15f))
        }
    }

    private fun setOnClick(map: GoogleMap) {
        map.setOnMapClickListener { latLng ->
            mLatLng = latLng
            map.clear()
            _viewModel.saveLatLng(latLng, "Custom Location")
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
            )
            poiMarker.showInfoWindow()
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
    }

}
