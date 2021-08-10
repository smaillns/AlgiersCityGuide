package smaillns.smail.toulouse.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import smaillns.smail.toulouse.CityGuideConfig
import smaillns.smail.toulouse.R
import smaillns.smail.toulouse.adapter.GalleryVideoRecyclerViewAdapter
import smaillns.smail.toulouse.database.LocalStorage.RoomService
import smaillns.smail.toulouse.database.RetrofitService
import smaillns.smail.toulouse.entity.TestDB
import smaillns.smail.toulouse.entity.Video
import smaillns.smail.toulouse.utility.Logcat
import smaillns.smail.toulouse.utility.NetworkManager
import smaillns.smail.toulouse.view.GridSpacingItemDecoration
import smaillns.smail.toulouse.view.ViewState


class GalleryVideoFragment : Fragment() {

    private var mView: View? = null
    private var vAdapter: GalleryVideoRecyclerViewAdapter? = null

    private var mViewState: ViewState? = null
    private var mDataset = ArrayList<Video>()

    var playingVideo: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
//            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_gallery_video_item_list, container, false)
        setupRecyclerView()
        return mView
    }

    private fun getRecyclerView(): RecyclerView? {
        return if (mView != null) mView!!.findViewById<View>(R.id.list_video_recycler_view) as RecyclerView else null
    }

    private fun setupRecyclerView(){
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
        val cellWidth = resources.getDimension(R.dimen.fragment_video_list_recycler_item_size)
        return Math.round(screenWidth / cellWidth)
    }





    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // load and show data
        if (mViewState == null || mViewState === smaillns.smail.toulouse.view.ViewState.OFFLINE) {
            loadData()
            mViewState = ViewState.CONTENT
        }
        if (mViewState === smaillns.smail.toulouse.view.ViewState.CONTENT) {
            if (mDataset != null) renderView()
            showContent()
        }
        if (mViewState === smaillns.smail.toulouse.view.ViewState.PROGRESS) {
            showProgress()
        }
        if (mViewState === smaillns.smail.toulouse.view.ViewState.EMPTY) {
            showEmpty()
        }

    }



    private fun renderView() {
        val recyclerView = getRecyclerView()
        val adView = mView!!.findViewById<View>(R.id.fragment_spot_videos_adview) as AdView

        // content
        if (recyclerView!!.adapter == null) {
            // create adapter
            vAdapter = GalleryVideoRecyclerViewAdapter(mDataset, this.activity!!, this)
        } else {
            // refill adapter
            vAdapter!!.mValues = mDataset
            vAdapter!!.notifyDataSetChanged()
        }

        // set fixed size
        recyclerView.setHasFixedSize(false)

        // add decoration
        val itemDecoration = GridSpacingItemDecoration(resources.getDimensionPixelSize(R.dimen.fragment_video_list_recycler_item_padding))
        recyclerView.addItemDecoration(itemDecoration)

        // set animator
        recyclerView.itemAnimator = DefaultItemAnimator()

        // set adapter
        recyclerView.adapter = vAdapter

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

    private fun loadData(){
        showProgress()

        val list = RoomService.appDataBase.getVideoDao().getVideos()

        for (video in list){
                mDataset.add(video)
        }
        if (mDataset.size == 0){
            showProgress()
            loadDataFromServer()
        }else{
            showContent()
            renderView()



            val call = RetrofitService.endPoint.getNbrVideos()
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
                        if (nbr != mDataset.size) {
                            mDataset.clear()
                            RoomService.appDataBase.getVideoDao().nukeTable()
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

//        mDataset.add(Video(1, "Casbah of Algiers قصبة الجزائر", "Allaqta", "assets/alger.jpg", url = "aech79qqLEo"))
//                mDataset.add(Video(2, "Casbah of Algiers قصبة الجزائر", "Allaqta", "assets/alger.jpg", url = "https://youtu.be/aech79qqLEo?list=WL"))
//                        mDataset.add(Video(3, "Casbah of Algiers قصبة الجزائر", "Allaqta", "assets/alger.jpg", url = "https://youtu.be/aech79qqLEo?list=WL"))

    }


    fun loadDataFromServer() {
        val listVideo_ = mutableListOf<Video>()
        val call = RetrofitService.endPoint.getVideos()
        call.enqueue(object : Callback<ArrayList<Video>> {

            override fun onFailure(call: Call<ArrayList<Video>>?, t: Throwable?) {
                showOffline()
                Logcat.d("not internet connection")
            }

            override fun onResponse(call: Call<ArrayList<Video>>?, response: Response<ArrayList<Video>>?) {
                if (response?.isSuccessful!!) {
                    val list: ArrayList<Video> = response.body()!!

                    for (item in list) {
                        RoomService.appDataBase.getVideoDao().addVideo(item)
                        listVideo_.add(item)
                        mDataset.add(item)
                    }
                    showContent()
                    renderView()
                } else {
                    showEmpty()
                    Logcat.d(""+response.toString())
//                    Toast.makeText(getActivity()!!, response.toString(), Toast.LENGTH_SHORT).show()
                }
                //added 11/01/2020
                if(mDataset.size == 0)
                    showEmpty()

            }

        })
    }

    private fun showContent() {
        // show content container
        val containerContent = mView!!.findViewById<View>(R.id.container_content) as ViewGroup
        val containerProgress = mView!!.findViewById<View>(R.id.container_progress) as ViewGroup
        val containerOffline = mView!!.findViewById<View>(R.id.container_offline) as ViewGroup
        val containerEmpty = mView!!.findViewById<View>(R.id.container_empty) as ViewGroup
        containerContent.visibility = View.VISIBLE
        containerProgress.visibility = View.GONE
        containerOffline.visibility = View.GONE
        containerEmpty.visibility = View.GONE
        mViewState = ViewState.CONTENT
    }

    private fun showProgress() {
        // show progress container
        val containerContent = mView!!.findViewById<View>(R.id.container_content) as ViewGroup
        val containerProgress = mView!!.findViewById<View>(R.id.container_progress) as ViewGroup
        val containerOffline = mView!!.findViewById<View>(R.id.container_offline) as ViewGroup
        val containerEmpty = mView!!.findViewById<View>(R.id.container_empty) as ViewGroup
        containerContent.visibility = View.GONE
        containerProgress.visibility = View.VISIBLE
        containerOffline.visibility = View.GONE
        containerEmpty.visibility = View.GONE
        mViewState = ViewState.PROGRESS
    }

    private fun showOffline() {
        // show offline container
        val containerContent = mView!!.findViewById<View>(R.id.container_content) as ViewGroup
        val containerProgress = mView!!.findViewById<View>(R.id.container_progress) as ViewGroup
        val containerOffline = mView!!.findViewById<View>(R.id.container_offline) as ViewGroup
        val containerEmpty = mView!!.findViewById<View>(R.id.container_empty) as ViewGroup
        containerContent.visibility = View.GONE
        containerProgress.visibility = View.GONE
        containerOffline.visibility = View.VISIBLE
        containerEmpty.visibility = View.GONE
        mViewState = ViewState.OFFLINE
    }

    private fun showEmpty() {
        // show empty container
        val containerContent = mView!!.findViewById<View>(R.id.container_content) as ViewGroup
        val containerProgress = mView!!.findViewById<View>(R.id.container_progress) as ViewGroup
        val containerOffline = mView!!.findViewById<View>(R.id.container_offline) as ViewGroup
        val containerEmpty = mView!!.findViewById<View>(R.id.container_empty) as ViewGroup
        containerContent.visibility = View.GONE
        containerProgress.visibility = View.GONE
        containerOffline.visibility = View.GONE
        containerEmpty.visibility = View.VISIBLE
        mViewState = ViewState.EMPTY
    }

    companion object {

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance() =
                GalleryVideoFragment().apply {
                    arguments = Bundle().apply {
                    }
                }
    }

}
