package com.example.mobetlog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class OrdersAdapter(
    private val orders: MutableList<OrderModel>,
    private val dbHelper: DatabaseHelper,
    private val onStatusChanged: (() -> Unit)? = null
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_card, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        val context = holder.itemView.context

        // Load buyer / seller info
        val buyerInfo = dbHelper.getUserProfile(order.buyerId)
        val sellerInfo = dbHelper.getUserProfile(order.sellerId)

        val prefs = context.getSharedPreferences("mobet_prefs", Context.MODE_PRIVATE)
        val currentUserId = prefs.getInt("USER_ID", -1)

        val isBuyer = currentUserId == order.buyerId
        val isSeller = currentUserId == order.sellerId

        // -------------------------
        // FILL BASIC ORDER DETAILS
        // -------------------------
        holder.tvOrderId.text = "Order #${order.orderId}"
        holder.tvShippingAddress.text = order.shippingAddress
        holder.tvTotalPrice.text = String.format("$ %.2f", order.totalPrice)

        // Items summary (ex: 1x Product A \n 2x Product B)
        val itemsSummary = dbHelper.getOrderItemsSummary(order.orderId)
        holder.tvOrderItems.text = itemsSummary.ifBlank { "No items" }

        // Dynamic Buyer/Seller Label
        if (isBuyer) {
            holder.tvBuyerLabel.text = "Seller:"
            holder.tvBuyerName.text =
                "${sellerInfo?.shopName ?: "Unknown"} (${sellerInfo?.username ?: ""})"
        } else {
            holder.tvBuyerLabel.text = "Buyer:"
            holder.tvBuyerName.text =
                "${buyerInfo?.shopName ?: "Unknown"} (${buyerInfo?.username ?: ""})"
        }

        // -------------------------
        // BUTTON VISIBILITY LOGIC
        // -------------------------
        if (order.status == "Pending") {
            when {
                isSeller -> {
                    // Seller can Cancel + Packaged
                    holder.btnPackaged.visibility = View.VISIBLE
                    holder.btnCancel.visibility = View.VISIBLE
                }
                isBuyer -> {
                    // Buyer: Cancel only
                    holder.btnPackaged.visibility = View.GONE
                    holder.btnCancel.visibility = View.VISIBLE
                }
                else -> {
                    holder.btnPackaged.visibility = View.GONE
                    holder.btnCancel.visibility = View.GONE
                }
            }
        } else {
            // Not pending -> No buttons at all
            holder.btnPackaged.visibility = View.GONE
            holder.btnCancel.visibility = View.GONE
        }

        // -------------------------
        // CANCEL BUTTON LOGIC
        // -------------------------
        holder.btnCancel.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Cancel Order")
                .setMessage("Are you sure you want to cancel this order?")
                .setPositiveButton("Yes") { _, _ ->
                    val success = dbHelper.updateOrderStatus(order.orderId, "Cancelled")
                    if (success) {
                        Toast.makeText(context, "Order cancelled", Toast.LENGTH_SHORT).show()
                        onStatusChanged?.invoke()
                    } else {
                        Toast.makeText(context, "Failed to cancel", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }

        // -------------------------
        // PACKAGED BUTTON LOGIC
        // -------------------------
        holder.btnPackaged.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Mark as Packaged")
                .setMessage("Move this order to Deliveries?")
                .setPositiveButton("Yes") { _, _ ->
                    val success = dbHelper.updateOrderStatus(order.orderId, "On Delivery")
                    if (success) {
                        Toast.makeText(context, "Order marked as On Delivery", Toast.LENGTH_SHORT).show()
                        onStatusChanged?.invoke()
                    } else {
                        Toast.makeText(context, "Failed to update order", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    override fun getItemCount(): Int = orders.size

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrderId: TextView = itemView.findViewById(R.id.tvOrderId)
        val tvBuyerLabel: TextView = itemView.findViewById(R.id.tvBuyerLabel)
        val tvBuyerName: TextView = itemView.findViewById(R.id.tvBuyerName)
        val tvShippingAddress: TextView = itemView.findViewById(R.id.tvShippingAddress)
        val tvOrderItems: TextView = itemView.findViewById(R.id.tvOrderItems)
        val tvTotalPrice: TextView = itemView.findViewById(R.id.tvTotalPrice)
        val btnPackaged: Button = itemView.findViewById(R.id.btnPackaged)
        val btnCancel: Button = itemView.findViewById(R.id.btnCancel)
    }
}
