package com.example.mobetlog

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Intro_3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro_3)

        val nextBtn = findViewById<TextView>(R.id.Nextarrow)

        nextBtn.setOnClickListener {
            // GO TO LOGIN
            val intent = Intent(this, Login::class.java)
            startActivity(intent)

            finish()
        }
    }
}