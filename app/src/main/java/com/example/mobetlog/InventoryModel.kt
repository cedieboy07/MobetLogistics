package com.example.mobetlog

data class InventoryModel(
    val invId: Int,
    val productId: Int,
    val productName: String,
    var stock: Int,
    var threshold: Int
)

