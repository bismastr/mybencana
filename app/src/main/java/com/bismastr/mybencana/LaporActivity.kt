package com.bismastr.mybencana

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bismastr.mybencana.databinding.ActivityLaporBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class LaporActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLaporBinding
    private lateinit var currentLat: String
    private lateinit var currentLong: String
    private lateinit var currentDate: String
    private lateinit var currentTime: String
    private var photoReference: String = "null"
    private var imageBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        binding = ActivityLaporBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()
        dropDownBencana()
        binding.btnLaporan.setOnClickListener {
            saveLaporan()
        }

        currentLong = intent.getStringExtra("EXTRA_CURRENT_LONG").toString()
        currentLat = intent.getStringExtra("EXTRA_CURRENT_LAT").toString()

        binding.imgGambar.setOnClickListener {
            dispatchTakePictureIntent()
        }

        getTime()
    }

    private fun dropDownBencana() {
        val bencanaDropdown = resources.getStringArray(R.array.bencana)
        val arrayAdapterView =
            ArrayAdapter(applicationContext, R.layout.dropdown_item, bencanaDropdown)
        binding.autoCompleteTextView.setAdapter(arrayAdapterView)
    }

    private fun uploadFirebase(
        deskripsi: String,
        title: String,
        lat: String,
        long: String,
        tipeBencana: String,
        dampak: String
    ) {
        uploadPhoto(imageBitmap)
        val db = FirebaseFirestore.getInstance()
        val laporan: MutableMap<String, Any> = HashMap()
        laporan["deskripsi"] = deskripsi
        laporan["title"] = title
        laporan["foto"] = photoReference
        laporan["tipeBencana"] = tipeBencana
        laporan["dampak"] = dampak
        laporan["latitude"] = lat
        laporan["longitude"] = long
        laporan["time"] = currentTime
        laporan["date"] = currentDate

        db.collection("laporan")
            .add(laporan)
            .addOnSuccessListener {
                Toast.makeText(this, "Complete added laporan", Toast.LENGTH_SHORT).show()
                uploadPhoto(imageBitmap)
            }.addOnFailureListener {
                Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
            }
    }

    private fun uploadPhoto(image: Bitmap?) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val randomKey = UUID.randomUUID().toString()
        photoReference = "images/$randomKey"
        val imageRef = storageRef.child(photoReference)
        val baos = ByteArrayOutputStream()
        image?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        imageRef.putBytes(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Uploaded", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MapsActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed Uploaded", Toast.LENGTH_SHORT).show()
            }

    }

    private fun getTime() {
        val c = Calendar.getInstance()
        val df = SimpleDateFormat("yyyy-MM-dd")
        val dt = SimpleDateFormat("HH:mm:ss")
        currentDate = df.format(c.time)
        currentTime = dt.format(c.time)
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imageBitmap = data?.extras?.get("data") as Bitmap
            binding.imgGambar.setImageBitmap(imageBitmap)
        }
    }

    private fun saveLaporan() {
        when {
            TextUtils.isEmpty(binding.etDeskripsi.text.toString().trim { it <= ' ' }) -> {
                Toast.makeText(
                    this,
                    "Please Enter Description",
                    Toast.LENGTH_SHORT
                ).show()
            }

            TextUtils.isEmpty(binding.etTitle.text.toString().trim { it <= ' ' }) -> {
                Toast.makeText(
                    this,
                    "Please Enter Title",
                    Toast.LENGTH_SHORT
                ).show()
            }

            TextUtils.isEmpty(binding.etDampak.text.toString().trim { it <= ' ' }) -> {
                Toast.makeText(
                    this,
                    "Please Enter Dampak",
                    Toast.LENGTH_SHORT
                ).show()
            }

            imageBitmap == null -> {
                Toast.makeText(
                    this,
                    "Please Input Photo",
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> {
                val title: String = binding.etTitle.text.toString()
                val deskripsi: String = binding.etDeskripsi.text.toString()
                val tipeBencana: String = binding.autoCompleteTextView.text.toString()
                val dampak: String = binding.etDampak.text.toString()
                uploadFirebase(deskripsi, title, currentLat, currentLong, tipeBencana, dampak)
            }
        }
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
    }
}