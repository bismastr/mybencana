package com.bismastr.mybencana

import android.content.ContentValues.TAG
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bismastr.mybencana.databinding.ActivityLaporBinding
import com.google.firebase.firestore.FirebaseFirestore

class LaporActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLaporBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnLaporan.setOnClickListener {
            saveLaporan()
        }


    }

    private fun uploadFirebase(deskripsi: String, title: String, foto: String) {
        val db = FirebaseFirestore.getInstance()
        val laporan: MutableMap<String, Any> = HashMap()
        laporan["deskripsi"] = deskripsi
        laporan["title"] = title
        laporan["foto"] = foto

        db.collection("laporan")
            .add(laporan)
            .addOnSuccessListener {
                Toast.makeText(this, "Complete added laporan", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
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

            TextUtils.isEmpty(binding.etFoto.text.toString().trim { it <= ' ' }) -> {
                Toast.makeText(
                    this,
                    "Please Enter Foto",
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> {
                val title: String = binding.etTitle.text.toString()
                val deskripsi: String = binding.etDeskripsi.text.toString()
                val foto: String = binding.etFoto.text.toString()
                uploadFirebase(deskripsi,title,foto)
            }
        }
    }
}