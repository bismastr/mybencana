package com.bismastr.mybencana

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bismastr.mybencana.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()
        binding.btnRegister.setOnClickListener {
            registerHelper()
        }

    }

    private fun registerHelper() {
        when {
            TextUtils.isEmpty(binding.etUsername.text.toString().trim { it <= ' ' }) -> {
                Toast.makeText(
                    this,
                    "Please Enter email.",
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
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnFailureListener { task ->
                        Toast.makeText(
                            this,
                            task.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val firebaseUser: FirebaseUser = task.result!!.user!!

                            Toast.makeText(
                                this,
                                "You are Registered Successfully",
                                Toast.LENGTH_SHORT
                            ).show()

                            val intent = Intent(this, MapsActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Register Failed, Check Your Connection",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }

    }
}