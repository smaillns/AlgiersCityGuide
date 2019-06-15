package smaillns.smail.dzair.fragment


import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.SearchRecentSuggestions
import android.provider.Settings
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ActionMode
import android.support.v7.widget.*
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.AutoCompleteTextView
import smaillns.smail.dzair.CityGuideConfig
import smaillns.smail.dzair.Query

import smaillns.smail.dzair.R
import smaillns.smail.dzair.adapter.SearchSuggestionAdapter
import smaillns.smail.dzair.adapter.SpotListAdapter
import smaillns.smail.dzair.content.SpotSearchRecentSuggestionsProvider
import smaillns.smail.dzair.database.LocalStorage.RoomService
import smaillns.smail.dzair.database.RetrofitService
import smaillns.smail.dzair.entity.Spot
import smaillns.smail.dzair.entity.TestDB
import smaillns.smail.dzair.gelocation.Geolocation
import smaillns.smail.dzair.gelocation.GeolocationListener
import smaillns.smail.dzair.listener.OnSearchListener
import smaillns.smail.dzair.utility.LocationUtility
import smaillns.smail.dzair.utility.Logcat
import smaillns.smail.dzair.utility.NetworkManager
import smaillns.smail.dzair.view.GridSpacingItemDecoration
import smaillns.smail.dzair.view.ViewState
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.maps.model.LatLng
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


class SpotListFragment() :  TaskFragment(), GeolocationListener {

    private var mLazyLoading = false
    private var mViewState: ViewState? = null
    private var mRootView: View? = null
    private var mAdapter: SpotListAdapter? = null
    private var mSearchListener: OnSearchListener? = null
    private var mActionMode: ActionMode? = null
//    private val mDatabaseCallManager = DatabaseCallManager()
    private var mGeolocation: Geolocation? = null
    private var mLocation: Location? = null
    private var mTimerHandler: Handler? = null
    private var mTimerRunnable: Runnable? = null


    private var mCategoryId: Int = 0
    private var mSearchQuery: String? = null

    private val mFooterList = java.util.ArrayList<Any>()


    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    val mDataset = ArrayList<Spot>()

    companion object {
            val CATEGORY_ID_ALL = 0
            val CATEGORY_ID_FAVORITES = 1
            val CATEGORY_ID_SEARCH = -3L


            private val ARGUMENT_CATEGORY_ID = "category_id"
            private val ARGUMENT_SEARCH_QUERY = "search_query"
            private val DIALOG_ABOUT = "about"
            private val TIMER_DELAY = 60000L // in milliseconds
            private val LAZY_LOADING_TAKE = 128
            private val LAZY_LOADING_OFFSET = 4



            fun newInstance(categoryId: Int): SpotListFragment {
                val fragment = SpotListFragment()

                // arguments
                val arguments = Bundle()
                arguments.putInt(ARGUMENT_CATEGORY_ID, categoryId)
                fragment.setArguments(arguments)

                return fragment
            }

            fun newInstance(searchQuery: String): SpotListFragment {
                val fragment = SpotListFragment()

                // arguments
                val arguments = Bundle()
                arguments.putLong(ARGUMENT_CATEGORY_ID, CATEGORY_ID_SEARCH)
                arguments.putString(ARGUMENT_SEARCH_QUERY, searchQuery)
                fragment.setArguments(arguments)

                return fragment
            }
        }


    override fun onAttach(activity: Activity) {
        super.onAttach(activity)

        // set search listener
        try {
           mSearchListener = activity as OnSearchListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.javaClass.name + " must implement " + OnSearchListener::class.java.name)
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setHasOptionsMenu(true)
        retainInstance = true

        // handle fragment arguments
        val arguments = arguments
        if (arguments != null) {
            handleArguments(arguments)
        }
    }

    private fun handleArguments(arguments: Bundle) {
        mCategoryId = arguments.getInt(ARGUMENT_CATEGORY_ID, CATEGORY_ID_ALL)
        mSearchQuery = arguments.getString(ARGUMENT_SEARCH_QUERY, "")
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_spot_list, container, false)
//        initData()
        setupRecyclerView()

