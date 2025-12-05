package com.example.mobetlog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignUp : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        dbHelper = DatabaseHelper(this)

        // 1. Bind Views (Based on your XML IDs)
        val etUsername = findViewById<EditText>(R.id.editTextText)
        val etEmail = findViewById<EditText>(R.id.editTextTextEmailAddress2)
        val etPassword = findViewById<EditText>(R.id.editTextTextPassword2)
        val etConfirmPass = findViewById<EditText>(R.id.editTextTextPassword3)
        val btnSignUp = findViewById<Button>(R.id.button2)

        // 2. Sign Up Button Logic
        btnSignUp.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            val confirmPass = etConfirmPass.text.toString().trim()

            // Validation Checks
            if (username.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else if (pass != confirmPass) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                // Save to Database
                val success = dbHelper.addUser(username, email, pass)

                if (success) {
                    Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}