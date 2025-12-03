package com.example.mobetlog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OrdersAdapter(private val orderList: List<OrderModel>) :
    RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_card, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val currentOrder = orderList[position]

        // 1. Bind Data to the IDs found in your XML
        holder.tvOrderId.text = "Order #${currentOrder.orderId}"
        holder.tvShippingAddress.text = currentOrder.shippingAddress
        holder.tvTotalPrice.text = "$ ${currentOrder.totalPrice}"

        // Optional: If your OrderModel has a list of items, you can set it here
        // holder.tvOrderItems.text = currentOrder.items

        // Optional: If your OrderModel has a buyer name
        // holder.tvBuyerName.text = currentOrder.buyerName

        // 2. Button Logic
        holder.btnPackaged.setOnClickListener {
            // Add update logic here
        }

        holder.btnCancel.setOnClickListener {
            // Add cancel logic here
        }
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    // 3. Update the ViewHolder to find the IDs that ACTUALLY exist in your XML
    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrderId: TextView = itemView.findViewById(R.id.tvOrderId)
        val tvShippingAddress: TextView = itemView.findViewById(R.id.tvShippingAddress)
        val tvTotalPrice: TextView = itemView.findViewById(R.id.tvTotalPrice)
        val btnPackaged: Button = itemView.findViewById(R.id.btnPackaged)
        val btnCancel: Button = itemView.findViewById(R.id.btnCancel)

        // You can also add these if you want to use them:
        // val tvBuyerName: TextView = itemView.findViewById(R.id.tvBuyerName)
        // val tvOrderItems: TextView = itemView.findViewById(R.id.tvOrderItems)
    }
}