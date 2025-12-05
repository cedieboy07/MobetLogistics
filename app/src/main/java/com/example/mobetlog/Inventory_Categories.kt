package com.example.mobetlog

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import CategoryModel

class Inventory_Categories : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var adapter: CategoryAdapter
    private lateinit var recycler: RecyclerView

    private var categories: List<CategoryModel> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_categories)

        // Bottom nav – highlight Inventory
        BottomNavHelper.setup(this, BottomNavHelper.NavItem.INVENTORY)

        db = DatabaseHelper(this)
        recycler = findViewById(R.id.recyclerCategories)
        recycler.layoutManager = LinearLayoutManager(this)

        findViewById<Button>(R.id.btnAddCategory).setOnClickListener {
            showAddDialog()
        }

        findViewById<Button>(R.id.btnDeleteCategory).setOnClickListener {
            showDeleteDialog()
        }

        loadCategories()
    }

    private fun getCurrentUserId(): Int {
        val prefs = getSharedPreferences("mobet_prefs", MODE_PRIVATE)
        return prefs.getInt("USER_ID", -1)
    }

    private fun loadCategories() {
        val userId = getCurrentUserId()
        if (userId == -1) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
            return
        }

        categories = db.getCategoriesForUser(userId)
        adapter = CategoryAdapter(categories.toMutableList()) { category ->
            showEditDialog(category)
        }
        recycler.adapter = adapter
    }

    private fun refreshCategories() {
        val userId = getCurrentUserId()
        if (userId == -1) return

        categories = db.getCategoriesForUser(userId)
        adapter.updateData(categories)
    }

    private fun showAddDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)
        val etName: EditText = dialogView.findViewById(R.id.etCatName)
        val etDesc: EditText = dialogView.findViewById(R.id.etCatDesc)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Category")
            .setView(dialogView)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val name = etName.text.toString().trim()
            val desc = etDesc.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = getCurrentUserId()
            if (userId == -1) {
                Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                return@setOnClickListener
            }

            val ok = db.addCategoryForUser(userId, name, desc)
            if (ok) {
                Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show()
                refreshCategories()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Error adding category", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEditDialog(category: CategoryModel) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_category, null)
        val etName: EditText = dialogView.findViewById(R.id.etCatName)
        val etDesc: EditText = dialogView.findViewById(R.id.etCatDesc)

        etName.setText(category.name)
        etDesc.setText(category.desc)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Category")
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val name = etName.text.toString().trim()
            val desc = etDesc.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val ok = db.updateCategory(category.id, name, desc)
            if (ok) {
                Toast.makeText(this, "Category updated", Toast.LENGTH_SHORT).show()
                refreshCategories()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Error updating category", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_category, null)
        val etId: EditText = dialogView.findViewById(R.id.etDelCatId)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Delete Category")
            .setView(dialogView)
            .setPositiveButton("Delete", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val idStr = etId.text.toString().trim()
            val id = idStr.toIntOrNull()
            if (id == null) {
                Toast.makeText(this, "Enter a valid ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val ok = db.deleteCategory(id)
            if (ok) {
                Toast.makeText(this, "Category deleted", Toast.LENGTH_SHORT).show()
                refreshCategories()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Category not found", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
