package smaillns.smail.toulouse.activity

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import smaillns.smail.toulouse.R
import smaillns.smail.toulouse.fragment.SpotListFragment
import smaillns.smail.toulouse.listener.OnSearchListener
import smaillns.smail.toulouse.utility.Logcat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import android.location.LocationManager
import android.net.Uri
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Toast
import smaillns.smail.toulouse.App
import smaillns.smail.toulouse.utility.LocaleHelper
import smaillns.smail.toulouse.utility.Preferences
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.analytics.GoogleAnalytics
import smaillns.smail.toulouse.fragment.GalleryVideoFragment


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnSearchListener {


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

    val VIDEO_GALLERY = 100


    /*language choices*/
    lateinit var choice1 : MenuItem
    lateinit var choice2 : MenuItem

    var cate : Int = 0

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(base))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        val preferences = Preferences(this)
        val context = LocaleHelper.setLocale(this, preferences.language)
        super.onCreate(savedInstanceState)

        //statusCheck()   // to enbale GPS

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        fab.setOnClickListener { view ->
            super.onSearchRequested()

//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
        }

        fab.visibility = View.GONE
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)


        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()


        val  listFragment = SpotListFragment()
        fragmentTransaction.replace(R.id._placeholder, listFragment)
        fragmentTransaction.commit()

        setTitle("")


        // init analytics tracker
        (application as App).getTracker()

        //
        MobileAds.initialize(this, "ca-app-pub-5514746004569529~8788212477")







