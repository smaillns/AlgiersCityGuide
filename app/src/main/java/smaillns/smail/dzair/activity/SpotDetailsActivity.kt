package smaillns.smail.dzair.activity

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v7.app.AppCompatActivity
import android.view.View
import smaillns.smail.dzair.R
import smaillns.smail.dzair.entity.Spot
import kotlinx.android.synthetic.main.activity_spot_details_acitvity.*
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import smaillns.smail.dzair.App
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer
import org.jetbrains.anko.toast
import android.widget.ImageView
import smaillns.smail.dzair.database.LocalStorage.RoomService
import smaillns.smail.dzair.database.RetrofitService
import smaillns.smail.dzair.entity.Favorite
import smaillns.smail.dzair.kbv.KenBurnsView
import smaillns.smail.dzair.utility.LocaleHelper
import smaillns.smail.dzair.utility.Preferences
import com.github.ivbaranov.mfb.MaterialFavoriteButton
import com.google.android.gms.analytics.GoogleAnalytics
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware
import kotlinx.android.synthetic.main.fragment_spot_details.*
import smaillns.smail.dzair.utility.Logcat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class SpotDetailsActivity : AppCompatActivity() {


    lateinit var spot: Spot

    private var mImageLoader = ImageLoader.getInstance()
    lateinit var mDisplayImageOptions: DisplayImageOptions

    /*language choices*/
    lateinit var choice1 : MenuItem
    lateinit var choice2 : MenuItem

    companion object {


        fun newIntent(context: Context, spot: Spot): Intent {
            val intent = Intent(context, SpotDetailsActivity::class.java)

            // extras
            intent.putExtra("spot", spot)

            return intent
        }

    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(base))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        val preferences = Preferences(this)
        val context = LocaleHelper.setLocale(this, preferences.language)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spot_details_acitvity)
        setSupportActionBar(toolbar)


        handleArguments()

        initialize()



//        initFonts()
        if (backBtn != null)
            backBtn.setOnClickListener({
                onBackPressed()
            })
        else
            {
                setupActionBar()
if (backBtn2 != null)               backBtn2.setOnClickListener({
                    onBackPressed()
                })
            }


            if (app_bar != null)
                    app_bar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
                        internal var isShow = true
                        internal var scrollRange = -1

                        override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                            if (scrollRange == -1) {
                                scrollRange = appBarLayout.totalScrollRange
                            }
                            if (scrollRange + verticalOffset == 0) {
            //                    toolbar_layout.title = " "
                                toolbar_layout.title = spot.name

                                materialFavoriteButton.visibility = View.GONE
            //                    imageView3333.setVisibility(View.GONE)
            //                    linearLayoutttt.setVisibility(View.GONE)
                                isShow = true
                            } else if (isShow) {
                                toolbar_layout.title = " "//carefull there should a space between double quote otherwise it wont work
                                isShow = false
                                materialFavoriteButton.visibility = View.VISIBLE
            //                    imageView3333.setVisibility(View.VISIBLE)
            //                    linearLayoutttt.setVisibility(View.VISIBLE)
                            }
                        }
                    })

                val favs = RoomService.appDataBase.getFavoriteDao().getFavorites()
                for (item in favs){
                    if (item.id == spot.id)
                    {
                        if (item.fav == 1)
                            if (materialFavoriteButton != null)
                                materialFavoriteButton.setFavorite(true, true)
                            else
                                materialFavoriteButton2.setFavorite(true, true)
                        else
                            if (materialFavoriteButton != null )
                                materialFavoriteButton.setFavorite(false, false)
                            else
                                materialFavoriteButton2.setFavorite(false, false)
                        break;
                    }
                }

        if (materialFavoriteButton != null)
                materialFavoriteButton.setOnFavoriteChangeListener(
                        MaterialFavoriteButton.OnFavoriteChangeListener { buttonView, favorite ->
                            //
                            if (favorite)
                            {
                               /*val favs = RoomService.appDataBase.getFavoriteDao().getFavorites()
                               var fnd = false
                               for (item in favs){
                                   if (item.id == spot.id)
                                    {
                                        item.fav = 1
                                        fnd = true
                                        break;
                                    }
                               }
                                if (!fnd){
                                    RoomService.appDataBase.getFavoriteDao().addFavorite(Favorite(spot.id, 1))
                                }*/
                                RoomService.appDataBase.getFavoriteDao().addFavorite(Favorite(spot.id, 1))
                                toast(R.string.add_favorite)
                            }

                            else {
                                val favs = RoomService.appDataBase.getFavoriteDao().getFavorites()
                                for (item in favs){
                                    if (item.id == spot.id)
                                    {
                                        item.fav = 0
                                        //RoomService.appDataBase.getFavoriteDao().updateFavorite(item)
                                        RoomService.appDataBase.getFavoriteDao().deleteFavorite(item)
                                        break;
                                    }
                                }
                                toast(R.string.delete_favorite)
                            }

                        })

        else
                materialFavoriteButton2.setOnFavoriteChangeListener(
                        MaterialFavoriteButton.OnFavoriteChangeListener { buttonView, favorite ->
                            //
                            if (favorite)
                            {
                                /*var fnd = false
                                for (item in favs){
                                    if (item.id == spot.id)
                                    {
                                        item.fav = 1
                                        fnd = true
                                        break;
                                    }
                                }
                                if (!fnd){
                                    RoomService.appDataBase.getFavoriteDao().addFavorite(Favorite(spot.id, 1))
                                } */
                                RoomService.appDataBase.getFavoriteDao().addFavorite(Favorite(spot.id, 1))
                                toast(R.string.add_favorite)}

                            else {
                                val favs = RoomService.appDataBase.getFavoriteDao().getFavorites()
                                for (item in favs){
                                    if (item.id == spot.id)
                                    {
                                        item.fav = 0
                                        //RoomService.appDataBase.getFavoriteDao().updateFavorite(item)
                                        RoomService.appDataBase.getFavoriteDao().deleteFavorite(item)
                                        break;
                                    }
                                }
                                toast(R.string.delete_favorite)
                            }

                        })


        // init analytics tracker
        (application as App).getTracker()




