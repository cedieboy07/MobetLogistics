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

        // 🔹 Wire up bottom nav for Orders screen
        BottomNavHelper.setup(this, BottomNavHelper.NavItem.ORDERS)

        // 1. Initialize Database Helper
        dbHelper = DatabaseHelper(this)

        // 2. Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerViewOrders)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 3. Setup Tabs (To switch between Pending and History)
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
            loadOrders("History")
            updateTabColors(isPendingSelected = false)
        }
    }

    private fun loadOrders(status: String) {
        if (status == "Pending") {

            val orders = dbHelper.getOrdersByStatus("Pending")
            val adapter = OrdersAdapter(orders.toMutableList(), dbHelper) {
                loadOrders("Pending")   // callback reload
            }

            recyclerView.adapter = adapter // FIXED

        } else {

            val prefs = getSharedPreferences("mobet_prefs", MODE_PRIVATE)
            val currentUserId = prefs.getInt("USER_ID", -1)

            val historyOrders = dbHelper.getOrdersForUserHistory(currentUserId)
            val historyAdapter = OrdersHistoryAdapter(historyOrders, dbHelper)

            recyclerView.adapter = historyAdapter // FIXED
        }
    }


    private fun updateTabColors(isPendingSelected: Boolean) {
        val activeColor = ContextCompat.getColor(this, R.color.cyan)
        val inactiveColor = ContextCompat.getColor(this, android.R.color.darker_gray)
        val white = ContextCompat.getColor(this, android.R.color.white)

        if (isPendingSelected) {
            tabPending.setBackgroundColor(activeColor)
            tabPending.setTextColor(white)

            tabHistory.setBackgroundColor(inactiveColor)
            tabHistory.setTextColor(activeColor)
        } else {
            tabHistory.setBackgroundColor(activeColor)
            tabHistory.setTextColor(white)

            tabPending.setBackgroundColor(inactiveColor)
            tabPending.setTextColor(activeColor)
        }
    }
}
