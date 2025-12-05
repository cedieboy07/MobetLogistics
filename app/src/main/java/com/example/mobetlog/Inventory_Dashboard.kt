package com.example.mobetlog

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Inventory_Dashboard : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    private lateinit var tvProductCount: TextView
    private lateinit var tvCategoryCount: TextView
    private lateinit var tvRestockCount: TextView
    private lateinit var tvHistoryCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_dashboard)

        dbHelper = DatabaseHelper(this)

        // bottom nav
        BottomNavHelper.setup(this, BottomNavHelper.NavItem.INVENTORY)

        // Includes from the layout
        val cardProduct: View = findViewById(R.id.cardProduct)
        val cardCategory: View = findViewById(R.id.cardCategory)
        val cardRestock: View = findViewById(R.id.cardRestock)
        val cardHistory: View = findViewById(R.id.cardHistory)

        // TextViews inside each included card layout
        tvProductCount  = cardProduct.findViewById(R.id.restockNumDisplay)      // products_inventory_card.xml
        tvCategoryCount = cardCategory.findViewById(R.id.CategoryNumDisplay)    // category_inventory_card.xml
        tvRestockCount  = cardRestock.findViewById(R.id.restockNumDisplay)      // restock_inventory_card.xml
        tvHistoryCount  = cardHistory.findViewById(R.id.HistoryNumDisplay)      // history_inventory_card.xml

        // Click listeners
        cardProduct.setOnClickListener {
            startActivity(Intent(this, Inventory_Products::class.java))
        }
        cardCategory.setOnClickListener {
            startActivity(Intent(this, Inventory_Categories::class.java))
        }
        cardRestock.setOnClickListener {
            startActivity(Intent(this, Inventory_Restock::class.java))
        }
        cardHistory.setOnClickListener {
            startActivity(Intent(this, Inventory_History::class.java))
        }

        // First load
        updateDashboardCounts()
    }

    override fun onResume() {
        super.onResume()
        updateDashboardCounts()
    }

    private fun getCurrentUserId(): Int {
        val prefs = getSharedPreferences("mobet_prefs", MODE_PRIVATE)
        return prefs.getInt("USER_ID", -1)
    }

    private fun updateDashboardCounts() {
        val userId = getCurrentUserId()
        if (userId == -1) return   // no logged-in user

        // PRODUCTS: number of products for this user
        val productCount = dbHelper.getProductsForUser(userId).size

        // CATEGORIES: number of categories for this user
        val categoryCount = dbHelper.getCategoriesForUser(userId).size

        // RESTOCK: number of low-stock items
        val inventoryItems = dbHelper.getInventoryForUser(userId)
        val restockCount = inventoryItems.count { item ->
            item.stock <= item.threshold
        }

        // HISTORY: number of inventory history logs
        val historyCount = dbHelper.getInventoryLogsForUser(userId).size

        // Push values into UI
        tvProductCount.text  = productCount.toString()
        tvCategoryCount.text = categoryCount.toString()
        tvRestockCount.text  = restockCount.toString()
        tvHistoryCount.text  = historyCount.toString()
    }
}
