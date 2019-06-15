package smaillns.smail.dzair.fragment


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import smaillns.smail.dzair.CityGuideConfig

import smaillns.smail.dzair.R
import smaillns.smail.dzair.R.attr.content
import smaillns.smail.dzair.activity.MapActivity
import smaillns.smail.dzair.adapter.GalleryAdapter
import smaillns.smail.dzair.database.LocalStorage.RoomService
import smaillns.smail.dzair.database.RetrofitService
import smaillns.smail.dzair.entity.Image_
import smaillns.smail.dzair.entity.Spot
import smaillns.smail.dzair.entity.TestDB
import smaillns.smail.dzair.gelocation.Geolocation
import smaillns.smail.dzair.gelocation.GeolocationListener
import smaillns.smail.dzair.listener.AnimateImageLoadingListener
import smaillns.smail.dzair.utility.LocationUtility
import smaillns.smail.dzair.utility.Logcat
import smaillns.smail.dzair.utility.NetworkManager
import smaillns.smail.dzair.utility.Preferences
import smaillns.smail.dzair.view.GridSpacingItemDecoration
import smaillns.smail.dzair.view.ViewState
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

import com.google.android.gms.maps.model.LatLng
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import kotlinx.android.synthetic.main.content_spot_details_info.*
import org.jetbrains.anko.makeCall
import java.util.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SpotDetailsFragment() : TaskFragment(), GeolocationListener {

    private val TIMER_DELAY = 60000L // in milliseconds
    private val MAP_ZOOM = 14

    private var mViewState: ViewState? = null
    private var mRootView: View? = null

    var mAdapter: GalleryAdapter? = null

    private val mImageLoader = ImageLoader.getInstance()
    private var mDisplayImageOptions: DisplayImageOptions? = null
    private var mImageLoadingListener: ImageLoadingListener? = null
    private var mGeolocation: Geolocation? = null
    private var mLocation: Location? = null
    private var mTimerHandler: Handler? = null
    private var mTimerRunnable: Runnable? = null

    private var mLanguage_ :String? = null

    private lateinit var spot: Spot

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val preferences = Preferences(activity!!)
        mLanguage_ = preferences.language


        mRootView = inflater.inflate(R.layout.fragment_spot_details, container, false)
        return mRootView
    }


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

        mImageLoader.init(ImageLoaderConfiguration.createDefault(activity))


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
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        /*pause prolème*/
        renderView()

        // load and show data
        if (mViewState == null || mViewState === ViewState.OFFLINE) {
            loadData()
        } else if (mViewState === ViewState.CONTENT) {
            if (spot != null) renderView()
            showContent()
        } else if (mViewState === ViewState.PROGRESS) {
            showProgress()
        } else if (mViewState === ViewState.EMPTY) {
            showEmpty()
        }

        // init timer task
        setupTimer()
    }


    override fun onStart() {
        super.onStart()
    }


    override fun onResume() {
        super.onResume()

        // timer
        startTimer()
    }


    override fun onPause() {
        super.onPause()

        // timer
        stopTimer()

        // stop geolocation
        if (mGeolocation != null) mGeolocation!!.stop()
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

        // cancel async tasks
//        mDatabaseCallManager.cancelAllTasks()
    }


    override fun onDetach() {
        super.onDetach()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        // save current instance state
        super.onSaveInstanceState(outState)
        userVisibleHint = true
    }




    override fun onGeolocationRespond(geolocation: Geolocation, location: Location) {
        runTaskCallback(Runnable {
            if (mRootView == null) return@Runnable  // view was destroyed


            Logcat.d("Fragment.onGeolocationRespond(): " + location.provider + " / " + location.latitude + " / " + location.longitude + " / " + Date(location.time).toString())
            mLocation = location
            if(spot!=null) renderView()

        })
    }


    override fun onGeolocationFail(geolocation: Geolocation) {
        runTaskCallback(Runnable {
            if (mRootView == null) return@Runnable  // view was destroyed

            Logcat.d("Fragment.onGeolocationFail()")
        })
    }


    private fun setupTimer() {
        mTimerHandler = Handler()
        mTimerRunnable = object : Runnable {
            override fun run() {
                Logcat.d("Fragment.timerRunnable()")

                // start geolocation
                mGeolocation = null

                if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted
                    // Should we show an explanation?


                }else {
                    mGeolocation = Geolocation(activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager, this@SpotDetailsFragment)
                    Logcat.d("test", "Permission is granted")
                }

//                mGeolocation = Geolocation(activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager, this@SpotDetailsFragment)

                mTimerHandler!!.postDelayed(this, TIMER_DELAY)
            }
        }
    }


    private fun startTimer() {
        mTimerHandler!!.postDelayed(mTimerRunnable, 0)
    }


    private fun stopTimer() {
        mTimerHandler!!.removeCallbacks(mTimerRunnable)
    }


    private fun handleExtras(extras: Bundle) {
        spot = extras.getSerializable("spot") as Spot
    }


    private fun showContent() {
        // show content container
        val containerContent = mRootView!!.findViewById<View>(R.id.container_content) as ViewGroup
        val containerProgress = mRootView!!.findViewById<View>(R.id.container_progress) as ViewGroup
        val containerOffline = mRootView!!.findViewById<View>(R.id.container_offline) as ViewGroup
        val containerEmpty = mRootView!!.findViewById<View>(R.id.container_empty) as ViewGroup
        containerContent.visibility = View.VISIBLE
        containerProgress.visibility = View.GONE
        containerOffline.visibility = View.GONE
        containerEmpty.visibility = View.GONE
        mViewState = ViewState.CONTENT

    }


    private fun showProgress() {
        // show progress container
        val containerContent = mRootView!!.findViewById<View>(R.id.container_content) as ViewGroup
        val containerProgress = mRootView!!.findViewById<View>(R.id.container_progress) as ViewGroup
        val containerOffline = mRootView!!.findViewById<View>(R.id.container_offline) as ViewGroup
        val containerEmpty = mRootView!!.findViewById<View>(R.id.container_empty) as ViewGroup
        containerContent.visibility = View.GONE
        containerProgress.visibility = View.VISIBLE
        containerOffline.visibility = View.GONE
        containerEmpty.visibility = View.GONE
        mViewState = ViewState.PROGRESS
    }


    private fun showOffline() {
        // show offline container
        val containerContent = mRootView!!.findViewById<View>(R.id.container_content) as ViewGroup
        val containerProgress = mRootView!!.findViewById<View>(R.id.container_progress) as ViewGroup
        val containerOffline = mRootView!!.findViewById<View>(R.id.container_offline) as ViewGroup
        val containerEmpty = mRootView!!.findViewById<View>(R.id.container_empty) as ViewGroup
        containerContent.visibility = View.GONE
        containerProgress.visibility = View.GONE
        containerOffline.visibility = View.VISIBLE
        containerEmpty.visibility = View.GONE
        mViewState = ViewState.OFFLINE

    }


    private fun showEmpty() {
        // show empty container
        val containerContent = mRootView!!.findViewById<View>(R.id.container_content) as ViewGroup
        val containerProgress = mRootView!!.findViewById<View>(R.id.container_progress) as ViewGroup
        val containerOffline = mRootView!!.findViewById<View>(R.id.container_offline) as ViewGroup
        val containerEmpty = mRootView!!.findViewById<View>(R.id.container_empty) as ViewGroup
        containerContent.visibility = View.GONE
        containerProgress.visibility = View.GONE
        containerOffline.visibility = View.GONE
        containerEmpty.visibility = View.VISIBLE
        mViewState = ViewState.EMPTY

    }


    private fun renderView() {

        renderViewInfo()
        renderViewBanner()
        renderViewMap()
        renderViewDescription()
        renderViewGap()
    }

    fun renderViewInfo(){
        // reference
        val introTextView = mRootView!!.findViewById<View>(R.id.spot_name_text) as TextView
        val addressTextView = mRootView!!.findViewById<View>(R.id.spot_adress_text) as TextView
        val distanceTextView = mRootView!!.findViewById<View>(R.id.spot_distance_text) as TextView
        val linkTextView = mRootView!!.findViewById<View>(R.id.spot_link_text) as TextView
        val phoneTextView = mRootView!!.findViewById<View>(R.id.spot_phone_text) as TextView
        val fbTextView = mRootView!!.findViewById<View>(R.id.spot_fb_text) as TextView

        val mailTextView = mRootView!!.findViewById<View>(R.id.spot_mail_text) as TextView


        // intro
        if (spot.getIntro(mLanguage_!!) != null && !spot.getIntro(mLanguage_!!).trim().equals("")) {
            introTextView.setText(spot.getIntro(mLanguage_!!))
            linear_title_line.visibility = View.VISIBLE
        } else {
            linear_title_line.visibility = View.GONE
        }

        // address
        if (spot.getAddress(mLanguage_!!) != null && !spot.getAddress(mLanguage_!!).trim().equals("")) {
            addressTextView.setText(spot.getAddress(mLanguage_!!))
            linear_adress_line.visibility = View.VISIBLE
            linear_adress_line.setOnClickListener {
                Snackbar.make(mRootView!!, getString(R.string.action_explore_map), Snackbar.LENGTH_SHORT)
                        .setAction("Ok", {startMapActivity(spot)}).show()
                }
        } else {
            linear_adress_line.visibility = View.GONE
        }

        // distance
        if (mLocation != null) {
            val myLocation = LatLng(mLocation!!.getLatitude(), mLocation!!.getLongitude())
            val poiLocation = LatLng(spot.getLatitude(), spot.getLongitude())
            val distance = LocationUtility.getDistanceString(LocationUtility.getDistance(myLocation, poiLocation), LocationUtility.isMetricSystem())
            distanceTextView.setText(distance)
            linear_distance_line.visibility = View.VISIBLE
            distanceTextView.setOnClickListener{
                Snackbar.make(mRootView!!, getString(R.string.action_navigate), Snackbar.LENGTH_SHORT)
                        .setAction("Ok", {startNavigateActivity(spot.getLatitude(), spot.getLongitude())}).show()
            }
        } else {
            linear_distance_line.visibility = View.GONE
        }

        // link
        if (spot.link != null && !spot.link!!.trim().equals("")) {
            linkTextView.setText(spot.link)
            linear_link_line.visibility = View.VISIBLE
            linear_link_line.setOnClickListener {
                Snackbar.make(mRootView!!, getString(R.string.action_start_web_activity), Snackbar.LENGTH_SHORT)
                        .setAction("Ok", {startWebActivity(spot.link!!)}).show()
            }
        } else {
            linear_link_line.visibility = View.GONE
        }

        // phone
        if (spot.phone != null && !spot.phone!!.trim().equals("")) {
            phoneTextView.setText(spot.phone)
            linear_phone_line.visibility = View.VISIBLE
            linear_phone_line.setOnClickListener { startCallActivity(spot.phone) }
        } else {
            linear_phone_line.visibility = View.GONE
        }

        // mail
        if (spot.mail != null && !spot.mail!!.trim().equals("")) {
            mailTextView.setText(spot.mail)
            linear_mail_line.visibility = View.VISIBLE
            linear_mail_line.setOnClickListener {
                Snackbar.make(mRootView!!, getString(R.string.action_send_mail), Snackbar.LENGTH_SHORT)
                        .setAction("Ok", {startEmailActivity(spot.mail!!)}).show()
            }
        } else {
            linear_mail_line.visibility = View.GONE
        }

        // facebook
        if (spot.fb != null && !spot.fb!!.trim().equals("")) {
            fbTextView.setText(spot.fb)
            linear_fb_line.visibility = View.VISIBLE
            linear_fb_line.setOnClickListener {
                Snackbar.make(mRootView!!, getString(R.string.action_start_web_activity), Snackbar.LENGTH_SHORT)
                        .setAction("Ok", {startWebActivity("https://"+spot.fb!!)}).show()
            }
        } else {
            linear_fb_line.visibility = View.GONE
        }
    }
    fun renderViewBanner(){

        // reference
        val adView = mRootView!!.findViewById<View>(R.id.fragment_spot_detail_banner_adview) as AdView
        val bannerViewGroup = mRootView!!.findViewById<View>(R.id.fragment_spot_detail_banner) as ViewGroup

        // admob
        if (CityGuideConfig.ADMOB_POI_DETAIL_BANNER && NetworkManager.isOnline(activity)) {
            val adRequest = AdRequest.Builder()
                    .build()
            //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
            //.addTestDevice(getString(R.string.admob_test_device_id))
            adView.loadAd(adRequest)
            adView.visibility = View.VISIBLE
            bannerViewGroup.visibility = View.VISIBLE
        } else {
            adView.visibility = View.GONE
            bannerViewGroup.visibility = View.GONE
        }
    }
    fun renderViewMap(){
        // reference
        val imageView = mRootView!!.findViewById<View>(R.id.fragment_details_image_map) as ImageView
//        val wrapViewGroup = mRootView.findViewById<View>(R.id.fragment_poi_detail_map_image_wrap) as ViewGroup
        val exploreButton = mRootView!!.findViewById<View>(R.id.btn_explor) as Button
        val navigateButton = mRootView!!.findViewById<View>(R.id.btn_navigate) as Button

        // image
        val key = getString(R.string.maps_api_key)
        val url = getStaticMapUrl(key, spot.getLatitude(), spot.getLongitude(), MAP_ZOOM)

        mImageLoader.displayImage(url, imageView, mDisplayImageOptions, mImageLoadingListener)

        // wrap
        imageView.setOnClickListener {
            startNavigateActivity(spot.getLatitude(), spot.getLongitude())
            //startMapActivity(spot)
        }

        // explore
        exploreButton.setOnClickListener {
            startMapActivity(spot)
        }

        // navigate
        navigateButton.setOnClickListener {
            startNavigateActivity(spot.getLatitude(), spot.getLongitude())
        }
    }
    fun renderViewDescription(){
        val descriptionTextViw = mRootView!!.findViewById<View>(R.id.textView_description) as TextView

        if (mLanguage_ == "fr")
            descriptionTextViw.setText(spot.description)
        else
            descriptionTextViw.setText(spot.description_en)

    }

    fun setupGallery(){

        val recyclerView = getRecyclerView()

        if (recyclerView!!.adapter == null) {
            // create adapter
            mAdapter = GalleryAdapter(spot.images!!, activity!!)
        } else {
            // refill adapter
            mAdapter!!.refill(spot.images!!)
        }


        // set fixed size
        recyclerView.setHasFixedSize(false)
        recyclerView.setNestedScrollingEnabled(false)

        // add decoration
        val itemDecoration = GridSpacingItemDecoration(resources.getDimensionPixelSize(R.dimen.fragment_spot_list_recycler_item_padding))
        recyclerView.addItemDecoration(itemDecoration)

        // set animator
        //recyclerView.itemAnimator = DefaultItemAnimator()

        // set adapter
        recyclerView.adapter = mAdapter

    }
    fun renderViewGap(){
        setupRecyclerView()




        if (spot.images == null)
            {
//                Toast.makeText(getActivity()!!, "spot.images == null", Toast.LENGTH_SHORT).show()
                loadImagesFromServer()
            }
        else
            {
//                Toast.makeText(getActivity()!!, "spot.images != null", Toast.LENGTH_SHORT).show()

//                for ( item in spot.images!! ){
//                    Toast.makeText(getActivity()!!, ""+item, Toast.LENGTH_SHORT).show()
//                }

                try{
                    setupGallery()
                }catch (e : Exception){

                }



                val call = RetrofitService.endPoint.getNbrImages(spot.id)

                call.enqueue(object : Callback<List<TestDB>> {
                    override fun onFailure(call: Call<List<TestDB>>?, t: Throwable?) {
                        //progressBar.visibility = View.GONE
                        Logcat.d("error server")
//                    Toast.makeText(getActivity()!!, "error server", Toast.LENGTH_SHORT).show()
                    }
                    override fun onResponse(call: Call<List<TestDB>>?, response: Response<List<TestDB>>?) {
                        //progressBar.visibility = View.GONE
                        if (response?.isSuccessful!!) {
                            val nbr = response.body()!![0].nbr_images
                            if (nbr != spot.images!!.size)
                                loadImagesFromServer()

                        } else {
                            Logcat.d(""+response.toString())
//                        Toast.makeText(getActivity()!!, response.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }

                })


            }


    }

    private fun setupRecyclerView() {
        val gridLayoutManager = GridLayoutManager(activity, getGridSpanCount())
        gridLayoutManager.orientation = GridLayoutManager.VERTICAL
        val recyclerView = getRecyclerView()
        recyclerView!!.setLayoutManager(gridLayoutManager)

    }

    private fun getGridSpanCount(): Int {
        val display = activity!!.getWindowManager().defaultDisplay
        val displayMetrics = DisplayMetrics()
        display.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels.toFloat()
        val cellWidth = resources.getDimension(R.dimen.fragment_image_gallery_recycler_item_size)
        return Math.round(screenWidth / cellWidth)
    }

    private fun getRecyclerView(): RecyclerView? {
        return if (mRootView != null) mRootView!!.findViewById<View>(R.id.list_images_recycler_view2) as RecyclerView else null
    }



    /*nbr images or the spot*/

    fun loadImagesFromServer(){
        val listImages_ = mutableListOf<String>()
        val call = RetrofitService.endPoint.getImages(spot.id)
        call.enqueue(object : Callback<ArrayList<Image_>> {

            override fun onFailure(call: Call<ArrayList<Image_>>?, t: Throwable?) {
                //progressBar.visibility = View.GONE
                //toast("erreur")
//                showOffline()
                Logcat.d("error")
            }

            override fun onResponse(call: Call<ArrayList<Image_>>?, response: Response<ArrayList<Image_>>?) {
                //progressBar.visibility = View.GONE
                if (response?.isSuccessful!!) {
                    val list: ArrayList<Image_> = response.body()!!
                    spot.images = ArrayList<String>()
                    for (item in list) {
                        listImages_.add(item.name)
//                        spot.images!!.add(item.name)
                    }
                    for (item in listImages_) {
                        spot.images!!.add(item)
                    }
                    RoomService.appDataBase.getSpotDao().deleteSpot(spot)
                    RoomService.appDataBase.getSpotDao().addSpot(spot)

                    try {
                        setupGallery()
                    }catch (e:Exception){

                    }


                } else {
//                    showEmpty()
                    Logcat.d(""+response.toString())
//                    Toast.makeText(getActivity()!!, response.toString(), Toast.LENGTH_SHORT).show()
                }
            }

        })

    }

    private fun startEmailActivity(email: String) {
        try {
            val builder = StringBuilder()
            builder.append("mailto:")
            builder.append(email)

            val intent = Intent(android.content.Intent.ACTION_SENDTO, Uri.parse(builder.toString()))
            startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            // can't start activity
        }

    }


    fun startWebActivity(url:String?){

        try {
            val intent = Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            // can't start activity
        }

    }

    fun startCallActivity(phone : String?){
//        snackbar(content, "voullez vous effectuer l'appel", "OK")
//         {makeCall(phone)}
//

        checkForPermission()

        Snackbar.make(mRootView!!, getString(R.string.action_call), Snackbar.LENGTH_SHORT)
                    .setAction("Ok", {activity!!.makeCall(phone!!)}).show()


    }
    fun checkForPermission(){

        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!,
                            Manifest.permission.CALL_PHONE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity!!,
                        arrayOf(Manifest.permission.CALL_PHONE),
                        2)

                Logcat.d("test","request fot permission")
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }


        }else {
            Logcat.d("test", "Permission is granted")
        }
    }



    private fun startMapActivity(spot: Spot) {
        val intent = MapActivity.newIntent(activity!!, spot.id, spot.getLatitude(), spot.getLongitude())
        startActivity(intent)
    }

    private fun startNavigateActivity(lat: Double, lon: Double) {
        try {
            val uri = String.format("http://maps.google.com/maps?daddr=%s,%s", java.lang.Double.toString(lat), java.lang.Double.toString(lon))
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            // can't start activity
        }

    }

    private fun getStaticMapUrl(key: String, lat: Double, lon: Double, zoom: Int): String {
        val typedValue = TypedValue()
        activity!!.getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true)
        val markerColor = typedValue.data
        val markerColorHex = String.format("0x%06x", 0xffffff and markerColor)

        val builder = StringBuilder()
        builder.append("https://maps.googleapis.com/maps/api/staticmap")
        builder.append("?key=")
        builder.append(key)
        builder.append("&size=320x320")
        builder.append("&scale=2")
        builder.append("&maptype=roadmap")
        builder.append("&zoom=")
        builder.append(zoom)
        builder.append("&center=")
        builder.append(lat)
        builder.append(",")
        builder.append(lon)
        builder.append("&markers=color:")
        builder.append(markerColorHex)
        builder.append("%7C")
        builder.append(lat)
        builder.append(",")
        builder.append(lon)
        return builder.toString()
    }


    private fun loadData() {
//        // load spot
//        if (!mDatabaseCallManager.hasRunningTask(PoiReadQuery::class.java)) {
//            // show progress
//            showProgress()
//
//            // run async task
//            val query = PoiReadQuery(mPoiId)
//            mDatabaseCallManager.executeTask(query, this)
//        }
    }

}
