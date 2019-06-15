package smaillns.smail.dzair.activity

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import smaillns.smail.dzair.App
import smaillns.smail.dzair.R
import smaillns.smail.dzair.fragment.MapFragment
import com.google.android.gms.analytics.GoogleAnalytics

class MapActivity : AppCompatActivity(){


    companion object {

        val EXTRA_SPOT_ID = "spot_id"
        val EXTRA_SPOT_LATITUDE = "spot_latitude"
        val EXTRA_SPOT_LONGITUDE = "spot_longitude"

        fun newIntent(context: Context): Intent {
            return Intent(context, MapActivity::class.java)
        }


        fun newIntent(context: Context, spotId: Int, spotLatitude: Double, spotLongitude: Double): Intent {
            val intent = Intent(context, MapActivity::class.java)

            // extras
            intent.putExtra(EXTRA_SPOT_ID, spotId)
            intent.putExtra(EXTRA_SPOT_LATITUDE, spotLatitude)
            intent.putExtra(EXTRA_SPOT_LONGITUDE, spotLongitude)

            return intent
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()


        val  mapFragment = MapFragment()
        fragmentTransaction.replace(R.id._placeholder2, mapFragment)
        fragmentTransaction.commit()


        setupActionBar()
        setTitle("Map")

//        actionBar.setDisplayHomeAsUpEnabled(true)


        // init analytics tracker
        (application as App).getTracker()

    }




    override fun onStart() {
        super.onStart()

        // analytics
        GoogleAnalytics.getInstance(this).reportActivityStart(this)
    }


    override fun onResume() {
        super.onResume()
    }


    override fun onPause() {
        super.onPause()
    }


    override fun onStop() {
        super.onStop()

        // analytics
        //GoogleAnalytics.getInstance(this).reportActivityStop(this)
    }


    override fun onDestroy() {
        super.onDestroy()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // action bar menu behaviour
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun setupActionBar() {
//        val toolbar = findViewById<Toolbar>(R.id.toolbar2) as Toolbar
//        setSupportActionBar(toolbar)

        val bar = supportActionBar
        bar!!.setDisplayUseLogoEnabled(false)
        bar!!.setDisplayShowTitleEnabled(true)
        bar!!.setDisplayShowHomeEnabled(true)
        bar!!.setDisplayHomeAsUpEnabled(true)
        bar!!.setHomeButtonEnabled(true)
    }



}
