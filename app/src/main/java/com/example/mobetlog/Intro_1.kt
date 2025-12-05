package com.example.mobetlog

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Intro_1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro_1)

        // Find the "Next" button (It is a TextView in your XML)
        val nextBtn = findViewById<TextView>(R.id.Nextarrow)

        // Set click listener to go to the next screen
        nextBtn.setOnClickListener {
            val intent = Intent(this, Intro_2::class.java)
            startActivity(intent)
        }
    }
}