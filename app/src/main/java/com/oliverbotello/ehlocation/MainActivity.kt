package com.oliverbotello.ehlocation

import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.doOnTextChanged
import com.huawei.hms.location.*
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.MapView
import com.huawei.hms.maps.MapsInitializer
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.site.api.SearchResultListener
import com.huawei.hms.site.api.SearchService
import com.huawei.hms.site.api.SearchServiceFactory
import com.huawei.hms.site.api.model.*
import com.oliverbotello.ehlocation.utils.showMessage
import com.oliverbotello.ehlocation.utils.showToast
import java.lang.Exception
import java.net.URLEncoder


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val MAPVIEW_BUNDLE_KEY: String = "MapViewBundleKey"

    private lateinit var mLocationCallback: LocationCallback
    private var catchLocation: Boolean = false
    private lateinit var searchService: SearchService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchService = SearchServiceFactory.create(this, getApiKey())
        initView(savedInstanceState)
    }

    private fun getApiKey(): String {
        var apiKey = "CwEAAAAA/waiquscvsSvqfvqC3UQ+uj7Qiz9/VF5JfrNkLDD25qKpOimlpZ3tBji21CVHoV4jjqEJsS3jlmAGZnUTL5CvQ6SlUE="

        return try {
            URLEncoder.encode(apiKey, "utf-8")
        } catch (e: Exception) {
            apiKey
        }
    }

    private fun initView(savedInstanceState: Bundle?) {
        initMapa(savedInstanceState)
        findViewById<Button>(R.id.btnStart).setOnClickListener {
            if (!catchLocation) {
                catchLocation = true
                (it as Button).text = "Detener"
                initLocation()
            }

            else {
                catchLocation = false
                (it as Button).text = "Iniciar"
                stopLocation()
            }
        }
        val edtTxt = findViewById<AppCompatEditText>(R.id.edtTxtSearch)
        edtTxt.doOnTextChanged {
            text, start, before, count ->
                showMessage(text.toString())
                onSearch(text.toString())
        }
    }

    private fun initMapa(savedInstanceState: Bundle?) {
        var mapViewBundle: Bundle? = null

        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }

        showMessage("Iniciando mapa")
        MapsInitializer.setApiKey("CwEAAAAA/waiquscvsSvqfvqC3UQ+uj7Qiz9/VF5JfrNkLDD25qKpOimlpZ3tBji21CVHoV4jjqEJsS3jlmAGZnUTL5CvQ6SlUE=")
        val mapa = findViewById<MapView>(R.id.mpVwMapa)
        mapa.onCreate(mapViewBundle)
        mapa.getMapAsync(this)
        showMessage("Finalizo mapa")
    }

    private fun initLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 1000
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val message = "Location: ${locationResult.lastLocation.latitude} - ${locationResult.lastLocation.longitude}"
                showToast(applicationContext, message)
                showMessage(message)
            }
        }

        fusedLocationProviderClient
            .requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper())
            .addOnSuccessListener {
                showMessage("Todo chido")
            }
            .addOnFailureListener {
                showMessage("Nada chido")
            }
    }

    private fun stopLocation() {
        fusedLocationProviderClient.removeLocationUpdates(mLocationCallback)
    }

    private fun onSearch(search: String) {
        val request = NearbySearchRequest()
        val location = Coordinate(19.45599408, -99.21111235)
        request.query = search
        request.location = location
        request.radius = 1000
        request.hwPoiType = HwLocationType.AMUSEMENT_PARK
//        request.countryCode = "MX"
        request.language = "es"
        request.pageIndex = 1
        request.pageSize = 5
//        request.countries = listOf("mx")
        val resultListener: SearchResultListener<NearbySearchResponse> = object : SearchResultListener<NearbySearchResponse> {
            // Return search results upon a successful search.
            override fun onSearchResult(results: NearbySearchResponse?) {
                if (results == null || results.getTotalCount() <= 0) {
                    showMessage("No hay sitios")
                    return
                }
                val sites: List<Site>? = results.getSites()
                if (sites == null || sites.isEmpty()) {
                    showMessage("No hay sitios 2")
                    return
                }
                for (site in sites) {
                    showMessage(site.getName())
                    showToast(applicationContext, site.getName())
                }
            }
            // Return the result code and description upon a search exception.
            override fun onSearchError(status: SearchStatus) {
                showMessage("${status.errorCode}: ${status.errorMessage}")
            }
        }
        searchService.nearbySearch(request, resultListener)
    }

    override fun onMapReady(map: HuaweiMap) {
        showMessage("Mapa listo")
        map.isMyLocationEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true
        map.uiSettings.isMapToolbarEnabled = true
    }
}