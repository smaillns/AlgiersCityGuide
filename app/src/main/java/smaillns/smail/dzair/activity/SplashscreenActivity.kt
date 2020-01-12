package smaillns.smail.dzair.activity

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import smaillns.smail.dzair.R
import smaillns.smail.dzair.kbv.KenBurnsView
import smaillns.smail.dzair.utility.LocaleHelper
import kotlinx.android.synthetic.main.activity_splashscreen.*
import java.util.*







/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class SplashscreenActivity : AppCompatActivity() {


    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(base))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splashscreen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        val mKenBurns = findViewById<View>(R.id.ken_burns_images) as KenBurnsView
//        mKenBurns.setImageResource(R.drawable.splash_screen_background)

        val rand = Random()
        val rdm = rand.nextInt(100)
        println(rdm)

        when(rdm){
            in 1..11 ->  mKenBurns.setImageResource(R.drawable.image1)
            in 12..38 ->  mKenBurns.setImageResource(R.drawable.image2)
            in 39..65 ->  mKenBurns.setImageResource(R.drawable.image3)
            in 66..84 ->  mKenBurns.setImageResource(R.drawable.image4)
            else   ->   mKenBurns.setImageResource(R.drawable.image5)

        }






        val mLogo = findViewById<View>(R.id.logo) as ImageView
        mLogo.setAlpha(1.0f)
        val anim = AnimationUtils.loadAnimation(this, R.anim.translate_top_to_center)
        mLogo.startAnimation(anim)

        val welcomeText = findViewById<View>(R.id.welcome_text) as TextView
        val alphaAnimation = ObjectAnimator.ofFloat(welcomeText, "alpha", 0.0f, 1.0f)
        alphaAnimation.startDelay = 1700
        alphaAnimation.duration = 500
        alphaAnimation.start()

        val intent = Intent(applicationContext, MainActivity::class.java)


        val context = LocaleHelper.setLocale(this, "fr");
        val resources = context.getResources();

//         Set up the user interaction to show tha main activity
        val loadingText = findViewById<View>(R.id.loadingTextView) as TextView
        loadingText.visibility = View.VISIBLE
        splash_screen_content2.setOnClickListener {

            loadingText.text = getString(R.string.loading)
            startActivity(intent)
            finish()
        }

    }


    private fun animation2() {
//        mLogo.setAlpha(1.0f)
        val anim = AnimationUtils.loadAnimation(this, R.anim.translate_top_to_center)
//        mLogo.startAnimation(anim)
    }

    private fun animation3() {
//        val alphaAnimation = ObjectAnimator.ofFloat(welcomeText, "alpha", 0.0f, 1.0f)
//        alphaAnimation.startDelay = 1700
//        alphaAnimation.duration = 500
//        alphaAnimation.start()
    }
}
