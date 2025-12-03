package com.example.mobetlog

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView



class OrdersActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrdersAdapter

    // UI Elements for Tabs
    private lateinit var tabPending: TextView
    private lateinit var tabHistory: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        // 1. Initialize Database Helper
        dbHelper = DatabaseHelper(this)

        // 2. Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerViewOrders)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 3. Setup Tabs (To switch between Pending and History)
            tabPending = findViewById(R.id.btnTabPending)
            tabHistory = findViewById(R.id.btnTabHistory)

        // Let's do a safer find approach based on your XML structure:
        val tabContainer = findViewById<android.view.ViewGroup>(R.id.tabContainer)
        tabPending = tabContainer.getChildAt(0) as TextView
        tabHistory = tabContainer.getChildAt(1) as TextView

        // 4. Load "Pending" orders by default when screen opens
        loadOrders("Pending")
        updateTabColors(isPendingSelected = true)

        // 5. Set Click Listeners for Tabs
        tabPending.setOnClickListener {
            loadOrders("Pending")
            updateTabColors(isPendingSelected = true)
        }

        tabHistory.setOnClickListener {
            // Assuming "Completed" or "History" is the status in your DB
            loadOrders("Completed")
            updateTabColors(isPendingSelected = false)
        }
    }

    private fun loadOrders(status: String) {
        // Fetch data from the database based on status
        val orderList = dbHelper.getOrdersByStatus(status)

        // Check if list is empty (Optional: Show a "No orders" text)
        if (orderList.isEmpty()) {
            // You could show a toast or text here
        }

        // Create adapter and attach to RecyclerView
        adapter = OrdersAdapter(orderList)
        recyclerView.adapter = adapter
    }

    private fun updateTabColors(isPendingSelected: Boolean) {
        val activeColor = ContextCompat.getColor(this, R.color.cyan) // Replace with your Cyan color
        val inactiveColor = ContextCompat.getColor(this, android.R.color.darker_gray)
        val white = ContextCompat.getColor(this, android.R.color.white)

        if (isPendingSelected) {
            // Pending is Active
            tabPending.setBackgroundColor(activeColor) // Use your Cyan Hex code if needed
            tabPending.setTextColor(white)

            tabHistory.setBackgroundColor(inactiveColor)
            tabHistory.setTextColor(activeColor) // Or black
        } else {
            // History is Active
            tabHistory.setBackgroundColor(activeColor)
            tabHistory.setTextColor(white)

            tabPending.setBackgroundColor(inactiveColor)
            tabPending.setTextColor(activeColor)
        }
    }
}