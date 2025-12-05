package com.example.mobetlog

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.ImageView
import android.view.View

class Profile : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    private lateinit var tvNickname: TextView
    private lateinit var tvContactLabel: TextView
    private lateinit var tvFirstName: TextView
    private lateinit var tvMiddleName: TextView
    private lateinit var tvLastName: TextView
    private lateinit var tvSex: TextView
    private lateinit var tvBirthdate: TextView
    private lateinit var tvAge: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvEmail: TextView

    private var currentProfile: UserProfileModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        BottomNavHelper.setup(this, BottomNavHelper.NavItem.PROFILE)

        dbHelper = DatabaseHelper(this)

        // Bind views
        tvNickname = findViewById(R.id.textView2)
        tvContactLabel = findViewById(R.id.textView12)
        tvFirstName = findViewById(R.id.valFirstName)
        tvMiddleName = findViewById(R.id.valMiddleName)
        tvLastName = findViewById(R.id.valLastName)
        tvSex = findViewById(R.id.valSex)
        tvBirthdate = findViewById(R.id.valBirthdate)
        tvAge = findViewById(R.id.valAge)
        tvAddress = findViewById(R.id.valAddress)
        tvEmail = findViewById(R.id.valEmail)

        val btnEdit: Button = findViewById(R.id.btnEditProfile)

        btnEdit.setOnClickListener {
            currentProfile?.let { showEditDialog(it) }
                ?: Toast.makeText(this, "No profile to edit", Toast.LENGTH_SHORT).show()
        }

        // Log Out button
        val btnLogout: Button = findViewById(R.id.button3)
        btnLogout.setOnClickListener {
            doLogout()
        }

        // Linked account icons
        val ivGoogle: ImageView = findViewById(R.id.imageView19)
        val ivYahoo: ImageView = findViewById(R.id.imageView21)
        val ivFacebook: ImageView = findViewById(R.id.imageView20)
        val ivLinkedIn: ImageView = findViewById(R.id.imageView22)

        // Reusable click listener for all of them
        val notAvailableClickListener = View.OnClickListener {
            Toast.makeText(this, "Not yet available!", Toast.LENGTH_SHORT).show()
        }

        ivGoogle.setOnClickListener(notAvailableClickListener)
        ivYahoo.setOnClickListener(notAvailableClickListener)
        ivFacebook.setOnClickListener(notAvailableClickListener)
        ivLinkedIn.setOnClickListener(notAvailableClickListener)

        loadProfile()
    }

    override fun onResume() {
        super.onResume()
        loadProfile()
    }

    private fun getCurrentUserId(): Int {
        val prefs = getSharedPreferences("mobet_prefs", MODE_PRIVATE)
        return prefs.getInt("USER_ID", -1)
    }

    private fun loadProfile() {
        val userId = getCurrentUserId()
        if (userId == -1) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val profile = dbHelper.getUserProfile(userId)
        if (profile == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            return
        }

        currentProfile = profile

        // Display basic info
        val nickname = when {
            !profile.shopName.isNullOrBlank() -> profile.shopName
            !profile.username.isNullOrBlank() -> profile.username
            else -> "Nickname"
        }

        tvNickname.text = nickname

        val contactText = profile.contact ?: "N/A"
        tvContactLabel.text = "$contactText"

        tvFirstName.text = profile.firstName ?: "..."
        tvMiddleName.text = profile.middleName ?: "..."
        tvLastName.text = profile.lastName ?: "..."
        tvSex.text = profile.sex ?: "..."
        tvBirthdate.text = profile.birthdate ?: "..."
        tvAge.text = profile.age?.toString() ?: "..."
        tvAddress.text = profile.address ?: "..."
        tvEmail.text = profile.email ?: "..."
    }

    private fun showEditDialog(profile: UserProfileModel) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)

        val etNickname = dialogView.findViewById<EditText>(R.id.etNickname)
        val etContact = dialogView.findViewById<EditText>(R.id.etContact)
        val etFirstName = dialogView.findViewById<EditText>(R.id.etFirstName)
        val etMiddleName = dialogView.findViewById<EditText>(R.id.etMiddleName)
        val etLastName = dialogView.findViewById<EditText>(R.id.etLastName)
        val etSex = dialogView.findViewById<EditText>(R.id.etSex)
        val etBirthdate = dialogView.findViewById<EditText>(R.id.etBirthdate)
        val etAge = dialogView.findViewById<EditText>(R.id.etAge)
        val etAddress = dialogView.findViewById<EditText>(R.id.etAddress)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)

        // Prefill with current values
        etNickname.setText(profile.shopName ?: profile.username)
        etContact.setText(profile.contact ?: "")
        etFirstName.setText(profile.firstName ?: "")
        etMiddleName.setText(profile.middleName ?: "")
        etLastName.setText(profile.lastName ?: "")
        etSex.setText(profile.sex ?: "")
        etBirthdate.setText(profile.birthdate ?: "")
        etAge.setText(profile.age?.toString() ?: "")
        etAddress.setText(profile.address ?: "")
        etEmail.setText(profile.email ?: "")

        AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val newProfile = profile.copy(
                    shopName = etNickname.text.toString(),
                    contact = etContact.text.toString(),
                    address = etAddress.text.toString(),
                    email = etEmail.text.toString(),
                    firstName = etFirstName.text.toString(),
                    middleName = etMiddleName.text.toString(),
                    lastName = etLastName.text.toString(),
                    sex = etSex.text.toString(),
                    birthdate = etBirthdate.text.toString(),
                    age = etAge.text.toString().toIntOrNull()
                )

                // Save to DB: basic stuff in USERS, extended in USER_PROFILE
                saveProfile(newProfile)
                loadProfile()
            }
            .create()
            .show()
    }

    private fun saveProfile(profile: UserProfileModel) {
        // Update USERS basic fields
        val db = dbHelper.writableDatabase
        val values = android.content.ContentValues().apply {
            put(DatabaseHelper.COL_SHOP_NAME, profile.shopName)
            put(DatabaseHelper.COL_CONTACT, profile.contact)
            put(DatabaseHelper.COL_USER_ADDRESS, profile.address)
            put(DatabaseHelper.COL_EMAIL, profile.email)
        }
        db.update(
            DatabaseHelper.TABLE_USERS,
            values,
            "${DatabaseHelper.COL_USER_ID} = ?",
            arrayOf(profile.userId.toString())
        )
        db.close()

        // Update extended fields in USER_PROFILE
        dbHelper.upsertUserProfile(profile)
    }

    private fun doLogout() {
        // Clear saved user session
        val prefs = getSharedPreferences("mobet_prefs", MODE_PRIVATE)
        prefs.edit()
            .remove("USER_ID")   // or .clear() if you want everything gone
            .apply()

        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()

        // Go back to Login screen
        val intent = Intent(this, Login::class.java) // <-- use your actual login activity class
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
