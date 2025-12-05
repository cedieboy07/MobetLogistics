package com.example.mobetlog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import CategoryModel

class CategoryAdapter(
    private var categories: MutableList<CategoryModel>,
    private val onEditClicked: (CategoryModel) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_card, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = categories[position]

        holder.tvName.text = item.name
        holder.tvDesc.text = item.desc
        holder.tvCount.text = "${item.productCount} products"

        holder.btnEdit.setOnClickListener {
            onEditClicked(item)
        }
    }

    override fun getItemCount(): Int = categories.size

    fun updateData(newList: List<CategoryModel>) {
        categories = newList.toMutableList()
        notifyDataSetChanged()
    }

    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvCatName)
        val tvDesc: TextView = view.findViewById(R.id.tvCatDesc)
        val tvCount: TextView = view.findViewById(R.id.tvCatCount)
        val btnEdit: Button = view.findViewById(R.id.btnEditCategory)
    }
}
