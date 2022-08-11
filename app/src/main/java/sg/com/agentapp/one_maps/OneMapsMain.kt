package sg.com.agentapp.one_maps

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.recyclerview.widget.LinearLayoutManager
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import sg.com.agentapp.R
import sg.com.agentapp.api.api_clients.RetroOneMapAPIClient
import sg.com.agentapp.api.api_models.OneMapsSearchResp
import sg.com.agentapp.databinding.OnemapsActivityBinding
import sg.com.agentapp.global.AutoCompHelper
import sg.com.agentapp.global.BaseActivity
import sg.com.agentapp.global.GlobalVars
import sg.com.agentapp.global.MiscHelper

class OneMapsMain : BaseActivity() {
    //===== helpers
    private val miscHelper = MiscHelper()
    private val autoCompHelper = AutoCompHelper(300)

    //===== data binding
    private lateinit var binding: OnemapsActivityBinding
    private lateinit var mapView: MapView

    private lateinit var mapboxMap: MapboxMap

    val obsSearchEditTxt = ObservableField<String>()
    val obsNeedUpdateSearch = ObservableBoolean(true)
    val obsShowSearchRV = ObservableBoolean()
    val obsShowNoResRV = ObservableBoolean()
    val obsOneMapsAdapter = ObservableField(OneMapsAdapter())

    //===== marker
    private var marker: Marker? = null
    private val markerOptions = MarkerOptions()

    //===== retro
    private val api = RetroOneMapAPIClient.api

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // initialise instance first
        Mapbox.getInstance(this, GlobalVars.ONE_MAP_ACC_CODE)

        binding = DataBindingUtil.setContentView(this, R.layout.onemaps_activity)
        binding.data = this

        setupToolbar()

