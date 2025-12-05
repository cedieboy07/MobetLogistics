package com.example.mobetlog

data class InventoryHistoryModel(
    val id: Int,
    val timestamp: String,
    val productName: String,
    val action: String,
    val qtyChange: Int
)
