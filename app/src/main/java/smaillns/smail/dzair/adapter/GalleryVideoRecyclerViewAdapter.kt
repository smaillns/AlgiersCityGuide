package smaillns.smail.dzair.adapter

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.common.util.WorkSourceUtil.add
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerView
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import smaillns.smail.dzair.R



import kotlinx.android.synthetic.main.fragment_video_item.view.*
import smaillns.smail.dzair.database.RetrofitService
import smaillns.smail.dzair.entity.Video
import smaillns.smail.dzair.listener.AnimateImageLoadingListener
import com.google.android.youtube.player.YouTubeInitializationResult
import smaillns.smail.dzair.activity.MainActivity
import smaillns.smail.dzair.fragment.GalleryVideoFragment
import smaillns.smail.dzair.fragment.SpotListFragment
import smaillns.smail.dzair.fragment.VideoPopUpFragment


class GalleryVideoRecyclerViewAdapter(var mValues: ArrayList<Video>, var mCtx: Context, var fragment: GalleryVideoFragment)
    : RecyclerView.Adapter<GalleryVideoRecyclerViewAdapter.ViewHolder>() {


    private var mImageLoader = ImageLoader.getInstance()
    var mDisplayImageOptions: DisplayImageOptions
    var mImageLoadingListener: ImageLoadingListener

    private lateinit var youtubePlayerView: YouTubePlayerView
    private lateinit var onInitializedListener: YouTubePlayer.OnInitializedListener


    init {
        mImageLoader.init(ImageLoaderConfiguration.createDefault(mCtx))

        // image caching options
        mDisplayImageOptions = DisplayImageOptions.Builder()
                .showImageOnLoading(android.R.color.transparent)
                .showImageForEmptyUri(R.drawable.ic_image_black_24dp)
                .showImageOnFail(R.drawable.ic_image_black_24dp)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .displayer(SimpleBitmapDisplayer())
                .build()
        mImageLoadingListener = AnimateImageLoadingListener()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_video_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]

        val imageAware = ImageViewAware(holder.videoMinature, false)
        mImageLoader.displayImage(RetrofitService.imageBaseUrl+"/Images/"+item.image, imageAware,mDisplayImageOptions, mImageLoadingListener)

        holder.mVideoTitle.setText(item.title)
        holder.mChannelName.setText(item.channelName)

        holder.itemView.setOnClickListener(
                { v ->
                    fragment.childFragmentManager.beginTransaction().apply {
                        add(R.id.video_placeholder2, VideoPopUpFragment.newInstance(item.url))
                        commit()
                    }
                    fragment.playingVideo = true
                })

    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mVideoTitle: TextView = mView.video_list_item_title
        val mChannelName: TextView = mView.video_channel_name
        val videoMinature: ImageView = mView.fragment_video_list_item_image

    }
}
