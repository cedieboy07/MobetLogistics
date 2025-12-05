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

        // Format the text to show ID, Price, and Address
        val displayText = "Order #${currentOrder.orderId}\n" +
                "Price: $${currentOrder.totalPrice}\n" +
                "Dest: ${currentOrder.shippingAddress}"

        holder.tvDetails.text = displayText

        // PACKAGED BUTTON LOGIC
        holder.btnPackaged.setOnClickListener {
            // Later we will add code here to update the 'status' column in SQL
        }
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDetails: TextView = itemView.findViewById(R.id.tvOrderDetails)
        val btnPackaged: Button = itemView.findViewById(R.id.btnPackaged)
        val btnCancel: Button = itemView.findViewById(R.id.btnCancel)
    }
}