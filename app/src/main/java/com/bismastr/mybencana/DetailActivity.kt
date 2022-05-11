package com.bismastr.mybencana

import android.content.Intent
import android.net.Uri
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
    private lateinit var latitude: String
    private lateinit var longitude: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        markerId = intent.getStringExtra("EXTRA_MARKER_ID").toString()
        binding.btnDirection.setOnClickListener {
            direction(latitude,longitude)
        }
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
                val tipeBencana = document.get("tipeBencana").toString()
                val dampak = document.get("dampak").toString()
                latitude = document.get("latitude").toString()
                longitude = document.get("longitude").toString()
                val time = document.get("time").toString()
                val date = document.get("date").toString()
                setData(deskripsi, title, foto,tipeBencana, dampak, latitude, longitude, time, date)
            }
            .addOnFailureListener { exception ->
                Log.w("MARKER", "Error getting documents.", exception)
            }
    }

    private fun setData(deskripsi: String, title: String, foto: String, tipeBencana: String, dampak: String, latitude: String, longitude: String, time: String, date: String) {
        val latlong = "$latitude, $longitude"
        val time = date + time
        binding.tvDeskripsi.text = deskripsi
        binding.tvTitle.text = title
        binding.tvLonglat.text = latlong
        binding.tvTipeBencana.text = tipeBencana
        binding.tvDampak.text = dampak
        binding.tvTime.text = time
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

    private fun direction(latitude: String, longitude: String){
        val psgPickupLocation = "$latitude, $longitude"
        val gmmIntentUri =
            Uri.parse("google.navigation:q=$psgPickupLocation")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }
}