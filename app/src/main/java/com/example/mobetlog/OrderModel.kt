package com.example.mobetlog

import java.io.Serializable

/**
 * Data Model representing a row in the "orders" table.
 * Implements Serializable so you can pass it between Activities if needed.
 */
data class OrderModel(
    val orderId: Int,           // Matches: order_id (INTEGER)
    val buyerId: Int,           // Matches: buyer_id (INTEGER)
    val sellerId: Int,          // Matches: seller_id (INTEGER)
    val shippingAddress: String,// Matches: shipping_address (TEXT)
    val status: String,         // Matches: status (TEXT)
    val totalPrice: Double,     // Matches: total_price (REAL)
    val orderDate: String       // Matches: order_date (TEXT)
) : Serializable