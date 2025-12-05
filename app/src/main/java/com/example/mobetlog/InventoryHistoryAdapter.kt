package com.example.mobetlog

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class InventoryHistoryAdapter(
    private var logs: List<InventoryHistoryModel>
) : RecyclerView.Adapter<InventoryHistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inventory_log, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val log = logs[position]

        // NOTE: if your IDs are different in item_inventory_log.xml,
        // just change them in HistoryViewHolder below.
        holder.tvDate.text = log.timestamp
        holder.tvProduct.text = log.productName
        holder.tvAction.text = log.action

        val qtyText = if (log.qtyChange > 0) "+${log.qtyChange}" else log.qtyChange.toString()
        holder.tvQty.text = qtyText

        // Color code qty: green for +, red for -
        holder.tvQty.setTextColor(
            if (log.qtyChange >= 0) Color.parseColor("#008000") else Color.parseColor("#D32F2F")
        )
    }

    override fun getItemCount(): Int = logs.size

    fun updateData(newLogs: List<InventoryHistoryModel>) {
        logs = newLogs
        notifyDataSetChanged()
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // CHANGE THESE IDs if your XML uses different ones
        val tvDate: TextView = itemView.findViewById(R.id.tvLogDate)
        val tvProduct: TextView = itemView.findViewById(R.id.tvLogProductName)
        val tvAction: TextView = itemView.findViewById(R.id.tvLogType)
        val tvQty: TextView = itemView.findViewById(R.id.tvLogAmount)
    }
}
