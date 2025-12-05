package com.example.mobetlog

import android.app.AlertDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.AutoCompleteTextView

class Inventory_Products : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter

    private var fullList: List<ProductModel> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_products)

        BottomNavHelper.setup(this, BottomNavHelper.NavItem.INVENTORY)

        dbHelper = DatabaseHelper(this)
        recyclerView = findViewById(R.id.recyclerProducts)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val btnAdd: Button = findViewById(R.id.btnAddItem)
        val btnRemove: Button = findViewById(R.id.btnRemoveItem)

        btnAdd.setOnClickListener {
            showAddEditDialog(null) // create new
        }

        btnRemove.setOnClickListener {
            showDeleteDialog()
        }

        loadProducts()
    }

    private fun getCurrentUserId(): Int {
        val prefs = getSharedPreferences("mobet_prefs", MODE_PRIVATE)
        return prefs.getInt("USER_ID", -1)
    }

    private fun loadProducts() {
        val userId = getCurrentUserId()
        if (userId == -1) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
            return
        }

        fullList = dbHelper.getProductsForUser(userId)
        adapter = ProductAdapter(fullList.toMutableList()) { product ->
            showAddEditDialog(product) // edit existing
        }
        recyclerView.adapter = adapter
    }

    private fun refreshList() {
        val userId = getCurrentUserId()
        if (userId == -1) return
        fullList = dbHelper.getProductsForUser(userId)
        adapter.updateData(fullList)
    }

    private fun showAddEditDialog(existing: ProductModel?) {

        // ---- SELECT LAYOUT ----
        val layoutRes = if (existing == null) {
            R.layout.dialog_add_product    // Add product
        } else {
            R.layout.dialog_edit_product   // Edit product
        }
        val dialogView = layoutInflater.inflate(layoutRes, null)

        // ---- FIND ALL VIEWS ----
        val etName: EditText = dialogView.findViewById(R.id.etName)
        val etPrice: EditText = dialogView.findViewById(R.id.etPrice)
        val etQty: EditText = dialogView.findViewById(R.id.etQty)
        val spCategory = dialogView.findViewById<AutoCompleteTextView>(R.id.spCategory)

        // If editing, pre-fill text fields
        if (existing != null) {
            etName.setText(existing.name)
            etPrice.setText(existing.price.toString())
            etQty.setText(existing.qty.toString())
        }

        // ---- LOAD CATEGORIES ----
        val userId = getCurrentUserId()
        val categories = dbHelper.getCategoriesForUser(userId)
        val categoryNames = categories.map { it.name }

        // ---- SPINNER ADAPTER ----
        val catAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            categoryNames
        )
        spCategory.setAdapter(catAdapter)
        spCategory.setOnClickListener {
            spCategory.showDropDown()
        }

        spCategory.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                spCategory.showDropDown()
            }
        }

        // ---- PRE-SELECT CATEGORY WHEN EDITING ----
        if (existing != null) {
            val index = categoryNames.indexOf(existing.categoryName)
            if (index >= 0) {
                spCategory.setText(categoryNames[index], false)
            }
        }

        // ---- BUILD DIALOG ----
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .setPositiveButton(if (existing == null) "Add" else "Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        // ---- POSITIVE BUTTON ACTION ----
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {

            val name = etName.text.toString().trim()
            val price = etPrice.text.toString().trim().toDoubleOrNull()
            val qty = etQty.text.toString().trim().toIntOrNull()

            if (name.isEmpty() || price == null || qty == null) {
                Toast.makeText(this, "Fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get selected category ID from spinner
            val selectedName = spCategory.text.toString()
            val selectedIndex = categoryNames.indexOf(selectedName)
            if (selectedIndex == -1) {
                Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val selectedCategoryId = categories[selectedIndex].id

            // ---- SAVE TO DATABASE ----
            val ok = if (existing == null) {
                dbHelper.addProductForUser(userId, name, price, qty, selectedCategoryId)
            } else {
                dbHelper.updateProductForUser(
                    userId,
                    existing.productId,
                    name,
                    price,
                    qty,
                    selectedCategoryId
                )
            }

            if (ok) {
                Toast.makeText(
                    this,
                    if (existing == null) "Product added" else "Product updated",
                    Toast.LENGTH_SHORT
                ).show()
                refreshList()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Error saving product", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun showDeleteDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_product, null)

        val etId: EditText = dialogView.findViewById(R.id.etDelId)
        val btnCancel: Button = dialogView.findViewById(R.id.btnCancelDelete)
        val btnConfirm: Button = dialogView.findViewById(R.id.btnConfirmDelete)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnConfirm.setOnClickListener {
            val idStr = etId.text.toString().trim()
            val productId = idStr.toIntOrNull()

            if (productId == null) {
                Toast.makeText(this, "Enter a valid product ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = getCurrentUserId()
            if (userId == -1) {
                Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                return@setOnClickListener
            }

            val ok = dbHelper.deleteProductForUser(userId, productId)
            if (ok) {
                Toast.makeText(this, "Product deleted", Toast.LENGTH_SHORT).show()
                refreshList()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Product not found for this user", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}
