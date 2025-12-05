package com.example.mobetlog

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Orders_History : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_orders_history)

        BottomNavHelper.setup(this, BottomNavHelper.NavItem.ORDERS)

        dbHelper = DatabaseHelper(this)
        recyclerView = findViewById(R.id.recyclerViewOrdersHistory)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadHistoryOrders()
    }

    private fun loadHistoryOrders() {
        val prefs = getSharedPreferences("mobet_prefs", MODE_PRIVATE)
        val userId = prefs.getInt("USER_ID", -1)

        // Load only COMPLETED or ON DELIVERY orders
        val historyOrders = dbHelper.getOrdersForUserHistory(userId)

        recyclerView.adapter = OrdersHistoryAdapter(historyOrders, dbHelper)
    }
}