//
//        // Verify the action and get the query
//        if (Intent.ACTION_SEARCH == intent.action) {
//            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
//                //doMySearch(query)
//            }
//        } else if(Intent.ACTION_VIEW == intent.action) {
//
//        }






    }

    fun doSearch(query : String){


    }

    private fun handleSearch() {
        val intent = intent
        if (Intent.ACTION_SEARCH == intent.action) {
            val searchQuery = intent.getStringExtra(SearchManager.QUERY)
            doSearch(searchQuery)
//            val adapter = CustomSearchAdapter(this,
//                    android.R.layout.simple_dropdown_item_1line,
//                    StoresData.filterData(searchQuery))
//            listView.setAdapter(adapter)


        } else if (Intent.ACTION_VIEW == intent.action) {
            val selectedSuggestionRowId = intent.dataString
            //execution comes here when an item is selected from search suggestions
            //you can continue from here with user selected search item
            Toast.makeText(this, "selected search suggestion " + selectedSuggestionRowId!!,
                    Toast.LENGTH_SHORT).show()
            finish()
        }
    }




    override fun onNewIntent(intent: Intent) {
        setIntent(intent)
        handleSearch()
    }


    override fun onStart() {
        super.onStart()

        // analytics
        GoogleAnalytics.getInstance(this).reportActivityStart(this)
    }

    override fun onSearchRequested(): Boolean {
//        pauseSomeStuff()
        return super.onSearchRequested()
    }




    override fun onBackPressed() {

        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            if (cate != CATEGORY_ID_ALL) {
                if (cate == VIDEO_GALLERY) {
                    val fragment = supportFragmentManager.findFragmentByTag("GALLERY_VIDEO_FRAGMENT") as GalleryVideoFragment
                    if (!fragment.playingVideo){
                        cate = CATEGORY_ID_ALL
                        val fragmentManager = supportFragmentManager
                        val fragmentTransaction = fragmentManager.beginTransaction()

                        val  listFragment = SpotListFragment.newInstance(CATEGORY_ID_ALL)
                        fragmentTransaction.replace(R.id._placeholder, listFragment).commitAllowingStateLoss()
                        setTitle(null)
                        tv_header_title.visibility = View.VISIBLE
                    } else{
                        val f_to = fragment.childFragmentManager.findFragmentById(R.id.video_placeholder2)
                        fragment.childFragmentManager.beginTransaction().apply {
                            remove(f_to)
                            commit()
                        }
                        fragment.playingVideo = false
                    }

                }else{
                    //TODO duplicated code
                    cate = CATEGORY_ID_ALL
                    val fragmentManager = supportFragmentManager
                    val fragmentTransaction = fragmentManager.beginTransaction()

                    val  listFragment = SpotListFragment.newInstance(CATEGORY_ID_ALL)
                    fragmentTransaction.replace(R.id._placeholder, listFragment).commitAllowingStateLoss()
                    setTitle(null)
                    tv_header_title.visibility = View.VISIBLE

                }

            }
            else
                super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        // retrieve the searchview here
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
//        val searchView = menu.findItem(R.id.fab).actionView as SearchView
//        // Assumes current activity is the searchable activity
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
//        searchView.setIconifiedByDefault(false)

        choice1 = menu.findItem(R.id.choix_en)
        choice2 = menu.findItem(R.id.choix_fr)

        val preferences = Preferences(this)
        if(preferences.language.equals("fr")) {
            choice1.isEnabled = true
            choice2.isEnabled = false
        }
        else {
            choice1.isEnabled = false
            choice2.isEnabled = true
        }
        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_rate -> {
                startWebActivity(getString(R.string.app_store_uri, App.getContext()!!.getPackageName()))
                return true}
            R.id.action_about -> {
                //alert("Sorry, but this fonctionality is not implemented yet","Hi"){}.show()

                val inflater = layoutInflater
                val alertLayout = inflater.inflate(R.layout.about, null)
                //val validateBtn = alertLayout.findViewById<Button>(R.id.validateBtn)

                val alert = AlertDialog.Builder(this)
                //alert.setTitle("About")

                // this is set the view from XML inside AlertDialog
                alert.setView(alertLayout)
                // disallow cancel of AlertDialog on click of back button and outside touch
                alert.setCancelable(true)
                //alert.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> Toast.makeText(baseContext, "Cancel clicked", Toast.LENGTH_SHORT).show() })

                val dialog = alert.create()
                dialog.show()

                return true
            }
            R.id.choix_fr ->{
                updateViews("fr")
                return true
            }
            R.id.choix_en ->{
                updateViews("en")
                return true
            }
            R.id.action_map -> {
                startMapActivity()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun updateViews(languageCode: String) {
        /*we can remove these lines*/
        val context = LocaleHelper.setLocale(this, languageCode)
        val resources = context.resources
        /**/

        val preferences = Preferences(this)
        preferences.language = languageCode

        recreate()
    }


    override fun onResume(){
        if (cate ==CATEGORY_ID_FAVORITES)
            {
                val fragmentManager = supportFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()

                val  listFragment = SpotListFragment.newInstance(CATEGORY_ID_FAVORITES)
                fragmentTransaction.replace(R.id._placeholder, listFragment).commitAllowingStateLoss()
            }
        super.onResume()
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()


        when (item.itemId) {
            R.id.menu_all_spots -> {
                cate = CATEGORY_ID_ALL
                val  listFragment = SpotListFragment.newInstance(CATEGORY_ID_ALL)
                fragmentTransaction.replace(R.id._placeholder, listFragment).commitAllowingStateLoss()

                setTitle(null)
                tv_header_title.visibility = View.VISIBLE

            }
            R.id.menu_hotels -> {
                cate = CATEGORY_ID_HOTELS
                val  listFragment = SpotListFragment.newInstance(CATEGORY_ID_HOTELS)
                fragmentTransaction.replace(R.id._placeholder, listFragment).commitAllowingStateLoss()

                tv_header_title.visibility = View.GONE
                setTitle("Hotels")
            }
            R.id.menu_favortites -> {

                cate = CATEGORY_ID_FAVORITES

                val  listFragment = SpotListFragment.newInstance(CATEGORY_ID_FAVORITES)
                fragmentTransaction.replace(R.id._placeholder, listFragment).commitAllowingStateLoss()

                tv_header_title.visibility = View.GONE
                setTitle(R.string.action_favorites)

            }
            R.id.menu_museum -> {

                cate = CATEGORY_ID_MUSEUM

                val  listFragment = SpotListFragment.newInstance(CATEGORY_ID_MUSEUM)
                fragmentTransaction.replace(R.id._placeholder, listFragment).commitAllowingStateLoss()

                tv_header_title.visibility = View.GONE
                setTitle(R.string.action_museum)
            }
            R.id.menu_attraction -> {
                cate = CATEGORY_ID_ATTRACTION

                val  listFragment = SpotListFragment.newInstance(CATEGORY_ID_ATTRACTION)
                fragmentTransaction.replace(R.id._placeholder, listFragment).commitAllowingStateLoss()

                tv_header_title.visibility = View.GONE
                setTitle(R.string.action_attraction)
            }
            R.id.menu_beaches -> {
                cate = CATEGORY_ID_BEACHES

                val  listFragment = SpotListFragment.newInstance(CATEGORY_ID_BEACHES)
                fragmentTransaction.replace(R.id._placeholder, listFragment).commitAllowingStateLoss()

                tv_header_title.visibility = View.GONE
                setTitle(R.string.action_beaches)
            }
            R.id.menu_shopping -> {

                cate = CATEGORY_ID_SHOPPOING

                val  listFragment = SpotListFragment.newInstance(CATEGORY_ID_SHOPPOING)
                fragmentTransaction.replace(R.id._placeholder, listFragment).commitAllowingStateLoss()

                tv_header_title.visibility = View.GONE
                setTitle(R.string.action_shopping)
            }
            R.id.menu_restaurants -> {

                cate = CATEGORY_ID_RESTAURANT

                val  listFragment = SpotListFragment.newInstance(CATEGORY_ID_RESTAURANT)
                fragmentTransaction.replace(R.id._placeholder, listFragment).commitAllowingStateLoss()

                tv_header_title.visibility = View.GONE
                setTitle(R.string.action_food)
            }
            R.id.menu_sport -> {
                cate = CATEGORY_ID_SPORT

                val  listFragment = SpotListFragment.newInstance(CATEGORY_ID_SPORT)
                fragmentTransaction.replace(R.id._placeholder, listFragment).commitAllowingStateLoss()

                tv_header_title.visibility = View.GONE
                setTitle(R.string.action_sport)
            }
            R.id.menu_transport -> {

                cate = CATEGORY_ID_TRANSPORT

                val  listFragment = SpotListFragment.newInstance(CATEGORY_ID_TRANSPORT)
                fragmentTransaction.replace(R.id._placeholder, listFragment).commitAllowingStateLoss()

                tv_header_title.visibility = View.GONE
                setTitle(R.string.action_transport)

            }
            R.id.menu_other -> {

                cate = CATEGORY_ID_OTHER

                val  listFragment = SpotListFragment.newInstance(CATEGORY_ID_OTHER)
                fragmentTransaction.replace(R.id._placeholder, listFragment).commitAllowingStateLoss()

                tv_header_title.visibility = View.GONE
                setTitle(R.string.action_more)
            }
            R.id.menu_proximity -> {

                cate = CATEGORY_ID_PROXIMITY

                val  listFragment = SpotListFragment.newInstance(CATEGORY_ID_PROXIMITY)
                fragmentTransaction.replace(R.id._placeholder, listFragment).commitAllowingStateLoss()

                tv_header_title.visibility = View.GONE
                setTitle(R.string.action_proximite)
            }
            R.id.menu_gallery -> {
                cate = VIDEO_GALLERY
                val  listVideoFragment = GalleryVideoFragment.newInstance()
                fragmentTransaction.replace(R.id._placeholder, listVideoFragment, "GALLERY_VIDEO_FRAGMENT").commitAllowingStateLoss()

                tv_header_title.visibility = View.GONE
                setTitle(R.string.video_gallery)
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }


    override fun onSearch(query: String) {
        val fragment = SpotListFragment.newInstance(query)
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().replace(R.id._placeholder, fragment).commitAllowingStateLoss()

        Logcat.d("test","I am here in the search method")

//        mDrawerAdapter.setSelected(mDrawerAdapter.getRecyclerPositionByCategory(0))
        title = getString(R.string.title_search) + ": " + query
    }

    private fun startMapActivity() {
        val intent = MapActivity.newIntent(this)
        startActivity(intent)

    }

    fun startWebActivity(url:String?){
        try {
            val intent = Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            // can't start activity
        }

    }


    fun statusCheck(){
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        }

    }

    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.gps))
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id -> startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
                .setNegativeButton("No") { dialog, id -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }
}
