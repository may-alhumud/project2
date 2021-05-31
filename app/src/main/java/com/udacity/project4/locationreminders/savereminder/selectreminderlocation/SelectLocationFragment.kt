package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.app.Activity
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.mahmoud.todoapp.util.LocationManager
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.LocationHelper
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.BuildConfig
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {
    val TAG = "SelectLocationFragment"

    //    Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var locationHelper: LocationHelper
    private lateinit var googleMap: GoogleMap
    private lateinit var marker: Marker
    private lateinit var selectedPointOfInterest: PointOfInterest
    var lat = 0.0
    var lng = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        val mapView = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment


        mapView.getMapAsync(this)

//        TODO: add the map setup implementation
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user select
//        TODO: call this function after the user confirms on the selected location

        binding.saveButton.setOnClickListener {
            if (this::selectedPointOfInterest.isInitialized) {
                _viewModel.selectedPOI.value = selectedPointOfInterest
                _viewModel.reminderSelectedLocationStr.value = selectedPointOfInterest.name
                _viewModel.latitude.value = selectedPointOfInterest.latLng.latitude
                _viewModel.longitude.value = selectedPointOfInterest.latLng.longitude
            }
            findNavController().popBackStack()

         /*   val args = Bundle()
            args.putFloat("lat", lat.toFloat())
            args.putFloat("lng", lng.toFloat())
            args.putString("place", getAddress(lat, lng))

            val navOptions =
                NavOptions.Builder().setPopUpTo(R.id.saveReminderFragment, true).build()
            binding.root?.findNavController()?.navigate(R.id.saveReminderFragment, args, navOptions)*/
        }

        return binding.root
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.

        R.id.normal_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(map: GoogleMap?) {
        googleMap = map!!

        clickAddress(googleMap)
        mapClick(googleMap)
        locationHelper = LocationHelper(requireActivity(), object : LocationManager {

            override fun onLocationChanged(location: Location?) {

            }

            override fun getLastKnownLocation(location: Location?) {
                Log.e(TAG, "getLastKnownLocation latitude: ${location?.latitude}")
                Log.e(TAG, "getLastKnownLocation longitude: ${location?.longitude}")
                if (location != null) {
                    lat = location.latitude
                    lng = location.longitude
                    map.uiSettings?.isZoomControlsEnabled = true
                    //    map?.isMyLocationEnabled = true
                    marker = map.addMarker(MarkerOptions().position(LatLng(lat, lng)));
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 18f))
                    marker.showInfoWindow()

                }
            }

        })

        if (LocationHelper.checkMapServices(requireActivity())) {
            if (locationHelper.checkLocationPermissions()) {
                locationHelper.startLocationUpdates()

            }
        }

    }

    private fun getAddress(lat: Double, lng: Double): String? {
        val geoCoder = Geocoder(requireContext(), Locale.getDefault())
        var addresses: List<Address>? = null

        var address: String? = null
        try {
            addresses = geoCoder.getFromLocation(lat, lng, 1)
            address = addresses[0].getAddressLine(0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return address
    }


    private fun clickAddress(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            if (this::marker.isInitialized) {
                marker.remove()
            }

            marker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )

            selectedPointOfInterest = poi

            marker.showInfoWindow()

        }
    }

     private fun mapClick(map: GoogleMap) {
      map.setOnMapLongClickListener { latLng ->
          if (this::marker.isInitialized) {
              marker.remove()
          }
          lat = latLng.latitude
          lng = latLng.longitude


          val snippet = String.format(
              Locale.getDefault(),
              getString(R.string.lat_long_snippet), lat, lat
          )
          selectedPointOfInterest = PointOfInterest(latLng, snippet, snippet)

          CoroutineScope(Dispatchers.Main).launch {
              val title = getAddress(lat, lat)?: getString(R.string.reminder_location)

              marker = map.addMarker(
                  MarkerOptions()
                      .position(latLng)
                      .title(title)
                      .snippet(snippet)
                      .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
              )

              marker.showInfoWindow()
          }



      }
  }



}
