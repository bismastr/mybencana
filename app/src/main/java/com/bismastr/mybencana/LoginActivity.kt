package com.bismastr.mybencana

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.bismastr.mybencana.data.user
import com.bismastr.mybencana.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()
        auth = Firebase.auth
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            signIn()
        }

    }

    private fun signIn() {
        when {
            TextUtils.isEmpty(binding.etUsername.text.toString().trim { it <= ' ' }) -> {
                Toast.makeText(
                    this,
                    "Please Enter Email.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            TextUtils.isEmpty(binding.etPassword.text.toString().trim { it <= ' ' }) -> {
                Toast.makeText(
                    this,
                    "Please Enter Password.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                val email: String = binding.etUsername.text.toString().trim { it <= ' ' }
                val password: String = binding.etPassword.text.toString().trim { it <= ' ' }

                //Firebase
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userLogin = auth.currentUser
                            Toast.makeText(
                                this,
                                "Login Successfull",
                                Toast.LENGTH_SHORT
                            ).show()
                            intent(userLogin)
                        } else {
                            Toast.makeText(
                                this,
                                "Login Failed, Check Your Connection",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }

    private fun intent(userLogin: FirebaseUser?) {
        if(userLogin != null){
            val userData = user(
                userLogin.email.toString(),
                userLogin.uid,
            )

            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("extra_user", userData)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

}