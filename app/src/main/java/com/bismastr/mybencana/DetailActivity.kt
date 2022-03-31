package com.bismastr.mybencana

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bismastr.mybencana.databinding.ActivityDetailBinding
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class DetailActivity : AppCompatActivity() {

    private lateinit var  binding: ActivityDetailBinding
    private lateinit var markerId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        markerId = intent.getStringExtra("EXTRA_MARKER_ID").toString()

        getData()
    }

    private fun getData(){
        val db = Firebase.firestore
        val docRef =  db.collection("laporan").document(markerId)
        docRef
            .get()
            .addOnSuccessListener { document ->
                Log.d("MARKERID", markerId)
                    Log.d("MARKER", "${document.id} => ${document.data}")
                val deskripsi = document.get("deskripsi").toString()
                val title = document.get("title").toString()
                val foto = document.get("foto").toString()
                setData(deskripsi, title, foto)
            }
            .addOnFailureListener { exception ->
                Log.w("MARKER", "Error getting documents.", exception)
            }
    }

    private fun setData(deskripsi: String, title: String, foto: String) {
        binding.tvDeskripsi.text = deskripsi
        binding.tvTitle.text = title
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val pathReference = storageRef.child(foto)
        val localFile = File.createTempFile("images", "jpeg")
        pathReference.getFile(localFile)
            .addOnSuccessListener {
                Glide.with(applicationContext)
                    .load(localFile)
                    .centerCrop()
                    .into(binding.imgGambarLaporan)
            }

    }
}