package smaillns.smail.dzair

import android.app.Application
import android.content.Context
import smaillns.smail.dzair.database.LocalStorage.RoomService
import smaillns.smail.dzair.utility.LocaleHelper
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker
import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiscCache
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.utils.StorageUtils
import smaillns.smail.dzair.R
import java.io.IOException


class App : Application() {

    private var mTracker: Tracker? = null

    companion object {
        var mInstance : Context? = null

        fun getContext(): Context? {
            return mInstance
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "fr"))
    }

    override fun onCreate() {
        super.onCreate()
         mInstance = applicationContext
         RoomService.context = applicationContext


        try {
            Class.forName("android.os.AsyncTask")
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }


        // init image caching
        val cacheDir = StorageUtils.getCacheDirectory(applicationContext)
        cacheDir.mkdirs() // requires android.permission.WRITE_EXTERNAL_STORAGE

        try {
            val config = ImageLoaderConfiguration.Builder(applicationContext)
                    .threadPoolSize(3)
                    .threadPriority(Thread.NORM_PRIORITY - 2)
                    .memoryCache(UsingFreqLimitedMemoryCache(2 * 1024 * 1024))
                    .diskCache(LruDiscCache(cacheDir, HashCodeFileNameGenerator(), (32 * 1024 * 1024).toLong()))
                    .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                    .build()

            ImageLoader.getInstance().init(config)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun getInstance(): Context? {
        return mInstance
    }

    @Synchronized
    fun getTracker(): Tracker {
        if (mTracker == null) {
            val analytics = GoogleAnalytics.getInstance(this)
            analytics.setDryRun(!CityGuideConfig.ANALYTICS)
            mTracker = analytics.newTracker(R.xml.analytics_app_tracker)
        }
        return mTracker!!
    }


}