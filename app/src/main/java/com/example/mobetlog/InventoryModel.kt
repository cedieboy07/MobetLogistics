package com.example.mobetlog

data class InventoryModel(
    val invId: Int,
    val productName: String,
    var stock: Int,
    var threshold: Int
)
