package com.bismastr.mybencana

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.bismastr.mybencana.data.user
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplashActivity : AppCompatActivity() {
    private val splashTimeout: Long = 2000
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(mainLooper).postDelayed({
            auth = Firebase.auth
            val currentUser = auth.currentUser
            if (currentUser != null){
                val i = Intent(this, MapsActivity::class.java)
                intent(currentUser, i)
            } else {
                val i = Intent(this, LoginActivity::class.java)
                intent(currentUser, i)
            }

        }, splashTimeout)
    }

    private fun intent(userLogin: FirebaseUser?, i: Intent) {
        if(userLogin != null) {
            val userData = user(
                userLogin.email.toString(),
                userLogin.uid,
            )

            i.putExtra("extra_user", userData)
            i.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
        } else{
            startActivity(i)
        }
    }
}