package smaillns.smail.dzair.fragment


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.Toast
import smaillns.smail.dzair.CityGuideConfig

import smaillns.smail.dzair.R
import smaillns.smail.dzair.activity.MapActivity
import smaillns.smail.dzair.activity.SpotDetailsActivity
import smaillns.smail.dzair.database.LocalStorage.RoomService
import smaillns.smail.dzair.database.RetrofitService
import smaillns.smail.dzair.entity.Category
import smaillns.smail.dzair.entity.Spot
import smaillns.smail.dzair.entity.TestDB
import smaillns.smail.dzair.graphics.BitmapScaler
import smaillns.smail.dzair.listener.AnimateImageLoadingListener
import smaillns.smail.dzair.utility.*
import smaillns.smail.dzair.view.ViewState
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.maps.*
import com.google.maps.android.clustering.ClusterManager
import java.util.ArrayList
import java.util.HashMap
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import smaillns.smail.dzair.utility.*
import java.io.IOException
import java.sql.SQLException


class MapFragment() : TaskFragment(), OnMapReadyCallback {


    private val MAP_ZOOM = 14

    private var mViewState: ViewState? = null
    private var mRootView: View? = null
    private var mMapView: MapView? = null
    //private val mDatabaseCallManager = DatabaseCallManager()

    private val mSpotList = ArrayList<Spot>()
    private var mClusterManager: ClusterManager<Spot>? = null
    private val mBitmapDescriptorMap = HashMap<Int, BitmapDescriptor>()
    private var mSpotId = 1
    private var mSpotLatitude = 0.0
    private var mSpotLongitude = 0.0

    lateinit var mMap: GoogleMap

    private var mImageLoader = ImageLoader.getInstance()
    lateinit var mDisplayImageOptions: DisplayImageOptions
    lateinit var mImageLoadingListener: ImageLoadingListener


    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
        retainInstance = true

        // handle intent extras
        val extras = activity!!.getIntent().extras
        if (extras != null) {
            handleExtras(extras)
        }



        /**/
        mImageLoader.init(ImageLoaderConfiguration.createDefault(context))

        // image caching options
        mDisplayImageOptions = DisplayImageOptions.Builder()
                .showImageOnLoading(android.R.color.transparent)
                .showImageForEmptyUri(R.drawable.ic_location_on_black_24dp)
                .showImageOnFail(R.drawable.ic_location_on_black_24dp)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .displayer(SimpleBitmapDisplayer())
                .build()
        mImageLoadingListener = AnimateImageLoadingListener()
        /**/
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_map, container, false)
        initMap()
        mMapView = mRootView!!.findViewById<View>(R.id.fragment_map_mapview) as MapView
        mMapView!!.onCreate(savedInstanceState)

        return mRootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // reference
        val map = (mRootView!!.findViewById<View>(R.id.fragment_map_mapview) as MapView).getMapAsync(this)
        // setup map