        initOneMaps(savedInstanceState)
        setupSearchBarObs()
        setupSearchResRV()
    }

    //===== setup funcs
    private fun initOneMaps(savedInstanceState: Bundle?) {
        // init map
        mapView = binding.map
        mapView.onCreate(savedInstanceState)

        // callback for map load complete
        mapView.getMapAsync { mapboxMap ->
            this.mapboxMap = mapboxMap
            mapboxMap.setStyle("https://maps-json.onemap.sg/Default.json") { style ->
                // initial settings
                mapboxMap.setMinZoomPreference(10.5)
                val latLngBounds = LatLngBounds.from(1.484749, 104.105685, 1.129380, 103.597098)
                mapboxMap.setLatLngBoundsForCameraTarget(latLngBounds)

                mapboxMap.uiSettings.isCompassEnabled = false
                mapboxMap.uiSettings.isRotateGesturesEnabled = false
                mapboxMap.uiSettings.isAttributionEnabled = false
                mapboxMap.uiSettings.isLogoEnabled = false

                // zoom to initial location
                zoomToPosition(1.358479, 103.815201, 11.2, true)

                // setup listeners
                mapboxAddOnDrag()
                mapboxAddOnClick()
            }
        }
    }

    //===== mapbox
    private fun mapboxAddOnDrag() {
        mapboxMap.addOnMoveListener(object : MapboxMap.OnMoveListener {
            override fun onMove(detector: MoveGestureDetector) {
                clearSearchUI()
            }

            override fun onMoveEnd(detector: MoveGestureDetector) {
            }

            override fun onMoveBegin(detector: MoveGestureDetector) {
            }

        })

        mapboxMap.addOnFlingListener {
            clearSearchUI()
        }
    }

    private fun mapboxAddOnClick() {
        mapboxMap.addOnMapClickListener {

        }
    }

    // setup searchview listener
    private fun setupSearchBarObs() {
        obsSearchEditTxt.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                // don't update anything if no need to update
                if (!obsNeedUpdateSearch.get()) {
                    return
                }

                // close all ui if empty
                if (obsSearchEditTxt.get()?.length == 0) {
                    clearSearchUI()
                }

                // search autocomplete delay
                autoCompHelper.delayAutoComp(obsSearchEditTxt.get() ?: "", postSearchQuery, null)
            }
        })
    }

    private fun setupSearchResRV() {
        binding.rvSearchRes.apply {
            // RecyclerView behavior
            layoutManager = LinearLayoutManager(context)

            adapter = obsOneMapsAdapter.get()
        }
    }


    //===== databinding funcs
    fun editTextOnClick(_v: View) {
        // set need update boolean
        obsNeedUpdateSearch.set(true)

        // re-show results rv if got results
        if (obsSearchEditTxt.get() != null && obsSearchEditTxt.get()!!.isNotEmpty()) {
            if (obsOneMapsAdapter.get()?.itemCount == 0) {
                obsShowNoResRV.set(true)

            } else {
                obsShowSearchRV.set(true)

            }
        }
    }

    fun clearSearchBtn(_v: View) {
        obsSearchEditTxt.set("")
    }

    fun doneBtnOnclick(_v: View) {
        // check if marker null first
        if (marker != null) { // got location
            val intent = Intent()

            val latLng = marker!!.position

            intent.putExtra("lat", latLng.latitude)
            intent.putExtra("lng", latLng.longitude)
            intent.putExtra("bldName", marker!!.snippet)

            setResult(RESULT_OK, intent)

            finish()
        }
    }

    //===== normal funcs
    // clear search ui
    fun clearSearchUI() {
        obsShowSearchRV.set(false)
        obsShowNoResRV.set(false)
    }

    fun createAndMoveToMarker(lat: Double?, lng: Double?, title: String?, address: String?) {
        if (lat != null && lng != null && title != null && address != null) {
            val point = LatLng(lat, lng)

            // setup marker options
            markerOptions.position(point)
                    .title(title)
                    .snippet(address)

            // remove and add marker
            if (marker != null) {
                marker!!.remove()
            }
            marker = mapboxMap.addMarker(markerOptions)

            // select and move to marker
            mapboxMap.selectMarker(marker!!)
            zoomToPosition(lat, lng, 13.0, true)
        }
    }

    private fun zoomToPosition(lat: Double, lng: Double, zoomLevel: Double, needAnimate: Boolean) {
        val zoomLocation = LatLng(lat, lng)
        val position = CameraPosition.Builder()
                .target(zoomLocation)
                .zoom(zoomLevel)// Sets the zoom
                .build() // Creates a CameraPosition from the builder

        if (needAnimate) {
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 2000)

        } else {
            mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(position))

        }
    }


    //===== retro funcs
    private val postSearchQuery = Runnable {
        api.search(obsSearchEditTxt.get() ?: "").enqueue(object :
                Callback<OneMapsSearchResp> {
            override fun onResponse(
                    call: Call<OneMapsSearchResp>,
                    response: Response<OneMapsSearchResp>
            ) {
                if (!response.isSuccessful) {
                    retroRespUnsuc(response)
                    return
                }
                retroRespSuccess(response)
            }

            override fun onFailure(call: Call<OneMapsSearchResp>, t: Throwable) {
                retroFailure(t)
            }
        })
    }

    //===== retro responses
    private fun retroRespSuccess(response: Response<OneMapsSearchResp>) {
        // show/hide search ui
        val resultsList = response.body()?.results

        obsShowNoResRV.set(resultsList?.size == 0)
        obsShowSearchRV.set(resultsList != null && resultsList.isNotEmpty())

        obsOneMapsAdapter.get()?.submitList(resultsList)
    }

    private fun retroRespUnsuc(response: Response<OneMapsSearchResp>) {
        miscHelper.retroLogUnsuc(
                GlobalVars.TAG1,
                "OneMapsMain retroRespSuccess",
                response
        )

        miscHelper.toastMsgInt(
                this,
                R.string.onresponse_unsuccessful,
                Toast.LENGTH_SHORT
        )
    }

    private fun retroFailure(t: Throwable) {
        miscHelper.retroLogFailure(GlobalVars.TAG1, "OneMapsMain retroFailure", t)

        miscHelper.toastMsgInt(this, R.string.onfailure, Toast.LENGTH_SHORT)
    }

    //===== oneMaps lifecycles (important!)
    @SuppressWarnings("MissingPermission")
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (outState != null) {
            mapView.onSaveInstanceState(outState)
        }
    }

    override fun onBackPressed() {
        // clear search if got
        if (obsShowSearchRV.get() || obsShowNoResRV.get()) {
            clearSearchUI()

        } else {
            super.onBackPressed()

        }
    }
}