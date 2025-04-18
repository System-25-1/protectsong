package com.example.protectsong

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.example.protectsong.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private var sampleNumber = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setDocument(
            FirebaseData(
                sampleName = "firstData",
                sampleNumber = sampleNumber,
                sampleBoolean = false

            )
        )
        binding.buttonSet.setOnClickListener {
            sampleNumber++
            setDocument(
                FirebaseData(
                    sampleName = "sampleData$sampleNumber",
                    sampleNumber = sampleNumber,
                    sampleBoolean = true
                )
            )
        }
    }

    private fun setDocument(data: FirebaseData) {
        FirebaseFirestore.getInstance()
            .collection("sampleCollection")
            .document(data.sampleName)
            .set(data)
            .addOnSuccessListener {
                binding.textResult.text = "success!"
            }
            .addOnFailureListener {
                binding.textResult.text = "fail!"
            }
    }
}