//        setupMap()
//        setupClusterManager()
    }


    override fun onStart() {
        super.onStart()
    }


    override fun onResume() {
        super.onResume()

        // map
        if (mMapView != null) mMapView!!.onResume()
    }


    override fun onPause() {
        super.onPause()

        // map
        if (mMapView != null) mMapView!!.onPause()
    }


    override fun onStop() {
        super.onStop()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mRootView = null
    }


    override fun onDestroy() {
        super.onDestroy()

        // map
        if (mMapView != null) mMapView!!.onDestroy()

        // cancel async tasks
//        mDatabaseCallManager.cancelAllTasks()
    }


    override fun onDetach() {
        super.onDetach()
    }


    override fun onLowMemory() {
        super.onLowMemory()

        // map
        if (mMapView != null) mMapView!!.onLowMemory()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        // save current instance state
        super.onSaveInstanceState(outState)
        userVisibleHint = true

        // map
        if (mMapView != null) mMapView!!.onSaveInstanceState(outState)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // action bar menu
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_map, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // action bar menu behaviour
        when (item.itemId) {
            R.id.menu_layers_normal -> {
                setMapType(GoogleMap.MAP_TYPE_NORMAL)
                return true
            }

            R.id.menu_layers_satellite -> {
                setMapType(GoogleMap.MAP_TYPE_SATELLITE)
                return true
            }

            R.id.menu_layers_hybrid -> {
                setMapType(GoogleMap.MAP_TYPE_HYBRID)
                return true
            }

//            R.id.menu_layers_terrain -> {
//                setMapType(GoogleMap.MAP_TYPE_TERRAIN)
//                return true
//            }

            else -> return super.onOptionsItemSelected(item)
        }
    }
    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        mMap = map

        setupMap()
        setupClusterManager()

        //loadData()

        //renderView()

        // load and show data
        if (mViewState == null || mViewState === ViewState.OFFLINE) {
            loadData()
        } else if (mViewState === ViewState.CONTENT) {
            if (mSpotList != null) renderView()
            showContent()
        } else if (mViewState === ViewState.PROGRESS) {
            showProgress()
        } else if (mViewState === ViewState.EMPTY) {
            showEmpty()
        }

    }

    @SuppressLint("MissingPermission")
    private fun setupMap(){
        // reference
        // val map = (mRootView!!.findViewById<View>(R.id.fragment_map_mapview) as MapView).getMapAsync(this)
        // settings
        if (mMap != null) {
            val preferences = Preferences(activity)

            mMap!!.setMapType(preferences.getMapType())
           try{
               mMap!!.isMyLocationEnabled = true
           }catch(e: Exception){
               Toast.makeText(getActivity()!!, getString(R.string.service_location_not_granted), Toast.LENGTH_LONG).show()
           }

            val settings = mMap!!.getUiSettings()
            settings.setAllGesturesEnabled(true)
            settings.setMyLocationButtonEnabled(true)
            settings.setZoomControlsEnabled(true)

            var latLng: LatLng? = null
            if (mSpotLatitude == 0.0 && mSpotLongitude == 0.0) {
                val locationManager = activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val location : Location?
                try{
                    location = getLastKnownLocation(locationManager)
                    if (location != null) latLng = LatLng(location!!.getLatitude(), location!!.getLongitude())
                }catch(e: Exception){
                    Toast.makeText(getActivity()!!, getString(R.string.service_location_not_granted), Toast.LENGTH_LONG).show()
                    latLng = LatLng(36.753766, 3.058784)  //the coordinates of Algiers|default
                }
            } else {
                latLng = LatLng(mSpotLatitude, mSpotLongitude)
            }

            if (latLng != null) {
                val cameraPosition = CameraPosition.Builder()
                        .target(latLng)
                        .zoom(MAP_ZOOM.toFloat())
                        .bearing(0f)
                        .tilt(0f)
                        .build()
                mMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }
        }
    }

    fun setupClusterManager(){
        //setupClusterManager()
        // clustering
        if (mMap != null) {
            mClusterManager = ClusterManager<Spot>(activity, mMap)
            mClusterManager!!.setRenderer(object : DefaultClusterRenderer<Spot>(activity, mMap, mClusterManager) {
                override fun onBeforeClusterItemRendered(spot: Spot?, markerOptions: MarkerOptions?) {
                    val category = spot!!.category
                    val bitmapDescriptor = loadBitmapDescriptor(category!!)

                    markerOptions!!.title(spot!!.name)
                    mMap.setInfoWindowAdapter(MapInfoWindowAdapter(context, spot, mImageLoader, mDisplayImageOptions, mImageLoadingListener))
                    markerOptions!!.icon(bitmapDescriptor)

                    super.onBeforeClusterItemRendered(spot, markerOptions)
                }
            })
            mClusterManager!!.setOnClusterItemInfoWindowClickListener(ClusterManager.OnClusterItemInfoWindowClickListener<Spot> {
                spot -> startSpotDetailActivity(spot)
            })
            mMap!!.setOnCameraChangeListener(mClusterManager)
            mMap!!.setOnInfoWindowClickListener(mClusterManager)
        }

    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(locationManager: LocationManager): Location? {
        val locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        val locationGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        var timeNet = 0L
        var timeGps = 0L

        if (locationNet != null) {
            timeNet = locationNet.time
        }

        if (locationGps != null) {
            timeGps = locationGps.time
        }

        return if (timeNet > timeGps)
            locationNet
        else
            locationGps
    }


    private fun loadBitmapDescriptor(category: Category): BitmapDescriptor {
        var bitmapDescriptor: BitmapDescriptor? = mBitmapDescriptorMap[category.id]
        if (bitmapDescriptor == null) {
            try {
                //CategoryDAO.refresh(category)
                bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(loadMarkerBitmap(category.marker))
            } catch (e: SQLException) {
                bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(getColorAccentHue())
            } catch (e: IOException) {
                bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(getColorAccentHue())
            } catch (e: IllegalArgumentException) {
                bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(getColorAccentHue())
            }

            mBitmapDescriptorMap[category.id] = bitmapDescriptor!!
        }
        return bitmapDescriptor
    }

    @Throws(IOException::class, IllegalArgumentException::class)
    private fun loadMarkerBitmap(path: String): Bitmap {
        val size = activity!!.getResources().getDimensionPixelSize(R.dimen.fragment_map_marker_size)
        val inputStream = activity!!.getAssets().open(path)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        val bitmap = BitmapScaler.scaleToFill(originalBitmap, size, size)
        if (originalBitmap != bitmap) originalBitmap.recycle()
        inputStream.close()
        return bitmap
    }


    private fun getColorAccentHue(): Float {
        // get accent color
        val typedValue = TypedValue()
        activity!!.getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true)
        val markerColor = typedValue.data

        // get hue
        val hsv = FloatArray(3)
        Color.colorToHSV(markerColor, hsv)
        return hsv[0]
    }


    private fun startSpotDetailActivity(spot: Spot) {
        val intent = SpotDetailsActivity.newIntent(activity!!,spot)
        startActivity(intent)
    }


    private fun renderView() {
        // reference
//        val map = (mRootView!!.findViewById<View>(R.id.fragment_map_mapview) as MapView).getMap()
        val adView = mRootView!!.findViewById<View>(R.id.fragment_map_adview) as AdView

        // map
        if (mMap != null) {
            // add pois
            mMap!!.clear()
            mClusterManager!!.clearItems()
            for (spot in mSpotList) {
                mClusterManager!!.addItem(spot)
            }
            mClusterManager!!.cluster()
        }

        // admob
        if (CityGuideConfig.ADMOB_MAP_BANNER && NetworkManager.isOnline(activity)) {
            val adRequest = AdRequest.Builder()
                    .build()
            //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
            //.addTestDevice(getString(R.string.admob_test_device_id))
            adView.loadAd(adRequest)
            adView.setVisibility(View.VISIBLE)
        } else {
            adView.setVisibility(View.GONE)
        }
    }




    private fun loadData() {
        showProgress()

        val list = RoomService.appDataBase.getSpotDao().getSpots()
        if(1==1){

            for (spot in list){
                if (spot.category == null)
                        fillCateories(spot)
                mSpotList.add(spot)

                }

            }




        if (mSpotList.size == 0) {
                loadDataFromServer()
        }
        else {
            showContent()
            renderView()

            val call = RetrofitService.endPoint.getNbrSpot()
            call.enqueue(object : Callback<List<TestDB>> {
                override fun onFailure(call: Call<List<TestDB>>?, t: Throwable?) {
                    //progressBar.visibility = View.GONE
                    Logcat.d("error server")
//                    Toast.makeText(getActivity()!!, "error server", Toast.LENGTH_SHORT).show()
                }
                override fun onResponse(call: Call<List<TestDB>>?, response: Response<List<TestDB>>?) {
                    //progressBar.visibility = View.GONE
                    if (response?.isSuccessful!!) {
                        val nbr = response.body()!![0].nbr
                        if (nbr != mSpotList.size) {
                            mSpotList.clear()
                            RoomService.appDataBase.getSpotDao().nukeTable()
                            showProgress()
                            loadDataFromServer()
                        }
                    } else {
                        Logcat.d(""+response.toString())
//                        Toast.makeText(getActivity()!!, response.toString(), Toast.LENGTH_SHORT).show()
                    }
                }

            })


        }

        //initData()

    }