        return mRootView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


//        /*Loadata to remove*/
//        loadData()
//        mViewState = ViewState.CONTENT

        // load and show data
        if (mViewState == null || mViewState === ViewState.OFFLINE) {
            loadData()
        } else if (mViewState === ViewState.CONTENT) {
            if (mDataset != null) renderView()
            showContent()
        } else if (mViewState === ViewState.PROGRESS) {
            showProgress()
        } else if (mViewState === ViewState.EMPTY) {
            showEmpty()
        }

        // lazy loading progress
        if (mLazyLoading) showLazyLoadingProgress(true)

        // show toolbar if hidden
        showToolbar(true)

        // init timer task
        setupTimer()

        //statusCheck()   // to enbale GPS
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

        // stop adapter
        if (mAdapter != null) mAdapter!!.stop()

        // stop geolocation
        if (mGeolocation != null) mGeolocation!!.stop()
    }


    override fun onStop() {
        super.onStop()
    }



    private fun setupRecyclerView() {
        val gridLayoutManager = GridLayoutManager(activity, getGridSpanCount())
        gridLayoutManager.orientation = GridLayoutManager.VERTICAL
        val recyclerView = getRecyclerView()
        recyclerView!!.setLayoutManager(gridLayoutManager)

//        viewAdapter = SpotListAdapter(mDataset, null, getActivity()!!)
//        recyclerView.adapter = viewAdapter
    }


    private fun getGridSpanCount(): Int {
        val display = activity!!.getWindowManager().defaultDisplay
        val displayMetrics = DisplayMetrics()
        display.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels.toFloat()
        val cellWidth = resources.getDimension(R.dimen.fragment_spot_list_recycler_item_size)
        return Math.round(screenWidth / cellWidth)
    }

    private fun getRecyclerView(): RecyclerView? {
        return if (mRootView != null) mRootView!!.findViewById<View>(R.id.list_spot_recycler_view) as RecyclerView else null
    }

    private fun setupTimer() {
        mTimerHandler = Handler()
        mTimerRunnable = object : Runnable {
            override fun run() {
                Logcat.d("Fragment.timerRunnable()")

                // start geolocation
                mGeolocation = null
                checkForPermission()
                checkForOtherPermissions()
                //mGeolocation = Geolocation(activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager, this@SpotListFragment)

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

    override fun onGeolocationRespond(geolocation: Geolocation, location: Location) {
        runTaskCallback(Runnable {
            if (mRootView == null) return@Runnable  // view was destroyed


            Logcat.d("Fragment.onGeolocationRespond(): " + location.provider + " / " + location.latitude + " / " + location.longitude + " / " + Date(location.time).toString())
            mLocation = location

            // calculate distances and sort
            calculateSpotDistances()
            sortSpotByDistance()
            if (mAdapter != null && mLocation != null && mDataset != null && mDataset.size > 0) mAdapter!!.notifyDataSetChanged()
        })
    }


    override fun onGeolocationFail(geolocation: Geolocation) {
        runTaskCallback(Runnable {
            if (mRootView == null) return@Runnable  // view was destroyed

            Logcat.d("Fragment.onGeolocationFail()")
        })
    }


    private fun calculateSpotDistances() {
        if (mLocation != null && mDataset != null && mDataset.size > 0) {
            for (i in mDataset.indices) {
                val spot = mDataset.get(i)
                val myLocation = LatLng(mLocation!!.getLatitude(), mLocation!!.getLongitude())
                val poiLocation = LatLng(spot.getLatitude(), spot.getLongitude())
                val distance = LocationUtility.getDistance(myLocation, poiLocation)
                spot.setDistancee(distance)
            }
        }
    }


    private fun sortSpotByDistance() {
        if (mLocation != null && mDataset != null && mDataset.size > 0) {
            Collections.sort(mDataset)
        }
    }


    @SuppressLint("RestrictedApi")
    private fun renderView() {
        // reference
        val recyclerView = getRecyclerView()
        val floatingActionButton = activity!!.findViewById(R.id.fab) as FloatingActionButton
        val adView = mRootView!!.findViewById<View>(R.id.fragment_spot_list_adview) as AdView

        // content
        if (recyclerView!!.adapter == null) {
            // create adapter
            mAdapter = SpotListAdapter(mDataset, mFooterList, this.activity!!)
        } else {
            // refill adapter
            mAdapter!!.refill(mDataset, mFooterList)
        }

        // set fixed size
        recyclerView.setHasFixedSize(false)

        // add decoration
        val itemDecoration = GridSpacingItemDecoration(resources.getDimensionPixelSize(R.dimen.fragment_spot_list_recycler_item_padding))
        recyclerView.addItemDecoration(itemDecoration)

        // set animator
        recyclerView.itemAnimator = DefaultItemAnimator()

        // set adapter
        recyclerView.adapter = mAdapter

        // lazy loading
        recyclerView.setOnScrollListener(object : RecyclerView.OnScrollListener() {
            private val THRESHOLD = 100

            private var mCounter = 0
            private val mToolbar = activity!!.findViewById(R.id.toolbar) as Toolbar


            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                // reset counter
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    mCounter = 0
                }

                // disable item animation in adapter
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    mAdapter!!.setAnimationEnabled(false)
                }
            }


            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView!!.layoutManager as GridLayoutManager
                val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = firstVisibleItem + visibleItemCount

                // lazy loading
                if (totalItemCount - lastVisibleItem <= LAZY_LOADING_OFFSET && mDataset.size % LAZY_LOADING_TAKE == 0 && !mDataset.isEmpty()) {
                    if (!mLazyLoading) lazyLoadData()
                }

                // toolbar and FAB animation
                mCounter += dy
                if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_DRAGGING || recyclerView.scrollState == RecyclerView.SCROLL_STATE_SETTLING) {
                    // scroll down
                    if (mCounter > THRESHOLD && firstVisibleItem > 0) {
                        // hide toolbar
                        if (mToolbar.visibility == View.VISIBLE && mToolbar.isEnabled) {
                            showToolbar(false)
                        }

                        // hide FAB
                        showFloatingActionButton(false)

                        mCounter = 0
                    } else if (mCounter < -THRESHOLD || firstVisibleItem == 0) {
                        // show toolbar
                        if (mToolbar.visibility == View.GONE && mToolbar.isEnabled) {
                            showToolbar(true)
                        }

                        // show FAB
                        showFloatingActionButton(true)

                        mCounter = 0
                    }// scroll up
                }
            }
        })

        // floating action button
