package com.example.mobetlog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductAdapter(
    private var products: MutableList<ProductModel>,
    private val onEditClicked: (ProductModel) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_card, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val item = products[position]

        holder.tvProdId.text = "ID: ${item.productId}"
        holder.tvProdPrice.text = "Price: ${item.price.toInt()}" // or format properly
        holder.tvProdQty.text = "Qty: ${item.qty}"
        holder.tvProdCat.text = "Category: ${item.categoryName}"
        holder.tvProdName.text = item.name

        // Image is just placeholder for now (ic_launcher_foreground already in XML)

        holder.btnEdit.setOnClickListener {
            onEditClicked(item)
        }
    }

    override fun getItemCount(): Int = products.size

    fun updateData(newList: List<ProductModel>) {
        products = newList.toMutableList()
        notifyDataSetChanged()
    }

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        val tvProdId: TextView = itemView.findViewById(R.id.tvProdId)
        val tvProdPrice: TextView = itemView.findViewById(R.id.tvProdPrice)
        val tvProdQty: TextView = itemView.findViewById(R.id.tvProdQty)
        val tvProdCat: TextView = itemView.findViewById(R.id.tvProdCat)
        val tvProdName: TextView = itemView.findViewById(R.id.tvProdName)
        val btnEdit: Button = itemView.findViewById(R.id.btnEditItem)
    }
}
