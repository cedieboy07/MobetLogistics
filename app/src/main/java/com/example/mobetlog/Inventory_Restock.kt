package com.example.mobetlog

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Inventory_Restock : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RestockAdapter

    private var fullList: List<InventoryModel> = emptyList()
    private var currentFilterLowStock: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_restock)

        BottomNavHelper.setup(this, BottomNavHelper.NavItem.INVENTORY)

        dbHelper = DatabaseHelper(this)
        recyclerView = findViewById(R.id.recyclerRestock)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val btnAll: Button = findViewById(R.id.btnFilterAll)
        val btnLow: Button = findViewById(R.id.btnFilterLow)

        btnAll.setOnClickListener {
            currentFilterLowStock = false
            applyFilter()
        }

        btnLow.setOnClickListener {
            currentFilterLowStock = true
            applyFilter()
        }

        loadInventory()
    }

    private fun loadInventory() {
        val prefs = getSharedPreferences("mobet_prefs", MODE_PRIVATE)
        val userId = prefs.getInt("USER_ID", -1)

        fullList = if (userId != -1) {
            dbHelper.getInventoryForUser(userId)
        } else {
            dbHelper.getAllInventory()
        }

        adapter = RestockAdapter(
            fullList.toMutableList(),
            dbHelper,
            this
        ) {
            // callback from adapter when stock changes or order placed
            reloadAndReapplyFilter()
        }
        recyclerView.adapter = adapter

        currentFilterLowStock = false // default "All"
        applyFilter()
    }

    private fun reloadAndReapplyFilter() {
        val prefs = getSharedPreferences("mobet_prefs", MODE_PRIVATE)
        val userId = prefs.getInt("USER_ID", -1)

        fullList = if (userId != -1) {
            dbHelper.getInventoryForUser(userId)
        } else {
            dbHelper.getAllInventory()
        }

        applyFilter()
    }

    private fun applyFilter() {
        if (!::adapter.isInitialized) return

        val shown = if (currentFilterLowStock) {
            fullList.filter { it.stock <= it.threshold }
        } else {
            fullList
        }

        adapter.updateData(shown)
    }
}
