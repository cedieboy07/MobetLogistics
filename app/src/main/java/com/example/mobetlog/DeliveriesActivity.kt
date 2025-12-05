package com.example.mobetlog

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DeliveriesActivity : AppCompatActivity() {

    private lateinit var recyclerDeliveries: RecyclerView
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: DeliveriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deliveries)

        dbHelper = DatabaseHelper(this)
        recyclerDeliveries = findViewById(R.id.recyclerDeliveries)
        recyclerDeliveries.layoutManager = LinearLayoutManager(this)

        loadDeliveries()

        // Bottom nav handlers
        setupBottomNavigation()
    }

    private fun loadDeliveries() {
        val ordersOnDelivery = dbHelper.getOrdersByStatus("On Delivery")
        adapter = DeliveriesAdapter(ordersOnDelivery.toMutableList(), dbHelper) {
            loadDeliveries()
        }
        recyclerDeliveries.adapter = adapter
    }

    private fun setupBottomNavigation() {
        findViewById<android.view.View>(R.id.navOrders).setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
            finish()
        }
        findViewById<android.view.View>(R.id.navInventory).setOnClickListener {
            startActivity(Intent(this, Inventory_Dashboard::class.java))
            finish()
        }
        findViewById<android.view.View>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, Profile::class.java))
            finish()
        }
        findViewById<android.view.View>(R.id.navChat).setOnClickListener {
            // Not yet implemented
        }
    }
}