//        val fragmentManager = supportFragmentManager
//        val fragmentTransaction = fragmentManager.beginTransaction()
//
//
//        val  listFragment = GalleryFragment()
//        fragmentTransaction.replace(R.id._placeholder_gallery, listFragment)
//        fragmentTransaction.commit()


    }


    override fun onStart() {
        super.onStart()

        // analytics
        GoogleAnalytics.getInstance(this).reportActivityStart(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_spot_details_acitvity, menu)

        choice1 = menu.findItem(R.id.choix_en2)
        choice2 = menu.findItem(R.id.choix_fr2)

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


    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so lng
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_rate -> {
                startWebActivity(getString(R.string.app_store_uri, App.getContext()!!.getPackageName()))
                return true }
            R.id.action_about -> {
               // alert("Sorry, but this fonctionality is not implemented yet","Hi"){}.show()
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
            R.id.choix_en2 -> {
                updateViews("en")
                return true
            }
            R.id.choix_fr2 -> {
                updateViews("fr")
                return true
            }
            R.id.action_share ->{
//                toast("Share functionality is not implemented yet !")
                checkForOtherPermissions()
                if (spot != null) {

//
//                    val shareIntent = Intent()
//                    shareIntent.action = Intent.ACTION_SEND
//                    shareIntent.type = "text/plain"
//                    shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
//                            "https://"+ spot.link + "\n"+
//                            "#AlgiersCityGuide  #DiscoverAlgiers  <3 <3")
////                        shareIntent.putExtra(Intent.EXTRA_TEXT, "text")
////                        shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri)
//
//                    startActivity(Intent.createChooser(shareIntent, getString(R.string.fragment_spot_detail_share_title)))

                    val imgview : ImageView
                    if (header_image_view != null) // Landescape Vs Normal display
                        imgview = findViewById<View>(R.id.header_image_view) as ImageView
                    else
                        imgview = findViewById<View>(R.id.header_image_view2) as ImageView


                    val bmpUri = getLocalBitmapUri(imgview)
                    if (bmpUri != null) {
                        // Construct a ShareIntent with link to image

                        startShareActivity(getString(R.string.fragment_spot_detail_share_subject),
                                getSpotText(), bmpUri)

                    } else {
                        // ...sharing failed, handle error
                    }
                }
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


    private fun startShareActivity(subject: String, text: String, pictureUri: Uri) {

        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "image/*"
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject)
            if (spot.link != null)
            intent.putExtra(android.content.Intent.EXTRA_TEXT, text + "https://"+ spot.link + "\n"+
                                                            "#AlgiersCityGuide  #DiscoverAlgiers  <3 <3")
            else
                intent.putExtra(android.content.Intent.EXTRA_TEXT, text + "\n"+
                        "#AlgiersCityGuide  #DiscoverAlgiers  <3 <3")
            intent.putExtra(Intent.EXTRA_STREAM, pictureUri)

            startActivity(Intent.createChooser(intent,getString(R.string.fragment_spot_detail_share_title)))
        } catch (e: android.content.ActivityNotFoundException) {
            // can't start activity
        }

    }

    /*fun setupFacebookShareIntent() {
        val shareDialog: ShareDialog
        FacebookSdk.sdkInitialize(applicationContext)
        shareDialog = ShareDialog(this)

        val linkContent = ShareLinkContent.Builder()
                .setContentTitle("Title")
                .setContentDescription(
                        "\"Body Of Test Post\"")
                .setContentUrl(Uri.parse("http://someurl.com/here"))
                .build()

        shareDialog.show(linkContent)
    }*/

    /*private fun startShareActivity(subject: String, text: String) {

        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject)
            intent.putExtra(android.content.Intent.EXTRA_TEXT, text)
            startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            // can't start activity
        }

    }*/



    private fun getSpotText(): String {
        val preferences = Preferences(this)
        val mLanguage_ = preferences.language

        val builder = StringBuilder()
        //builder.append(spot.name)
        if (spot.getIntro(mLanguage_) != null && !spot.getIntro(mLanguage_).trim().equals(""))
            builder.append(spot.getIntro(mLanguage_))
        builder.append("\n")
        if (spot.getAddress(mLanguage_) != null && !spot.getAddress(mLanguage_).trim().equals("")) {
            builder.append(spot.getAddress(mLanguage_))
            builder.append("\n\n")
        }

//        builder.append("#AlgiersCityGuide  #DiscoverAlgiers  <3 <3")
//        if (spot.link != null && !spot.link.trim().equals("")) {
//            builder.append(spot.link)
//        }
        return builder.toString()
    }

    fun handleArguments(){
        spot= intent.getSerializableExtra("spot") as Spot
    }


    fun initImageLoader() {

        mImageLoader.init(ImageLoaderConfiguration.createDefault(this))

        // image caching options
        mDisplayImageOptions = DisplayImageOptions.Builder()
                .showImageOnLoading(android.R.color.transparent)
                .showImageForEmptyUri(R.drawable.ic_location_on_black_24dp)
                .showImageOnFail(R.drawable.ic_location_on_black_24dp)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .displayer(SimpleBitmapDisplayer())
                .build()
    }



    fun initialize(){

        initImageLoader()


        if(header_image_view != null)
        {
            val mKenBurns = findViewById<View>(R.id.header_image_view) as KenBurnsView//we can delete this line and use id directly
//            mImageLoader.displayImage("assets://"+spot.image, mKenBurns)

            val imageAware = ImageViewAware(mKenBurns, false)
            mImageLoader.displayImage(RetrofitService.baseUrl+"/"+spot.image, imageAware,mDisplayImageOptions)
//            mImageLoader.displayImage(RetrofitService.baseUrl+"/"+spot.image, mKenBurns)
        }
        else
        {
            val mKenBurns2 = findViewById<View>(R.id.header_image_view2) as KenBurnsView
//            mImageLoader.displayImage("assets://"+spot.image, mKenBurns2)
            val imageAware = ImageViewAware(mKenBurns2, false)
            mImageLoader.displayImage(RetrofitService.baseUrl+"/"+spot.image, imageAware,mDisplayImageOptions)
//            mImageLoader.displayImage(RetrofitService.baseUrl+"/"+spot.image, mKenBurns2)

        }

           // mKenBurns.setImageResource(R.drawable.alger)

    }
    fun initFonts(){
        val tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto_Slab/RobotoSlab-Regular.ttf");
        toolbar_layout.setCollapsedTitleTypeface(tf);
        toolbar_layout.setExpandedTitleTypeface(tf);
    }


    fun startWebActivity(url:String?){
        try {
            val intent = Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            // can't start activity
        }

    }



    fun getLocalBitmapUri(imageView: ImageView):Uri? {
        val drawable = imageView.drawable
        var bmp: Bitmap? = null
        if (drawable is BitmapDrawable) {
            bmp = (imageView.drawable as BitmapDrawable).bitmap
        } else {
            return null
        }
        // Store image to default external storage directory
        var bmpUri: Uri? = null
        try {
            val file = File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png")
            file.parentFile.mkdirs()
            val out = FileOutputStream(file)
            bmp!!.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.close()
            bmpUri = Uri.fromFile(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return bmpUri
    }

    private fun setupActionBar() {
//        val toolbar = findViewById<Toolbar>(R.id.toolbar2) as Toolbar
//        setSupportActionBar(toolbar)

        val bar = supportActionBar
        //bar!!.setDisplayUseLogoEnabled(false)
if (bar != null)
        bar!!.setDisplayShowTitleEnabled(false)
        //bar!!.setDisplayShowHomeEnabled(true)
        //bar!!.setDisplayHomeAsUpEnabled(true)
        //bar!!.setHomeButtonEnabled(true)

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }


    fun checkForOtherPermissions(){
//        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.INTERNET)
//                != PackageManager.PERMISSION_GRANTED
//                && ContextCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_NETWORK_STATE)
//                != PackageManager.PERMISSION_GRANTED) {
//            // Permission is not granted
//            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!,
//                            Manifest.permission.ACCESS_FINE_LOCATION)
//                    || ActivityCompat.shouldShowRequestPermissionRationale(activity!!,
//                            Manifest.permission.ACCESS_NETWORK_STATE)) {
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//            } else {
//                // No explanation needed, we can request the permission.
//                ActivityCompat.requestPermissions(activity!!,
//                        arrayOf(Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE),
//                        3)
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                        4)

                Logcat.d("test","request fot permission")
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }


        }else {
            Logcat.d("test", "Permission is granted")
        }


    }


}
