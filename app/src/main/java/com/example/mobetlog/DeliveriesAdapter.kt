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

class DeliveriesAdapter(
    private val orders: MutableList<OrderModel>,
    private val dbHelper: DatabaseHelper,
    private val onStatusChanged: (() -> Unit)?
) : RecyclerView.Adapter<DeliveriesAdapter.DelViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_delivery_card, parent, false)
        return DelViewHolder(view)
    }

    override fun onBindViewHolder(holder: DelViewHolder, position: Int) {
        val order = orders[position]
        val context = holder.itemView.context

        holder.tvOrderId.text = "Order #${order.orderId}"
        holder.tvDelAddress.text = order.shippingAddress
        holder.tvStatus.text = "On Delivery"

        val prefs = context.getSharedPreferences("mobet_prefs", Context.MODE_PRIVATE)
        val currentUserId = prefs.getInt("USER_ID", -1)

        val isSeller = currentUserId == order.sellerId
        val isBuyer = currentUserId == order.buyerId

        // Show update button ONLY for seller
        holder.btnUpdateStatus.visibility =
            if (isSeller) View.VISIBLE else View.GONE

        holder.btnUpdateStatus.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Complete Delivery")
                .setMessage("Mark this order as Completed?")
                .setPositiveButton("Yes") { _, _ ->
                    val success = dbHelper.updateOrderStatus(
                        order.orderId, "Completed"
                    )

                    if (success) {
                        Toast.makeText(context, "Delivery completed", Toast.LENGTH_SHORT).show()
                        onStatusChanged?.invoke()
                    } else {
                        Toast.makeText(context, "Failed to update status", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    override fun getItemCount(): Int = orders.size

    class DelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrderId: TextView = itemView.findViewById(R.id.tvDelOrderId)
        val tvDelAddress: TextView = itemView.findViewById(R.id.tvDelAddress)
        val tvStatus: TextView = itemView.findViewById(R.id.tvDelStatus)
        val btnUpdateStatus: Button = itemView.findViewById(R.id.btnUpdateStatus)
    }
}
