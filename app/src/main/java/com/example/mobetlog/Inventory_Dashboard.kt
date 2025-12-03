package com.example.mobetlog

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Inventory_Dashboard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_dashboard)

        // 1. Restock card
        val cardRestock = findViewById<View>(R.id.cardRestock)
        cardRestock.setOnClickListener {
            Toast.makeText(this, "Opening Restock...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, Inventory_Restock::class.java)
            startActivity(intent)
        }

        // 2. Bottom nav for this screen (Inventory tab highlighted)
        BottomNavHelper.setup(this, BottomNavHelper.NavItem.INVENTORY)
    }
}
