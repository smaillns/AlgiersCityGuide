package com.example.smail.algiers_city_guide.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.*
import com.example.smail.algiers_city_guide.R
import com.example.smail.algiers_city_guide.activity.SpotDetailsActivity
import com.example.smail.algiers_city_guide.database.LocalStorage.RoomService
import com.example.smail.algiers_city_guide.database.RetrofitService
import com.example.smail.algiers_city_guide.entity.Spot
import com.example.smail.algiers_city_guide.listener.AnimateImageLoadingListener
import com.example.smail.algiers_city_guide.utility.LocationUtility
import com.example.smail.algiers_city_guide.utility.Logcat
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware
import com.nostra13.universalimageloader.core.imageaware.ImageAware




class SpotListAdapter(private var myDataset: ArrayList<Spot>,var mFooterList: List<Any>?, var mCtx: Context): RecyclerView.Adapter<SpotListAdapter.ViewHolder>() {


    private val VIEW_TYPE_POI = 1
    private val VIEW_TYPE_FOOTER = 2


    private var mGridSpanCount: Int = 0
    private var mAnimationEnabled = true
    private var mAnimationPosition = -1

    private var mImageLoader = ImageLoader.getInstance()
    lateinit var mDisplayImageOptions: DisplayImageOptions
    lateinit var mImageLoadingListener: ImageLoadingListener


    init {
        mImageLoader.init(ImageLoaderConfiguration.createDefault(mCtx))

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


    class ViewHolder(holder : View) : RecyclerView.ViewHolder(holder){

        val nom = holder.findViewById<TextView>(R.id.spot_list_item_name)
        var distance = holder.findViewById<TextView>(R.id.spot_list_item_distance)
        var imageView = holder.findViewById<ImageView>(R.id.fragment_spot_list_item_image)


    }



    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ViewHolder {
        // create a new view
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.spot_item, parent, false)
        // set the view's size, margins, paddings and layout parameters


        initFonts(v)


        return ViewHolder(v)
    }




    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element


        holder.nom.setText(myDataset[position].name)

//            holder.imageView.setImageResource(myDataset[position].image)
//        mImageLoader.displayImage("assets://"+myDataset[position].image, holder.imageView)
//        mImageLoader.displayImage(RetrofitService.baseUrl+"/"+myDataset[position].image, holder.imageView)
        val imageAware = ImageViewAware(holder.imageView, false)
        mImageLoader.displayImage(RetrofitService.baseUrl+"/"+myDataset[position].image, imageAware,mDisplayImageOptions, mImageLoadingListener)


//            mImageLoader.displayImage("assets://eiffeltower.png", holder.imageView, mDisplayImageOptions, mImageLoadingListener)

    if (myDataset[position].distance != 0){
        val distance = LocationUtility.getDistanceString(myDataset[position].distance, LocationUtility.isMetricSystem())
        holder.distance.setText(distance)
        holder.distance.visibility = View.VISIBLE
    }else
        holder.distance.visibility = View.GONE



        holder.itemView.setOnClickListener(
                { view ->
//                    Toast.makeText(mCtx, " Click Action ", Toast.LENGTH_SHORT).show()
                    val intent = Intent(mCtx,SpotDetailsActivity::class.java)

                    val extras = Bundle()
                    extras.putSerializable("spot",RoomService.appDataBase.getSpotDao().getSpot(myDataset[position].id))

                    intent.putExtras(extras)
                    mCtx.startActivity(intent)
                })


//        // set item margins
//        setItemMargins(holder.itemView, position)


        // set animation
        setAnimation(holder.itemView, position)

    }


    private fun setAnimation(view: View, position: Int) {
        if (mAnimationEnabled && position > mAnimationPosition) {
            Logcat.d("test","enter "+ position)
            view.scaleX = 0f
            view.scaleY = 0f
            view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(650).interpolator = DecelerateInterpolator()

            mAnimationPosition = position
        }



    }



    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size

    fun getRecyclerPositionByFooter(footerPosition: Int): Int {
        return footerPosition + getItemCount()
    }


    fun refill(spotList: ArrayList<Spot>, footerList: List<Any>) {
        myDataset = spotList
        mFooterList = footerList
        notifyDataSetChanged()
    }

    fun setAnimationEnabled(animationEnabled: Boolean) {
        mAnimationEnabled = animationEnabled
    }

    fun stop(){}



    fun initFonts(v:View){

        val tf = Typeface.createFromAsset(mCtx.getAssets(), "fonts/Roboto_Slab/RobotoSlab-Regular.ttf");


        val nom = v.findViewById<TextView>(R.id.spot_list_item_name)
        nom.setTypeface(tf)
        var distance = v.findViewById<TextView>(R.id.spot_list_item_distance)
        distance.setTypeface(tf)

    }
}