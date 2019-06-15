package smaillns.smail.dzair.adapter

import android.content.Context
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import smaillns.smail.dzair.R
import smaillns.smail.dzair.database.RetrofitService
import smaillns.smail.dzair.listener.AnimateImageLoadingListener
import smaillns.smail.dzair.utility.Logcat
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener

class GalleryAdapter(private var myListImages: ArrayList<String>, var mCtx: Context): RecyclerView.Adapter<GalleryAdapter.ViewHolder>(){

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

        val image = holder.findViewById<ImageView>(R.id.image_gallery_)

    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ViewHolder {
        // create a new view
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.image_gallery_item, parent, false)
        // set the view's size, margins, paddings and layout parameters

        return ViewHolder(v)
    }



    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element


        val imageAware = ImageViewAware(holder.image, false)
        mImageLoader.displayImage(RetrofitService.baseUrl+"/"+myListImages[position], imageAware,mDisplayImageOptions, mImageLoadingListener)


        holder.itemView.setOnClickListener(
                { view ->
                    val inflater = LayoutInflater.from(mCtx)
                    val alertLayout = inflater.inflate(R.layout.image_gallery_dialog, null)


                    val alert = AlertDialog.Builder(mCtx)
                    //alert.setTitle("About")

                    // this is set the view from XML inside AlertDialog
                    alert.setView(alertLayout)
                    val image_gall = alertLayout.findViewById<ImageView>(R.id.imageView_gallery)
                    val imageAware2 = ImageViewAware(image_gall, false)
                    mImageLoader.displayImage(RetrofitService.baseUrl+"/"+myListImages[position], imageAware2,mDisplayImageOptions, mImageLoadingListener)


                    // disallow cancel of AlertDialog on click of back button and outside touch
                    alert.setCancelable(true)
                    //alert.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> Toast.makeText(baseContext, "Cancel clicked", Toast.LENGTH_SHORT).show() })

                    val dialog = alert.create()


                    dialog.show()
                    
                })


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
    override fun getItemCount() = myListImages.size

    fun getRecyclerPositionByFooter(footerPosition: Int): Int {
        return footerPosition + getItemCount()
    }


    fun refill(list: ArrayList<String>) {
        myListImages = list
        notifyDataSetChanged()
    }

    fun setAnimationEnabled(animationEnabled: Boolean) {
        mAnimationEnabled = animationEnabled
    }


}