//        floatingActionButton.setOnClickListener {
//            mActionMode = (activity as AppCompatActivity).getSupportActionBar()!!.
//                    startActionMode(this.SearchActionModeCallback())
//            Logcat.d("test","I am here")
//        }

        // admob
        if (CityGuideConfig.ADMOB_POI_LIST_BANNER && NetworkManager.isOnline(activity)) {
            val adRequest = AdRequest.Builder().build()
            //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
            //.addTestDevice(getString(R.string.admob_test_device_id))
            adView.loadAd(adRequest)
            adView.setVisibility(View.VISIBLE)
        } else {
            adView.setVisibility(View.GONE)
        }
    }


    private fun showFloatingActionButton(visible: Boolean) {
        try {
            val fab = activity!!.findViewById(R.id.fab) as FloatingActionButton
            if (visible) {
                //fab.show()
            } else {
                //fab.hide()
            }
        }catch (e : Exception){
            Logcat.d("error excpetion")
        }

    }


    private fun showToolbar(visible: Boolean) {
        val toolbar = activity!!.findViewById(R.id.toolbar) as Toolbar
        if (visible) {
            toolbar.animate()
                    .translationY(0f)
                    .setDuration(200)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .setListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animator: Animator) {
                            toolbar.visibility = View.VISIBLE
                            toolbar.isEnabled = false
                        }

                        override fun onAnimationEnd(animator: Animator) {
                            toolbar.isEnabled = true
                        }

                        override fun onAnimationCancel(animator: Animator) {}

                        override fun onAnimationRepeat(animator: Animator) {}
                    })
        } else {
            toolbar.animate()
                    .translationY((-toolbar.bottom).toFloat())
                    .setDuration(200)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .setListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animator: Animator) {
                            toolbar.isEnabled = false
                        }


                        override fun onAnimationEnd(animator: Animator) {
                            toolbar.visibility = View.GONE
                            toolbar.isEnabled = true
                        }


                        override fun onAnimationCancel(animator: Animator) {}


                        override fun onAnimationRepeat(animator: Animator) {}
                    })
        }
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

        // floating action button
        showFloatingActionButton(true)
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

        // floating action button
        showFloatingActionButton(false)
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

        // floating action button
        showFloatingActionButton(false)
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

        // floating action button
        showFloatingActionButton(false)
    }

    private fun loadData() {
        showProgress()

        val list = RoomService.appDataBase.getSpotDao().getSpots()
        if(mCategoryId == CATEGORY_ID_FAVORITES){

            val favs = RoomService.appDataBase.getFavoriteDao().getFavorites()
            for (spot in list){
                for (item in favs){
                    if ((item.id == spot.id) &&(item.fav == 1))
                    {
                        mDataset.add(spot)
                        break;
                    }
                }
            }
        }
        else{
            for (spot in list){

                if (spot.id_category == mCategoryId || mCategoryId == 0)
                    mDataset.add(spot)
            }

        }


        if (mDataset.size == 0) {
            if (mCategoryId == CATEGORY_ID_FAVORITES)
                showEmpty()
            else
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
                    if (nbr != mDataset.size && mCategoryId != CATEGORY_ID_FAVORITES && mCategoryId == CATEGORY_ID_ALL) {
                        mDataset.clear()
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

        calculateSpotDistances()
        sortSpotByDistance()

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
                        if (item.id_category == mCategoryId || mCategoryId == 0)
                            mDataset.add(item)
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

    private fun showLazyLoadingProgress(visible: Boolean) {
        if (visible) {
            mLazyLoading = true

            // show footer
            if (mFooterList.size <= 0) {
                mFooterList.add(Any())
                mAdapter!!.notifyItemInserted(mAdapter!!.getRecyclerPositionByFooter(0))
            }
        } else {
            // hide footer
            if (mFooterList.size > 0) {
                mFooterList.removeAt(0)
                mAdapter!!.notifyItemRemoved(mAdapter!!.getRecyclerPositionByFooter(0))
            }

            mLazyLoading = false
        }
    }

    private fun lazyLoadData() {
        // show lazy loading progress
        showLazyLoadingProgress(true)

        // run async task
        val query: Query
//        if (mCategoryId == CATEGORY_ID_ALL) {
//            query = PoiReadAllQuery(mPoiList.size, LAZY_LOADING_TAKE)
//        } else if (mCategoryId == CATEGORY_ID_FAVORITES) {
//            query = PoiReadFavoritesQuery(mPoiList.size, LAZY_LOADING_TAKE)
//        } else if (mCategoryId == CATEGORY_ID_SEARCH) {
//            query = PoiSearchQuery(mSearchQuery, mPoiList.size, LAZY_LOADING_TAKE)
//        } else {
//            query = PoiReadByCategoryQuery(mCategoryId, mPoiList.size, LAZY_LOADING_TAKE)
//        }
//        mDatabaseCallManager.executeTask(query, this)
    }

    fun initData() {
//
//        mDataset.add(Spot(1,"nom11","nom_en","alg",36.755 ,3.0417,"","","Description...","",null,null,
//                "0552349395","www.algiers-city-guide.dz",0,null,null,null,null,Category(1,"All","markers/food.png",null)))
//
//        mDataset.add(Spot(2,"nom12","nom_en","alger.jpg",36.7525 ,3.06197,"","","Description...","",null,null,
//                "0552349395","www.algiers-city-guide.dz",0,null,"smaillounes1@gmail.com",null,null,Category(2,"attractions","markers/attractions.png",null)))
//        mDataset.add(Spot(3,"nom13","nom_en","eiffeltower.jpg",36.7925 ,3.04197,"","","Description...","",null,null,
//                "0552349395","www.algiers-city-guide.dz",0,null,null,null,null,Category(3,"All","markers/services.png",null)))
//
//
//        mDataset.add(Spot(4,"nom21","nom_en","eiffeltower.jpg",36.1525 ,3.0197,"","","Description...","",null,null,
//                "0552349395","www.algiers-city-guide.dz",0,null,null,null,null,Category(4,"All","markers/sport.png",null)))
//
//        mDataset.add(Spot(5,"nom22","nom_en","eiffeltower.jpg",36.7565 ,3.14197,"","","Description...","",null,null,
//                "0552349395","www.algiers-city-guide.dz",0,null,null,null,null,Category(4,"All","markers/sport.png",null)))
//
//
//        mDataset.add(Spot(6,"nom31","nom_en","alger.jpg",36.5755 ,3.0417,"","","Description...","",null,null,
//                "0552349395","www.algiers-city-guide.dz",0,null,null,null,null,Category(6,"All","marker",null)))
//        mDataset.add(Spot(7,"nom32","nom_en","eiffeltower.jpg",36.7525 ,3.04197,"","","Description...","",null,null,
//                "0552349395","www.algiers-city-guide.dz",0,"facebook.com/smail.lounes.58",null,null,null,Category(5,"All","markers/transport.png",null)))
//        mDataset.add(Spot(8,"nom33","nom_en","eiffeltower.jpg",36.7325 ,3.0970,"","","Description...","",null,null,
//                "0552349395","www.algiers-city-guide.dz",0,null,null,null,null,Category(7,"All","markers/shopping.png",null)))
//        mDataset.add(Spot(9,"nom34","nom_en","alger.jpg",36.7525 ,3.04297,"","","Description...","",null,null,
//                "0552349395","www.algiers-city-guide.dz",0, null,null,null,null,Category(8,"All","markers/hotels.png",null)))
//
//
//        mDataset.add(Spot(10,"nom35","nom_en","alger.jpg",36.5525 ,3.03197,"","","Description...","",null,null,
//                "0552349395","www.algiers-city-guide.dz",0,null,null,null,null,Category(6,"All","marker",null)))
//
//
//        calculateSpotDistances()
    }


    private inner class SearchActionModeCallback() : ActionMode.Callback {
        private var mSearchView: SearchView? = null
        private var mSearchSuggestionAdapter: SearchSuggestionAdapter? = null


        override fun onCreateActionMode(actionMode: ActionMode, menu: Menu): Boolean {
            // search view
            mSearchView = SearchView((activity as AppCompatActivity).getSupportActionBar()!!.getThemedContext())
            setupSearchView(mSearchView!!)

            // search menu item
            val searchMenuItem = menu.add(Menu.NONE, Menu.NONE, 1, getString(R.string.menu_search))
            searchMenuItem.setIcon(R.drawable.ic_search_black_24dp)
            MenuItemCompat.setActionView(searchMenuItem, mSearchView)
            MenuItemCompat.setShowAsAction(searchMenuItem, MenuItem.SHOW_AS_ACTION_ALWAYS)

            return true
        }


        override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
            showFloatingActionButton(false)
            return true
        }


        override fun onActionItemClicked(actionMode: ActionMode, menuItem: MenuItem): Boolean {
            return false
        }


        override fun onDestroyActionMode(actionMode: ActionMode) {
            showFloatingActionButton(true)
        }


        private fun setupSearchView(searchView: SearchView) {
            // expand action view
            searchView.setIconifiedByDefault(true)
            searchView.isIconified = false
            searchView.onActionViewExpanded()

            // search hint
            searchView.queryHint = getString(R.string.menu_search_hint)

            // text color
            val searchText = searchView.findViewById<View>(android.support.v7.appcompat.R.id.search_src_text) as AutoCompleteTextView
            searchText.setTextColor(resources.getColor(R.color.global_text_primary_inverse))
            searchText.setHintTextColor(resources.getColor(R.color.global_text_secondary_inverse))

            // suggestion listeners
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    // listener
                    mSearchListener!!.onSearch(query)

                    // save query for suggestion
                    val suggestions = SearchRecentSuggestions(activity, SpotSearchRecentSuggestionsProvider.AUTHORITY, SpotSearchRecentSuggestionsProvider.MODE)
                    suggestions.saveRecentQuery(query, null)

                    // close action mode
                    mActionMode!!.finish()

                    return true
                }

                override fun onQueryTextChange(query: String): Boolean {
                    if (query.length > 2) {
                        updateSearchSuggestion(query)
                    }
                    return true
                }
            })
            searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
                override fun onSuggestionSelect(position: Int): Boolean {
                    return false
                }

                override fun onSuggestionClick(position: Int): Boolean {
                    // get query
                    val cursor = mSearchSuggestionAdapter!!.getItem(position) as Cursor
                    val title = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1))

                    // listener
                    mSearchListener!!.onSearch(title)

                    // close action mode
                    mActionMode!!.finish()

                    return true
                }
            })
        }


        private fun updateSearchSuggestion(query: String) {
            // cursor
            val contentResolver = activity!!.getApplicationContext().contentResolver
            val contentUri = "content://" + SpotSearchRecentSuggestionsProvider.AUTHORITY + '/' + SearchManager.SUGGEST_URI_PATH_QUERY
            val uri = Uri.parse(contentUri)
            val cursor = contentResolver.query(uri, null, null, arrayOf(query), null)

            // searchview content
            if (mSearchSuggestionAdapter == null) {
                // create adapter
                mSearchSuggestionAdapter = SearchSuggestionAdapter(activity!!, cursor)

                // set adapter
                mSearchView!!.suggestionsAdapter = mSearchSuggestionAdapter
            } else {
                // refill adapter
                mSearchSuggestionAdapter!!.refill(activity!!, cursor)

                // set adapter
                mSearchView!!.suggestionsAdapter = mSearchSuggestionAdapter
            }
        }
    }



    fun checkForPermission(){
        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                    || ActivityCompat.shouldShowRequestPermissionRationale(activity!!,
                            Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(activity!!,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                        5)


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity!!,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                        1)

                Logcat.d("test","request fot permission")
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }


        }else {
            mGeolocation = Geolocation(activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager, this@SpotListFragment)
            Logcat.d("test", "Permission is granted")
        }


    }
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    mGeolocation = Geolocation(activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager, this@SpotListFragment)
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    fun checkForOtherPermissions(){
        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                    || ActivityCompat.shouldShowRequestPermissionRationale(activity!!,
                            Manifest.permission.ACCESS_NETWORK_STATE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity!!,
                        arrayOf(Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE),
                        3)

                Logcat.d("test","request fot permission")
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }


        }else {
            Logcat.d("test", "Permission is granted")
        }

//        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            // Permission is not granted
//            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!,
//                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//            } else {
//                // No explanation needed, we can request the permission.
//                ActivityCompat.requestPermissions(activity!!,
//                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
//                        4)
//
//                Logcat.d("test","request fot permission")
//                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//                // app-defined int constant. The callback method gets the
//                // result of the request.
//            }
//
//
//        }else {
//            Logcat.d("test", "Permission is granted")
//        }
//
//
//        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.READ_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            // Permission is not granted
//            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!,
//                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//            } else {
//                // No explanation needed, we can request the permission.
//                ActivityCompat.requestPermissions(activity!!,
//                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
//                        5)
//
//                Logcat.d("test","request fot permission")
//                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//                // app-defined int constant. The callback method gets the
//                // result of the request.
//            }
//
//
//        }else {
//            Logcat.d("test", "Permission is granted")
//        }
//


    }






    fun statusCheck(){
        val manager = activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }

    }

    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(activity!!)
        builder.setMessage(getString(R.string.gps))
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
                .setNegativeButton("No") { dialog, id -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }




}
