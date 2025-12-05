package com.example.mobetlog

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

class RestockAdapter(
    private var inventoryList: MutableList<InventoryModel>,
    private val dbHelper: DatabaseHelper,
    private val context: Context,
    private val onDataChanged: (() -> Unit)? = null
) : RecyclerView.Adapter<RestockAdapter.RestockViewHolder>() {

    private fun getCurrentUserId(): Int {
        val prefs = context.getSharedPreferences("mobet_prefs", Context.MODE_PRIVATE)
        return prefs.getInt("USER_ID", -1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_restock_card, parent, false)
        return RestockViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestockViewHolder, position: Int) {
        val item = inventoryList[position]

        holder.tvName.text = item.productName
        holder.tvQty.text = item.stock.toString()
        holder.etAdd.setText("")

        applyStockColor(holder, item)

        // MANUAL UPDATE BUTTON
        holder.btnUpdate.setOnClickListener {
            val addStr = holder.etAdd.text.toString().trim()
            val addQty = addStr.toIntOrNull()

            if (addQty == null || addQty <= 0) {
                Toast.makeText(context, "Enter a valid quantity to add", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newStock = item.stock + addQty
            dbHelper.updateInventoryStock(item.invId, newStock)

            val userId = getCurrentUserId()
            if (userId != -1) {
                dbHelper.insertInventoryLog(
                    userId = userId,
                    productName = item.productName,
                    action = "Manual Restock",
                    qtyChange = addQty
                )
            }

            Toast.makeText(
                context,
                "Stock updated: +$addQty (now $newStock)",
                Toast.LENGTH_SHORT
            ).show()

            // Let Activity reload from DB (keeps filters accurate)
            onDataChanged?.invoke()
        }

        // ORDER STOCK BUTTON
        holder.btnOrder.setOnClickListener {
            showOrderDialog(item)
        }
    }

    private fun applyStockColor(holder: RestockViewHolder, item: InventoryModel) {
        if (item.stock <= item.threshold) {
            holder.tvQty.setTextColor(Color.parseColor("#D32F2F"))  // low stock = red
        } else {
            holder.tvQty.setTextColor(Color.parseColor("#0D3E51"))  // normal teal
        }
    }

    private fun showOrderDialog(item: InventoryModel) {
        val dialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_order_stock, null)

        val tvProductName = dialogView.findViewById<TextView>(R.id.tvOrderProductName)
        val spinnerSuppliers = dialogView.findViewById<Spinner>(R.id.spinnerSuppliers)
        val etOrderQty = dialogView.findViewById<EditText>(R.id.etOrderQty)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelOrder)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirmOrder)

        // ✅ GET BUYER ID FIRST BEFORE ANYTHING
        val prefs = context.getSharedPreferences("mobet_prefs", Context.MODE_PRIVATE)
        val buyerId = prefs.getInt("USER_ID", -1)

        if (buyerId == -1) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // ✅ NOW THIS WILL WORK
        val suppliersList = dbHelper.getAllCompaniesExcept(buyerId)

        // Convert list<Pair<Int, String>> into pure list<String> for spinner display
        val supplierNames = suppliersList.map { it.second }

        val spinnerAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            supplierNames
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSuppliers.adapter = spinnerAdapter

        tvProductName.text = item.productName

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnConfirm.setOnClickListener {

            val qtyStr = etOrderQty.text.toString().trim()
            val qty = qtyStr.toIntOrNull()

            if (qty == null || qty <= 0) {
                Toast.makeText(context, "Enter a valid quantity", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get selected SELLER ID
            val sellerIndex = spinnerSuppliers.selectedItemPosition
            val sellerId = suppliersList[sellerIndex].first

            val buyerProfile = dbHelper.getUserProfile(buyerId)
            val address = buyerProfile?.address ?: "Unknown Address"

            val orderId = dbHelper.createOrder(
                buyerId = buyerId,
                sellerId = sellerId,
                address = address,
                productId = item.productId,
                qty = qty
            )

            if (orderId > 0) {
                Toast.makeText(context, "Order placed successfully!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Order failed.", Toast.LENGTH_LONG).show()
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    override fun getItemCount(): Int = inventoryList.size

    fun updateData(newList: List<InventoryModel>) {
        inventoryList = newList.toMutableList()
        notifyDataSetChanged()
    }

    class RestockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvRestockName)
        val tvQty: TextView = itemView.findViewById(R.id.tvRestockQty)
        val etAdd: EditText = itemView.findViewById(R.id.etAddQty)
        val btnUpdate: Button = itemView.findViewById(R.id.btnConfirmRestock)
        val btnOrder: Button = itemView.findViewById(R.id.btnOrderStock)
    }
}