fun fillCateories(sp: Spot){

    val CATEGORY_ID_ALL = 0
    val CATEGORY_ID_FAVORITES = 1
    val CATEGORY_ID_HOTELS = 2
    val CATEGORY_ID_MUSEUM = 3
    val CATEGORY_ID_ATTRACTION = 4
    val CATEGORY_ID_BEACHES = 5
    val CATEGORY_ID_SHOPPOING = 6
    val CATEGORY_ID_RESTAURANT = 7
    val CATEGORY_ID_SPORT = 8
    val CATEGORY_ID_TRANSPORT = 9
    val CATEGORY_ID_OTHER = 10
    val CATEGORY_ID_PROXIMITY = 11

    val category_all = Category(CATEGORY_ID_ALL, "", "")
    val category_fav = Category(CATEGORY_ID_FAVORITES, "", "")
    val category_hotel = Category(CATEGORY_ID_HOTELS, "hotels", "markers/hotels.png")
    val category_museum = Category(CATEGORY_ID_MUSEUM, "museums", "markers/museum.png")
    val category_attraction = Category(CATEGORY_ID_ATTRACTION, "attraction", "markers/attractions.png")
    val category_beach = Category(CATEGORY_ID_BEACHES, "beaches", "markers/beaches.png")
    val category_shopping = Category(CATEGORY_ID_SHOPPOING, "shopping", "markers/shopping.png")
    val category_restaurant = Category(CATEGORY_ID_RESTAURANT, "restaurants", "markers/food.png")
    val category_sport = Category(CATEGORY_ID_SPORT, "sport", "markers/sport.png")
    val category_transport = Category(CATEGORY_ID_TRANSPORT, "transport", "markers/transport.png")
    val category_other = Category(CATEGORY_ID_OTHER, "other", "markers/other.png")
    val category_proximity = Category(CATEGORY_ID_ALL, "proximity", "markers/proximity.png")


        when (sp.id_category) {
            CATEGORY_ID_ALL -> {
                sp.category = category_all
            }
            CATEGORY_ID_FAVORITES -> {
                sp.category = category_fav
            }
            CATEGORY_ID_HOTELS -> {
                sp.category = category_hotel
            }
            CATEGORY_ID_MUSEUM -> {
                sp.category = category_museum
            }
            CATEGORY_ID_ATTRACTION -> {
                sp.category = category_attraction
            }
            CATEGORY_ID_BEACHES -> {
                sp.category = category_beach
            }
            CATEGORY_ID_SHOPPOING -> {
                sp.category = category_shopping
            }
            CATEGORY_ID_RESTAURANT -> {
                sp.category = category_restaurant
            }
            CATEGORY_ID_SPORT -> {
                sp.category = category_sport
            }
            CATEGORY_ID_TRANSPORT -> {
                sp.category = category_transport
            }
            CATEGORY_ID_OTHER -> {
                sp.category = category_other
            }
            CATEGORY_ID_PROXIMITY -> {
                sp.category = category_proximity
            }
        }


}
    fun loadDataFromServer() {
        val listSpot_ = mutableListOf<Spot>()
        val call = RetrofitService.endPoint.getSpots()
        call.enqueue(object : Callback<ArrayList<Spot>> {

            override fun onFailure(call: Call<ArrayList<Spot>>?, t: Throwable?) {
                //progressBar.visibility = View.GONE
                //toast("erreur")
                showOffline()
                Logcat.d("error")
//                Toast.makeText(getActivity()!!, "error", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<ArrayList<Spot>>?, response: Response<ArrayList<Spot>>?) {
                //progressBar.visibility = View.GONE
                if (response?.isSuccessful!!) {
                    val list: ArrayList<Spot> = response.body()!!

                    for (item in list) {
                        RoomService.appDataBase.getSpotDao().addSpot(item)
                        // Toast.makeText(getActivity()!!, "ObjetAjouté "+item.id_restaurant, Toast.LENGTH_SHORT).show()
                        listSpot_.add(item)
                        fillCateories(item)
                        mSpotList.add(item)
                    }


                    showContent()
                    renderView()
                } else {
                    showEmpty()
                    Logcat.d(""+response.toString())
//                    Toast.makeText(getActivity()!!, response.toString(), Toast.LENGTH_SHORT).show()
                }
            }

        })
    }




    private fun showContent() {
        // show content container
//        val containerContent = mRootView!!.findViewById<View>(R.id.container_content) as ViewGroup
//        val containerProgress = mRootView!!.findViewById<View>(R.id.container_progress) as ViewGroup
//        val containerOffline = mRootView!!.findViewById<View>(R.id.container_offline) as ViewGroup
//        val containerEmpty = mRootView!!.findViewById<View>(R.id.container_empty) as ViewGroup
//        containerContent.visibility = View.VISIBLE
//        containerProgress.visibility = View.GONE
//        containerOffline.visibility = View.GONE
//        containerEmpty.visibility = View.GONE
        mViewState = ViewState.CONTENT
    }


    private fun showProgress() {
        // show progress container
//        val containerContent = mRootView!!.findViewById<View>(R.id.container_content) as ViewGroup
//        val containerProgress = mRootView!!.findViewById<View>(R.id.container_progress) as ViewGroup
//        val containerOffline = mRootView!!.findViewById<View>(R.id.container_offline) as ViewGroup
//        val containerEmpty = mRootView!!.findViewById<View>(R.id.container_empty) as ViewGroup
//        containerContent.visibility = View.GONE
//        containerProgress.visibility = View.VISIBLE
//        containerOffline.visibility = View.GONE
//        containerEmpty.visibility = View.GONE
        mViewState = ViewState.PROGRESS
    }


    private fun showOffline() {
        // show offline container
//        val containerContent = mRootView!!.findViewById<View>(R.id.container_content) as ViewGroup
//        val containerProgress = mRootView!!.findViewById<View>(R.id.container_progress) as ViewGroup
//        val containerOffline = mRootView!!.findViewById<View>(R.id.container_offline) as ViewGroup
//        val containerEmpty = mRootView!!.findViewById<View>(R.id.container_empty) as ViewGroup
//        containerContent.visibility = View.GONE
//        containerProgress.visibility = View.GONE
//        containerOffline.visibility = View.VISIBLE
//        containerEmpty.visibility = View.GONE
        mViewState = ViewState.OFFLINE
    }


    private fun showEmpty() {
        // show empty container
//        val containerContent = mRootView!!.findViewById<View>(R.id.container_content) as ViewGroup
//        val containerProgress = mRootView!!.findViewById<View>(R.id.container_progress) as ViewGroup
//        val containerOffline = mRootView!!.findViewById<View>(R.id.container_offline) as ViewGroup
//        val containerEmpty = mRootView!!.findViewById<View>(R.id.container_empty) as ViewGroup
//        containerContent.visibility = View.GONE
//        containerProgress.visibility = View.GONE
//        containerOffline.visibility = View.GONE
//        containerEmpty.visibility = View.VISIBLE
        mViewState = ViewState.EMPTY
    }


    private fun initMap() {
        if (!Version.isSupportedOpenGlEs2(activity)) {
            Toast.makeText(activity, "You device does not support map", Toast.LENGTH_LONG).show()
        }

        try {
            MapsInitializer.initialize(activity)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun setMapType(type: Int) {
        if (mMap != null) {
            mMap!!.setMapType(type)

            val preferences = Preferences(activity)
            preferences.mapType = type
        }
    }


    private fun handleExtras(extras: Bundle) {
        if (extras.containsKey(MapActivity.EXTRA_SPOT_ID)) {
            mSpotId = extras.getInt(MapActivity.EXTRA_SPOT_ID)
        }
        if (extras.containsKey(MapActivity.EXTRA_SPOT_LATITUDE)) {
            mSpotLatitude = extras.getDouble(MapActivity.EXTRA_SPOT_LATITUDE)
        }
        if (extras.containsKey(MapActivity.EXTRA_SPOT_LONGITUDE)) {
            mSpotLongitude = extras.getDouble(MapActivity.EXTRA_SPOT_LONGITUDE)
        }
    }


    fun initData() {
//            calculateSpotDistances()
    }

}
