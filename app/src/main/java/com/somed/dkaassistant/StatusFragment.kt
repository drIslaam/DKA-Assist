package com.somed.dkaassistant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.somed.dkaassistant.databinding.FragmentStatusBinding
import android.animation.ObjectAnimator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.Animator
import android.annotation.SuppressLint
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.OvershootInterpolator
import android.view.animation.LinearInterpolator
import java.time.LocalTime
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class StatusFragment : Fragment() {
    private lateinit var binding: FragmentStatusBinding
  //  private lateinit var auth: FirebaseAuth

    private var authUsername: String? = "Islam Abdelrahim"
    private var isNotification = false
    private var notificationNumber: Int? = 0
    private var isNotificationNumberDisplayed = true
    private val paddingBetweenProfilePictureAndUsername = 35
    private var withdrawalDPForLinearlayoutOfGreetingAndUsername = 0

    private val welcomeAnimatorSet = AnimatorSet()

    private val notificationAnimatorSet = AnimatorSet()

    val networkOnlineAnimationSet = AnimatorSet()
    val networkOfflineAnimationSet = AnimatorSet()
    var isInternetIndicatorDisplayed = false

    // EVERY SINGLE ANIMATION HAS A DESCRIPTION, SO, WE STORE IT IN OBJECT TO BE READY FOR PLAY
    // ----------------------------------------------------------------------------------------
    // -------- PROFILE PICTURE AND USER NAME AND GREETING ANIMATION OBJECTS ------------------
    // ----------------------------------------------------------------------------------------

    // Animation Object for to Fading in the Profile Picture
    private lateinit var profilePictureFadeInAnimator: ObjectAnimator

    // Animation Object for Sliding the Linearlayout that hold Greeting and Username from beneath Profile Picture
    private lateinit var slidingToExposeGreetingAndUsernameAnimator: ObjectAnimator

    // Animation Object for Fading in the Username
    private lateinit var usernameFadeInAnimator: ObjectAnimator

    // Animation Object for Fading in the Greeting Message (e.g., Good morning)
    private lateinit var greetingFadeInAnimator: ObjectAnimator

    // Animation Object for Popping up the Indicator of Being Online
    private lateinit var onlineStatusIndicatorAnimatorX: ObjectAnimator
    private lateinit var onlineStatusIndicatorAnimatorY: ObjectAnimator

    // Animation Object for Popping up the Indicator of Being Offline
    private lateinit var offlineStatusIndicatorAnimatorX: ObjectAnimator
    private lateinit var offlineStatusIndicatorAnimatorY: ObjectAnimator

    // ----------------------------------------------------------------------------------------
    // -------------------- NOTIFICATION ANIMATION OBJECTS ------------------------------------
    // ----------------------------------------------------------------------------------------

    // Animation Objects for Shaking of Notification Icon and Return to Neutral position
    private lateinit var notificationIconSwingAnimator: ObjectAnimator
    private lateinit var notificationIconRestoreNeutralAnimator: ObjectAnimator

    // Animation Objects for Scaling down Notification Icon to Collapse and
    // For Scaling up to Normal Size as if Expanding
    private lateinit var notificationIconCollapseAnimatorX: ObjectAnimator
    private lateinit var notificationIconCollapseAnimatorY: ObjectAnimator
    private lateinit var notificationIconExpandAnimatorX: ObjectAnimator
    private lateinit var notificationIconExpandAnimatorY: ObjectAnimator

    // Animation Objects for Scaling down Notification Number to Collapse and
    // For Scaling up to Normal Size as if Expanding
    private lateinit var notificationNumberCollapseAnimatorX: ObjectAnimator
    private lateinit var notificationNumberCollapseAnimatorY: ObjectAnimator
    private lateinit var notificationNumberExpandAnimatorX: ObjectAnimator
    private lateinit var notificationNumberExpandAnimatorY: ObjectAnimator

    // -----------------------------------------------------------------------

    private lateinit var mediaPlayer: MediaPlayer

    private var handlerThread: HandlerThread? = null
    private var handler: Handler? = null
    private var checkNetwork: Runnable? = null
    var isOffline = false

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_status, container, false)

      //  auth = Firebase.auth
      //  val user = auth.currentUser
       // authUsername = user?.displayName
        binding.tvUserFullName.text = authUsername!!



        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(
            requireContext(),
            Uri.parse("android.resource://" + requireContext().packageName + "/" + R.raw.notification_sound)
        )
        mediaPlayer.prepare()

        binding.tvGreeting.text = getUserGreeting()

        

        // Create and start the HandlerThread
        handlerThread = HandlerThread("MyHandlerThread")
        handlerThread?.start()

        // Create a Handler associated with the HandlerThread's looper
        handlerThread?.looper?.let { looper ->
            handler = Handler(looper)
        }

        checkNetwork = object : Runnable {
            override fun run() {
                activity?.runOnUiThread {
                    // Get network state and Check if there is access to internet
                    if (isConnected(requireContext())) {
                        isOffline = false
                        if (!isInternetIndicatorDisplayed) {
                            networkOnlineAnimationSet.start()
                            isInternetIndicatorDisplayed = true
                        }
                    } else {
                        isOffline = true
                        if (isInternetIndicatorDisplayed) {
                            networkOfflineAnimationSet.start()
                            isInternetIndicatorDisplayed = false
                        }
                    }
                }

                // Check formal train Location every  seconds
                handler?.postDelayed(this, 1000)
            }
        }

        // Start runnable after 1 millisecond after launching the app
        handler?.postDelayed(checkNetwork as Runnable, 700)
        // endregion

        // Waiting for Profile Picture to be inflated
        binding.profilePictureLinearlayout.post {
            hideUsernameUnderTheProfilePictureForAnimation()
            createProfilePictureAndUsernameAndGreetingAnimatorObjects()
            createNotificationAnimatorObjects()

            notificationAnimatorSet.startDelay = 700

            // Start the profile picture fade-in animation first
            welcomeAnimatorSet.play(profilePictureFadeInAnimator)

            // Then play the username title move animation and profile picture fade-in animation together
            welcomeAnimatorSet.play(slidingToExposeGreetingAndUsernameAnimator)
                .with(usernameFadeInAnimator).with(greetingFadeInAnimator)

            if (isNotification) {
                //welcomeAnimatorSet.play(notificationIconCollapseAnimatorX).with(notificationIconCollapseAnimatorY)
                binding.notificationIcon.setImageResource(R.drawable.notification_exists_icon)
                binding.tvNumberOfNotifications.visibility = View.VISIBLE
                notificationAnimatorSet.play(notificationIconExpandAnimatorX)
                    .with(notificationIconExpandAnimatorY)

                notificationAnimatorSet.play(notificationIconRestoreNeutralAnimator)
                    .after(notificationIconSwingAnimator)


                if (isNotificationNumberDisplayed) {
                    notificationAnimatorSet.play(notificationNumberCollapseAnimatorX)
                        .with(notificationNumberCollapseAnimatorY)
                }
                isNotificationNumberDisplayed = true
                binding.tvNumberOfNotifications.text = "  $notificationNumber  "
                notificationAnimatorSet.play(notificationNumberExpandAnimatorX)
                    .with(notificationNumberExpandAnimatorY)

            } else {
                binding.notificationIcon.setImageResource(R.drawable.notification_not_exist_icon2)
                if (isNotificationNumberDisplayed) {
                    isNotificationNumberDisplayed = false
                    notificationAnimatorSet.play(notificationNumberCollapseAnimatorX)
                        .with(notificationNumberCollapseAnimatorY)
                    binding.tvNumberOfNotifications.visibility = View.GONE
                }
                notificationAnimatorSet.play(notificationIconExpandAnimatorX)
                    .with(notificationIconExpandAnimatorY)
            }

            // Set the listener for welcomeAnimatorSet
            welcomeAnimatorSet.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    // Animation start callback
                }

                override fun onAnimationEnd(animation: Animator) {
                    // Animation end callback
                }

                override fun onAnimationCancel(animation: Animator) {
                    // Animation cancel callback
                }

                override fun onAnimationRepeat(animation: Animator) {
                    // Animation repeat callback
                }
            })

            // Create a Handler and post a delayed message to play the media after 700 milliseconds
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                // Start playing the media
                if (isNotification){
                    mediaPlayer.start()
                }
            }, 700)

            // Start the animator set
            welcomeAnimatorSet.start()

            notificationAnimatorSet.start()
        }

        return binding.root
    }

    private fun hideUsernameUnderTheProfilePictureForAnimation() {
        // Convert pixels that return from measureWidth of Linearlayout to dp
        val dpOfDevice = requireContext().resources.displayMetrics.density

        // profilePictureLinearlayout is put over usernameAndGreetingLinearlayout
        // due to variability of username length between user,
        // usernameAndGreetingLinearlayout width is always more than profilePictureLinearlayout
        // We try to hide the part of usernameAndGreetingLinearlayout that exceed and appear beyond profilePictureLinearlayout
        // So, we first Calculate how much dp of usernameAndGreetingLinearlayout to be withdrawn
        val margin =
            (binding.usernameAndGreetingLinearlayout.measuredWidth / dpOfDevice) - (binding.profilePictureLinearlayout.measuredWidth / dpOfDevice)

        // Multiply by 5 to make sure it is always hidden and to avoid user used font variability
        withdrawalDPForLinearlayoutOfGreetingAndUsername = margin.toInt() * 5

        binding.usernameAndGreetingLinearlayout.layoutParams =
            (binding.usernameAndGreetingLinearlayout.layoutParams as ViewGroup.MarginLayoutParams).apply {
                // Xiaomi Device we have to subtract 13 to get the right edge of both usernameAndGreetingLinearlayout and profilePictureLinearlayout aligned
                // But for safety we move it back further to 30 

                leftMargin = -withdrawalDPForLinearlayoutOfGreetingAndUsername
            }
    }

    private fun createProfilePictureAndUsernameAndGreetingAnimatorObjects() {
        // Animation Object for to Fading in the Profile Picture
        profilePictureFadeInAnimator =
            ObjectAnimator.ofFloat(binding.userProfilePictureCircularImageView, "alpha", 0f, 1f)
        profilePictureFadeInAnimator.duration = 700
        profilePictureFadeInAnimator.interpolator = AccelerateDecelerateInterpolator()

        // Animation Object for Sliding the Linearlayout that hold Greetings and Username from beneath Profile Picture
        slidingToExposeGreetingAndUsernameAnimator = ObjectAnimator.ofFloat(
            binding.usernameAndGreetingLinearlayout,
            "translationX",
            0f,
            binding.profilePictureLinearlayout.width.toFloat() + withdrawalDPForLinearlayoutOfGreetingAndUsername.toFloat() + paddingBetweenProfilePictureAndUsername
        )
        slidingToExposeGreetingAndUsernameAnimator.duration = 1000
        slidingToExposeGreetingAndUsernameAnimator.interpolator =
            AnticipateOvershootInterpolator(1.01f)
        // slidingToExposeGreetingAndUsernameAnimator.startDelay = 0

        // Animation Object for Fading in the Username
        usernameFadeInAnimator = ObjectAnimator.ofFloat(binding.tvUserFullName, "alpha", 0f, 1f)
        usernameFadeInAnimator.duration = 500
        usernameFadeInAnimator.interpolator = AccelerateDecelerateInterpolator()

        // Animation Object for Fading in the Greeting Message
        greetingFadeInAnimator = ObjectAnimator.ofFloat(binding.tvGreeting, "alpha", 0f, 1f)
        greetingFadeInAnimator.duration = 1000
        greetingFadeInAnimator.interpolator = LinearInterpolator()

        // Animation Object for Popping up the Indicator of Being Online
        onlineStatusIndicatorAnimatorX =
            ObjectAnimator.ofFloat(binding.lnOnlineIndicator, "scaleX", 0f, 1f, 0.80f)
        onlineStatusIndicatorAnimatorX.duration = 1500
        onlineStatusIndicatorAnimatorX.interpolator = AccelerateDecelerateInterpolator()
        onlineStatusIndicatorAnimatorY =
            ObjectAnimator.ofFloat(binding.lnOnlineIndicator, "scaleY", 0f, 1f, 0.80f)
        onlineStatusIndicatorAnimatorY.duration = 1500
        onlineStatusIndicatorAnimatorY.interpolator = AccelerateDecelerateInterpolator()

        // Animation Object for Popping down the Indicator of Being Offline
        offlineStatusIndicatorAnimatorX =
            ObjectAnimator.ofFloat(binding.lnOnlineIndicator, "scaleX", 0.80f, 1f, 0f)
        offlineStatusIndicatorAnimatorX.duration = 1500
        offlineStatusIndicatorAnimatorX.interpolator = AccelerateDecelerateInterpolator()
        offlineStatusIndicatorAnimatorY =
            ObjectAnimator.ofFloat(binding.lnOnlineIndicator, "scaleY", 0.8f, 1f, 0f)
        offlineStatusIndicatorAnimatorY.duration = 1500
        offlineStatusIndicatorAnimatorY.interpolator = AccelerateDecelerateInterpolator()

        networkOnlineAnimationSet.play(onlineStatusIndicatorAnimatorX)
            .with(onlineStatusIndicatorAnimatorY)

        networkOfflineAnimationSet.play(offlineStatusIndicatorAnimatorX)
            .with(offlineStatusIndicatorAnimatorY)
    }

    private fun createNotificationAnimatorObjects() {
        // Animation Objects for Shaking of Notification Icon and Return to Neutral position
        notificationIconSwingAnimator =
            ObjectAnimator.ofFloat(binding.notificationIcon, "rotation", 10f, -10f)
        notificationIconSwingAnimator.interpolator = LinearInterpolator()
        notificationIconSwingAnimator.duration = 200  // Set the duration of each swing
        notificationIconSwingAnimator.repeatCount = 3 // Number of swings
        notificationIconSwingAnimator.repeatMode =
            ObjectAnimator.REVERSE // Reverse the animation on each swing
        notificationIconSwingAnimator.startDelay = 0

        notificationIconRestoreNeutralAnimator =
            ObjectAnimator.ofFloat(binding.notificationIcon, "rotation", 10f, 0f)
        notificationIconRestoreNeutralAnimator.interpolator = LinearInterpolator()
        notificationIconRestoreNeutralAnimator.duration = 200  // Set the duration of each swing

        // Animation Objects for Scaling down Notification Icon to Collapse and
        // For Scaling up to Normal Size as if Expanding
        notificationIconCollapseAnimatorX =
            ObjectAnimator.ofFloat(binding.notificationIcon, "scaleX", 1f, 0f)
        notificationIconCollapseAnimatorX.duration = 300
        notificationIconCollapseAnimatorX.interpolator = AccelerateDecelerateInterpolator()
        // notificationIconCollapseAnimatorX.startDelay = 0

        notificationIconCollapseAnimatorY =
            ObjectAnimator.ofFloat(binding.notificationIcon, "scaleY", 1f, 0f)
        notificationIconCollapseAnimatorY.duration = 300
        notificationIconCollapseAnimatorY.interpolator = AccelerateDecelerateInterpolator()
        // notificationIconCollapseAnimatorY.startDelay = 0

        notificationIconExpandAnimatorX =
            ObjectAnimator.ofFloat(binding.notificationIcon, "scaleX", 0f, 1f)
        notificationIconExpandAnimatorX.duration = 300
        notificationIconExpandAnimatorX.interpolator = AccelerateDecelerateInterpolator()
        // notificationIconExpandAnimatorX.startDelay = 0

        notificationIconExpandAnimatorY =
            ObjectAnimator.ofFloat(binding.notificationIcon, "scaleY", 0f, 1f)
        notificationIconExpandAnimatorY.duration = 300
        notificationIconExpandAnimatorY.interpolator = AccelerateDecelerateInterpolator()
        // notificationIconExpandAnimatorY.startDelay = 0

        // Animation Objects for Scaling down Notification Number to Collapse and
        // For Scaling up to Normal Size as if Expanding
        notificationNumberCollapseAnimatorX =
            ObjectAnimator.ofFloat(binding.tvNumberOfNotifications, "scaleX", 1f, 0.8f, 0f)
        notificationNumberCollapseAnimatorX.duration = 300
        notificationNumberCollapseAnimatorX.interpolator = OvershootInterpolator()
        // notificationNumberCollapseAnimatorX.startDelay = 0

        notificationNumberCollapseAnimatorY =
            ObjectAnimator.ofFloat(binding.tvNumberOfNotifications, "scaleY", 1f, 0.8f, 0f)
        notificationNumberCollapseAnimatorY.duration = 300
        notificationNumberCollapseAnimatorY.interpolator = OvershootInterpolator()
        // notificationNumberCollapseAnimatorY.startDelay = 0

        notificationNumberExpandAnimatorX =
            ObjectAnimator.ofFloat(binding.tvNumberOfNotifications, "scaleX", 0f, 1f, 0.80f)
        notificationNumberExpandAnimatorX.duration = 900
        notificationNumberExpandAnimatorX.interpolator = OvershootInterpolator()
        // notificationNumberExpandAnimatorX.startDelay = 0

        notificationNumberExpandAnimatorY =
            ObjectAnimator.ofFloat(binding.tvNumberOfNotifications, "scaleY", 0f, 1f, 0.80f)
        notificationNumberExpandAnimatorY.duration = 900
        notificationNumberExpandAnimatorY.interpolator = OvershootInterpolator()
        // notificationNumberExpandAnimatorY.startDelay = 0
    }

    private fun getUserGreeting(): String {
        // Get the current time
        val now = LocalTime.now()

        // Create a greeting based on the hour of the day
        val greeting = when (now.hour) {
            in 0..4 -> "Good Night"
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }

        // Set the text of the TextView to the greeting
        return greeting
    }
}