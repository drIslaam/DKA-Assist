@file:Suppress("DEPRECATION")

package com.somed.dkaassistant

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.somed.dkaassistant.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // private lateinit var analytics: FirebaseAnalytics
    //private lateinit var mGoogleSignInClient: GoogleSignInClient
    //private lateinit var auth: FirebaseAuth
    private var _binding: ActivityMainBinding? = null



    private val binding: ActivityMainBinding
        get() = checkNotNull(_binding) { "Activity has been destroyed" }

    private val fragmentManager: FragmentManager by lazy { supportFragmentManager }
    private val statusFragment = StatusFragment()
    private val mainFragment = MainFragment()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate and get instance of binding
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the FirebaseAnalytics instance.
      //  analytics = Firebase.analytics

        // Make Status Bar Background White
        window.statusBarColor = Color.parseColor("#FBF9FA")
        // Make Status Bar Text Black
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.isAppearanceLightStatusBars = true
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // Inflate Profile Status Fragment
        fragmentManager.beginTransaction().replace(R.id.profile_fragment_container, statusFragment)
            .commit()

        // Inflate Profile Status Fragment
       fragmentManager.beginTransaction()
           .replace(R.id.main_fragment_container, mainFragment)
           .commit()

        // Inside onCreate() method
        // binding.logoutButton.setOnClickListener {
           // signOutAndStartSignInActivity()
        //}
    }

    
}