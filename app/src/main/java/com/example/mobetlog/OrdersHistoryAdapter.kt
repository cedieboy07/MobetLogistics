package com.example.mobetlog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OrdersHistoryAdapter(
    private val orderList: List<OrderModel>,
    private val dbHelper: DatabaseHelper
) : RecyclerView.Adapter<OrdersHistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_history_card, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val order = orderList[position]

        holder.tvOrderId.text = "Order #${order.orderId}"
        holder.tvDate.text = order.orderDate
        holder.tvStatus.text = order.status
        holder.tvTotalPrice.text = String.format("$ %.2f", order.totalPrice)

        // items text (1x Product A, etc.)
        val itemsSummary = dbHelper.getOrderItemsSummary(order.orderId)
        holder.tvItems.text = itemsSummary.ifBlank { "No items" }

        // View details button – for now just a stub
//        holder.btnViewDetails.setOnClickListener {
            // TODO: open details screen later
//        }
    }

    override fun getItemCount(): Int = orderList.size

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrderId: TextView = itemView.findViewById(R.id.tvHistoryOrderId)
        val tvDate: TextView = itemView.findViewById(R.id.tvHistoryDate)
        val tvStatus: TextView = itemView.findViewById(R.id.tvHistoryStatus)
        val tvItems: TextView = itemView.findViewById(R.id.tvHistoryItems)
        val tvTotalPrice: TextView = itemView.findViewById(R.id.tvHistoryPrice)
    }
}
