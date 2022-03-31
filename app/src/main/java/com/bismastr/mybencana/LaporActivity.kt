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
import java.util.*

class LaporActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLaporBinding
    private lateinit var currentLat: String
    private lateinit var currentLong: String
    private var photoReference: String = "null"
    private  var imageBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        binding = ActivityLaporBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        dropDownBencana()
        binding.btnLaporan.setOnClickListener {
            saveLaporan()
        }

        currentLong = intent.getStringExtra("EXTRA_CURRENT_LONG").toString()
        currentLat = intent.getStringExtra("EXTRA_CURRENT_LAT").toString()

        binding.imgGambar.setOnClickListener{
            dispatchTakePictureIntent()
        }
    }

    private fun dropDownBencana(){
        val bencanaDropdown = resources.getStringArray(R.array.bencana)
        val arrayAdapterView = ArrayAdapter(applicationContext, R.layout.dropdown_item, bencanaDropdown)
        binding.autoCompleteTextView.setAdapter(arrayAdapterView)
    }

    private fun uploadFirebase(deskripsi: String, title: String,lat: String, long: String, tipeBencana: String) {
        uploadPhoto(imageBitmap)
        val db = FirebaseFirestore.getInstance()
        val laporan: MutableMap<String, Any> = HashMap()
        laporan["deskripsi"] = deskripsi
        laporan["title"] = title
        laporan["foto"] = photoReference
        laporan["tipeBencana"] = tipeBencana
        laporan["latitude"] = lat
        laporan["longitude"] = long

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
            }
            .addOnFailureListener{
                Toast.makeText(this, "Failed Uploaded", Toast.LENGTH_SHORT).show()
            }
            .addOnProgressListener {
                Toast.makeText(this, "Uploading", Toast.LENGTH_SHORT).show()
            }

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

            imageBitmap == null -> {
                Toast.makeText(
                    this,
                    "Input Photo",
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> {
                val title: String = binding.etTitle.text.toString()
                val deskripsi: String = binding.etDeskripsi.text.toString()
                val tipeBencana: String = binding.autoCompleteTextView.text.toString()
                uploadFirebase(deskripsi,title, currentLat, currentLong, tipeBencana)

            }
        }
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
    }
}