package com.example.mobetlog

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Inventory_History : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: InventoryHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_history)

        BottomNavHelper.setup(this, BottomNavHelper.NavItem.INVENTORY)

        dbHelper = DatabaseHelper(this)
        recyclerView = findViewById(R.id.recyclerHistory)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val btnClear: Button = findViewById(R.id.btnClearLogs)

        loadLogs()

        btnClear.setOnClickListener {
            clearLogs()
        }
    }

    private fun getCurrentUserId(): Int {
        val prefs = getSharedPreferences("mobet_prefs", MODE_PRIVATE)
        return prefs.getInt("USER_ID", -1)
    }

    private fun loadLogs() {
        val userId = getCurrentUserId()
        if (userId == -1) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val logs = dbHelper.getInventoryLogsForUser(userId)
        adapter = InventoryHistoryAdapter(logs)
        recyclerView.adapter = adapter
    }

    private fun reloadLogs() {
        val userId = getCurrentUserId()
        if (userId == -1) return
        val logs = dbHelper.getInventoryLogsForUser(userId)
        adapter.updateData(logs)
    }

    private fun clearLogs() {
        val userId = getCurrentUserId()
        if (userId == -1) return

        val cleared = dbHelper.clearInventoryLogsForUser(userId)
        if (cleared) {
            Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show()
            reloadLogs()
        } else {
            Toast.makeText(this, "No logs to clear", Toast.LENGTH_SHORT).show()
        }
    }
}
