package com.example.mobetlog

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Intro_2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro_2)

        val nextBtn = findViewById<TextView>(R.id.Nextarrow)

        nextBtn.setOnClickListener {
            val intent = Intent(this, Intro_3::class.java)
            startActivity(intent)
        }
    }
}