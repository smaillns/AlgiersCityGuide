package smaillns.smail.toulouse.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import smaillns.smail.toulouse.R
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerFragment
import com.google.android.youtube.player.YouTubeInitializationResult
import java.lang.Exception


class VideoPopUpFragment : Fragment() {

    companion object {

        @JvmStatic
        fun newInstance(videoUrl: String) =
                VideoPopUpFragment().apply {
                    arguments = Bundle().apply {
                        putString("url", videoUrl)
                    }
                }
    }


    private var videoView: YouTubePlayerFragment? = null
    private var listener: YouTubePlayer.OnInitializedListener? = null


    private lateinit var videoUrl: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            videoUrl = it.getString("url")
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_video_pop_up, container, false)

        videoView = activity!!.fragmentManager.findFragmentById(R.id.fragment_youtube) as YouTubePlayerFragment

        listener = object : YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(provider: YouTubePlayer.Provider, youTubePlayer: YouTubePlayer, wasRestored: Boolean) {
                if (!wasRestored)
                    youTubePlayer.cueVideo(videoUrl)
//                    youTubePlayer.loadVideo("aech79qqLEo")
//                youTubePlayer.setShowFullscreenButton(false)
            }

            override fun onInitializationFailure(provider: YouTubePlayer.Provider, youTubeInitializationResult: YouTubeInitializationResult) {

            }
        }

        try {
            videoView!!.initialize(getString(R.string.youtube_api_key), listener);
        }catch (e: Exception){
            Toast.makeText(activity!!,"Oups some problem occured!", Toast.LENGTH_LONG)
        }

        return v;

    }


    override fun onDestroyView() {
        super.onDestroyView()
        this.onDestroy()
            if (videoView != null) activity!!.fragmentManager.beginTransaction().remove(videoView).commitAllowingStateLoss()
    }


}
