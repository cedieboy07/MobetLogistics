package com.example.mobetlog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Login : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        dbHelper = DatabaseHelper(this)

        // 1. Bind Views (Using the IDs from your XML)
        val etEmail = findViewById<EditText>(R.id.editTextTextEmailAddress)
        val etPassword = findViewById<EditText>(R.id.editTextTextPassword)
        val btnLogin = findViewById<Button>(R.id.button) // Your Login Button
        val tvSignUp = findViewById<TextView>(R.id.textView7) // Your "Sign Up" text
        val tvForgot = findViewById<TextView>(R.id.textView6) // Forgot Password
        val chkRemember = findViewById<CheckBox>(R.id.checkBox)

        // 2. Login Button Logic
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            } else {
                // Check Database
                val isValid = dbHelper.checkUser(email, password)

                if (isValid) {
                    // Get user_id and save it for later screens
                    val userId = dbHelper.getUserId(email, password)

                    if (userId != null) {
                        val prefs = getSharedPreferences("mobet_prefs", MODE_PRIVATE)
                        prefs.edit()
                            .putInt("USER_ID", userId)
                            .putString("USER_EMAIL", email)
                            .apply()
                    }

                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, Inventory_Dashboard::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show()
                }
            }
        }


        // 3. Sign Up Navigation
        tvSignUp.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        // 4. Forgot Password (Optional - just a toast for now)
        tvForgot.setOnClickListener {
            Toast.makeText(this, "Feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }
}