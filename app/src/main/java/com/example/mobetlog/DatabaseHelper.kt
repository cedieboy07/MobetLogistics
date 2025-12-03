package com.example.mobetlog

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class DatabaseHelper(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "mobet_logistics.db"
        private const val DATABASE_VERSION = 1

        // 1. TABLE: USERS
        const val TABLE_USERS = "users"
        const val COL_USER_ID = "user_id"
        const val COL_USERNAME = "username"
        const val COL_EMAIL = "email"
        const val COL_PASSWORD = "password"
        const val COL_SHOP_NAME = "shop_name"
        const val COL_CONTACT = "contact"
        const val COL_USER_ADDRESS = "address"

        // 2. TABLE: CATEGORIES
        const val TABLE_CATEGORIES = "categories"
        const val COL_CAT_ID = "category_id"
        const val COL_CAT_OWNER_ID = "owner_id"
        const val COL_CAT_NAME = "name"
        const val COL_CAT_DESC = "description"

        // 3. TABLE: PRODUCTS
        const val TABLE_PRODUCTS = "products"
        const val COL_PROD_ID = "product_id"
        const val COL_PROD_OWNER_ID = "owner_id"
        const val COL_PROD_CAT_ID = "category_id"
        const val COL_PROD_NAME = "name"
        const val COL_PROD_DESC = "description"
        const val COL_PROD_PRICE = "price"
        const val COL_PROD_IMAGE = "image"

        // 4. TABLE: INVENTORY
        const val TABLE_INVENTORY = "inventory"
        const val COL_INV_ID = "inv_id"
        const val COL_INV_USER_ID = "user_id"
        const val COL_INV_PROD_ID = "product_id"
        const val COL_INV_STOCK = "stock"
        const val COL_INV_AUTO_RESTOCK = "auto_restock"
        const val COL_INV_SUPPLIER_ID = "restock_supplier_id"
        const val COL_INV_THRESHOLD = "restock_threshold"

        // 5. TABLE: ORDERS
        const val TABLE_ORDERS = "orders"
        const val COL_ORDER_ID = "order_id"
        const val COL_ORDER_BUYER_ID = "buyer_id"
        const val COL_ORDER_SELLER_ID = "seller_id"
        const val COL_ORDER_ADDRESS = "shipping_address"
        const val COL_ORDER_STATUS = "status"
        const val COL_ORDER_TOTAL_PRICE = "total_price"
        const val COL_ORDER_DATE = "order_date"

        // 6. TABLE: ORDER ITEMS
        const val TABLE_ORDER_ITEMS = "order_items"
        const val COL_ITEM_ID = "order_item_id"
        const val COL_ITEM_ORDER_ID = "order_id"
        const val COL_ITEM_PROD_ID = "product_id"
        const val COL_ITEM_QTY = "quantity"
        const val COL_ITEM_PRICE = "price"
    }

    private var dbPath: String = ""

    init {
        dbPath = context.getDatabasePath(DATABASE_NAME).path
        createDatabase()
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Empty because we are copying a pre-made database structure
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (newVersion > oldVersion) {
            context.deleteDatabase(DATABASE_NAME)
            copyDatabase()
            addDummyDataIfEmpty()
        }
    }

    // --- DATABASE COPYING LOGIC ---

    private fun createDatabase() {
        if (!checkDatabase()) {
            // First time the app is installed on this device
            this.readableDatabase
            this.close()
            try {
                copyDatabase()
                addDummyDataIfEmpty()
            } catch (e: IOException) {
                throw RuntimeException("Error copying database", e)
            }
        }
        // If DB already exists, we don't touch user data anymore
    }

    private fun checkDatabase(): Boolean {
        val dbFile = context.getDatabasePath(DATABASE_NAME)
        return dbFile.exists()
    }

    private fun copyDatabase() {
        val inputStream: InputStream = context.assets.open(DATABASE_NAME)
        val outputStream: OutputStream = FileOutputStream(dbPath)

        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }

        outputStream.flush()
        outputStream.close()
        inputStream.close()
    }

    // --- POPULATE DUMMY DATA FOR DEMO (RUNS ONLY ON FIRST INSTALL) ---
    private fun addDummyDataIfEmpty() {
        val db = this.writableDatabase

        db.beginTransaction()
        try {
            // 0. Wipe existing data in correct FK order
            db.execSQL("DELETE FROM $TABLE_ORDER_ITEMS")
            db.execSQL("DELETE FROM $TABLE_ORDERS")
            db.execSQL("DELETE FROM $TABLE_INVENTORY")
            db.execSQL("DELETE FROM $TABLE_PRODUCTS")
            db.execSQL("DELETE FROM $TABLE_CATEGORIES")
            db.execSQL("DELETE FROM $TABLE_USERS")

            // Helper lambdas
            fun insertUser(
                username: String,
                email: String,
                password: String,
                shopName: String,
                contact: String,
                address: String
            ): Long {
                val cv = ContentValues().apply {
                    put(COL_USERNAME, username)
                    put(COL_EMAIL, email)
                    put(COL_PASSWORD, password)
                    put(COL_SHOP_NAME, shopName)
                    put(COL_CONTACT, contact)
                    put(COL_USER_ADDRESS, address)
                }
                return db.insert(TABLE_USERS, null, cv)
            }

            fun insertCategory(ownerId: Long, name: String, desc: String): Long {
                val cv = ContentValues().apply {
                    put(COL_CAT_OWNER_ID, ownerId)
                    put(COL_CAT_NAME, name)
                    put(COL_CAT_DESC, desc)
                }
                return db.insert(TABLE_CATEGORIES, null, cv)
            }

            fun insertProduct(
                ownerId: Long,
                categoryId: Long,
                name: String,
                desc: String,
                price: Double
            ): Long {
                val cv = ContentValues().apply {
                    put(COL_PROD_OWNER_ID, ownerId)
                    put(COL_PROD_CAT_ID, categoryId)
                    put(COL_PROD_NAME, name)
                    put(COL_PROD_DESC, desc)
                    put(COL_PROD_PRICE, price)
                }
                return db.insert(TABLE_PRODUCTS, null, cv)
            }

            fun insertInventory(
                userId: Long,
                productId: Long,
                stock: Int,
                threshold: Int,
                autoRestock: Int = 0
            ): Long {
                val cv = ContentValues().apply {
                    put(COL_INV_USER_ID, userId)
                    put(COL_INV_PROD_ID, productId)
                    put(COL_INV_STOCK, stock)
                    put(COL_INV_THRESHOLD, threshold)
                    put(COL_INV_AUTO_RESTOCK, autoRestock)
                }
                return db.insert(TABLE_INVENTORY, null, cv)
            }

            fun insertOrder(
                buyerId: Long,
                sellerId: Long,
                address: String,
                status: String,
                totalPrice: Double,
                date: String
            ): Long {
                val cv = ContentValues().apply {
                    put(COL_ORDER_BUYER_ID, buyerId)
                    put(COL_ORDER_SELLER_ID, sellerId)
                    put(COL_ORDER_ADDRESS, address)
                    put(COL_ORDER_STATUS, status)
                    put(COL_ORDER_TOTAL_PRICE, totalPrice)
                    put(COL_ORDER_DATE, date)
                }
                return db.insert(TABLE_ORDERS, null, cv)
            }

            fun insertOrderItem(
                orderId: Long,
                productId: Long,
                quantity: Int,
                price: Double
            ): Long {
                val cv = ContentValues().apply {
                    put(COL_ITEM_ORDER_ID, orderId)
                    put(COL_ITEM_PROD_ID, productId)
                    put(COL_ITEM_QTY, quantity)
                    put(COL_ITEM_PRICE, price)
                }
                return db.insert(TABLE_ORDER_ITEMS, null, cv)
            }

            // ==== 1. USERS (admin + extra) ====
            val adminId = insertUser(
                "MobetAdmin", "admin@gmail.com", "123456",
                "Mobet Logistics Main", "0917-123-4567", "Quezon City, Metro Manila"
            )
            val buyer1Id = insertUser(
                "Juan Dela Cruz", "juan@example.com", "password",
                "Personal", "0917-000-1111", "Makati City, Metro Manila"
            )
            val buyer2Id = insertUser(
                "Maria Santos", "maria@example.com", "password",
                "Personal", "0917-222-3333", "Cebu City"
            )

            // ==== 2. CATEGORIES ====
            val catElectronicsId = insertCategory(adminId, "Electronics", "Phones, laptops, accessories")
            val catFashionId = insertCategory(adminId, "Fashion", "Clothes and shoes")
            val catHomeId = insertCategory(adminId, "Home & Living", "Appliances, furniture")
            val catGroceriesId = insertCategory(adminId, "Groceries", "Everyday essentials")
            val catOfficeId = insertCategory(adminId, "Office & School", "Supplies and stationery")

            // ==== 3. PRODUCTS (lots of them) ====
            // Electronics
            val pEarbudsId = insertProduct(adminId, catElectronicsId, "Wireless Earbuds Pro", "Noise-cancelling, 24h battery", 1500.0)
            val pPhoneCaseId = insertProduct(adminId, catElectronicsId, "Shockproof Phone Case", "For 6.5\" phones", 350.0)
            val pPowerbankId = insertProduct(adminId, catElectronicsId, "20,000mAh Powerbank", "Fast charging, dual USB", 1200.0)
            val pSpeakerId = insertProduct(adminId, catElectronicsId, "Bluetooth Speaker", "Water-resistant, 10W", 999.0)

            // Fashion
            val pRunningShoesId = insertProduct(adminId, catFashionId, "Speedster Running Shoes", "Men's size 10", 2500.0)
            val pHoodieId = insertProduct(adminId, catFashionId, "Oversized Hoodie", "Fleece, dark green", 899.0)
            val pCapId = insertProduct(adminId, catFashionId, "Baseball Cap", "Navy blue", 299.0)

            // Home & Living
            val pCoffeeMakerId = insertProduct(adminId, catHomeId, "Auto-Brew Coffee Maker", "1.5L capacity", 3200.0)
            val pAirFryerId = insertProduct(adminId, catHomeId, "Digital Air Fryer 4L", "8 presets", 2800.0)
            val pDeskLampId = insertProduct(adminId, catHomeId, "LED Desk Lamp", "Dimmable", 650.0)

            // Groceries
            val pInstantNoodlesId = insertProduct(adminId, catGroceriesId, "Instant Noodles Pack (x10)", "Beef flavor", 199.0)
            val pCoffeeBeansId = insertProduct(adminId, catGroceriesId, "Premium Coffee Beans 1kg", "Medium roast", 799.0)

            // Office & School
            val pNotebookId = insertProduct(adminId, catOfficeId, "Spiral Notebook A5", "80 sheets, dotted", 89.0)
            val pPenSetId = insertProduct(adminId, catOfficeId, "Gel Pen Set (10 colors)", "0.5mm tip", 129.0)
            val pPaperReamId = insertProduct(adminId, catOfficeId, "Bond Paper A4 (500s)", "70gsm", 320.0)

            // ==== 4. INVENTORY (mix of high / low stock) ====
            // High stock
            insertInventory(adminId, pEarbudsId, stock = 50, threshold = 10, autoRestock = 1)
            insertInventory(adminId, pPhoneCaseId, stock = 120, threshold = 30, autoRestock = 1)
            insertInventory(adminId, pPowerbankId, stock = 40, threshold = 15, autoRestock = 1)
            insertInventory(adminId, pSpeakerId, stock = 35, threshold = 10)

            // Low stock (these should scream on your restock page)
            insertInventory(adminId, pRunningShoesId, stock = 3, threshold = 10)
            insertInventory(adminId, pHoodieId, stock = 5, threshold = 8)
            insertInventory(adminId, pCapId, stock = 2, threshold = 5)
            insertInventory(adminId, pPaperReamId, stock = 7, threshold = 15)

            // Medium stock
            insertInventory(adminId, pCoffeeMakerId, stock = 15, threshold = 5)
            insertInventory(adminId, pAirFryerId, stock = 8, threshold = 5)
            insertInventory(adminId, pDeskLampId, stock = 25, threshold = 10)
            insertInventory(adminId, pInstantNoodlesId, stock = 200, threshold = 50, autoRestock = 1)
            insertInventory(adminId, pCoffeeBeansId, stock = 12, threshold = 8)
            insertInventory(adminId, pNotebookId, stock = 90, threshold = 20)
            insertInventory(adminId, pPenSetId, stock = 60, threshold = 15)

            // ==== 5. ORDERS + ITEMS (for Orders screen later) ====
            val order1Id = insertOrder(
                buyerId = buyer1Id,
                sellerId = adminId,
                address = "123 Sampaguita St, Makati City",
                status = "Pending",
                totalPrice = 1500.0,
                date = "2023-11-01"
            )
            insertOrderItem(order1Id, pEarbudsId, quantity = 1, price = 1500.0)

            val order2Id = insertOrder(
                buyerId = buyer2Id,
                sellerId = adminId,
                address = "IT Park, Cebu City",
                status = "Completed",
                totalPrice = 5698.0,
                date = "2023-11-05"
            )
            insertOrderItem(order2Id, pRunningShoesId, quantity = 1, price = 2500.0)
            insertOrderItem(order2Id, pAirFryerId, quantity = 1, price = 2800.0)
            insertOrderItem(order2Id, pNotebookId, quantity = 3, price = 398.0)

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }


    // ==========================================
    // DATA ACCESS METHODS (LOGIC)
    // ==========================================

    // CHECK LOGIN CREDENTIALS
    fun checkUser(email: String, pass: String): Boolean {
        val db = this.readableDatabase
        val columns = arrayOf(COL_USER_ID)
        val selection = "$COL_EMAIL = ? AND $COL_PASSWORD = ?"
        val selectionArgs = arrayOf(email, pass)

        val cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null)
        val count = cursor.count
        cursor.close()
        db.close()

        return count > 0 // Returns true if user exists
    }

    // GET ORDERS BY STATUS (Pending / Completed)
    fun getOrdersByStatus(status: String): ArrayList<OrderModel> {
        val orderList = ArrayList<OrderModel>()
        val db = this.readableDatabase

        try {
            val cursor = db.rawQuery(
                "SELECT * FROM $TABLE_ORDERS WHERE $COL_ORDER_STATUS = ?",
                arrayOf(status)
            )

            if (cursor.moveToFirst()) {
                do {
                    val order = OrderModel(
                        orderId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ORDER_ID)),
                        buyerId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ORDER_BUYER_ID)),
                        sellerId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ORDER_SELLER_ID)),
                        shippingAddress = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_ADDRESS)),
                        status = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_STATUS)),
                        totalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_ORDER_TOTAL_PRICE)),
                        orderDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_DATE))
                    )
                    orderList.add(order)
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }

        return orderList
    }

    // REGISTER NEW USER (Sign Up)
    fun addUser(username: String, email: String, pass: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()

        contentValues.put(COL_USERNAME, username)
        contentValues.put(COL_EMAIL, email)
        contentValues.put(COL_PASSWORD, pass)
        // Default values for fields not in your Sign Up screen yet
        contentValues.put(COL_SHOP_NAME, "My Shop")
        contentValues.put(COL_CONTACT, "N/A")
        contentValues.put(COL_USER_ADDRESS, "N/A")

        val result = db.insert(TABLE_USERS, null, contentValues)
        db.close()

        // If result is -1, insert failed. Otherwise, it worked.
        return result != -1L
    }

    // GET ALL INVENTORY ITEMS (For Restock Page)
    fun getAllInventory(): ArrayList<InventoryModel> {
        val invList = ArrayList<InventoryModel>()
        val db = this.readableDatabase

        // SQL JOIN: Get Inventory details + Product Name
        val query = """
        SELECT i.$COL_INV_ID, p.$COL_PROD_NAME, i.$COL_INV_STOCK, i.$COL_INV_THRESHOLD
        FROM $TABLE_INVENTORY i
        JOIN $TABLE_PRODUCTS p ON i.$COL_INV_PROD_ID = p.$COL_PROD_ID
    """

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val item = InventoryModel(
                    invId = cursor.getInt(0),
                    productName = cursor.getString(1),
                    stock = cursor.getInt(2),
                    threshold = cursor.getInt(3)   // use actual threshold from DB
                )
                invList.add(item)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return invList
    }


    // GET INVENTORY FOR SPECIFIC USER (admin account, etc.)
    fun getInventoryForUser(userId: Int): ArrayList<InventoryModel> {
        val invList = ArrayList<InventoryModel>()
        val db = this.readableDatabase

        val query = """
        SELECT i.$COL_INV_ID, p.$COL_PROD_NAME, i.$COL_INV_STOCK, i.$COL_INV_THRESHOLD
        FROM $TABLE_INVENTORY i
        JOIN $TABLE_PRODUCTS p ON i.$COL_INV_PROD_ID = p.$COL_PROD_ID
        WHERE i.$COL_INV_USER_ID = ?
    """

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val item = InventoryModel(
                    invId = cursor.getInt(0),
                    productName = cursor.getString(1),
                    stock = cursor.getInt(2),
                    threshold = cursor.getInt(3)
                )
                invList.add(item)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return invList
    }

    fun getUserId(email: String, pass: String): Int? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COL_USER_ID),
            "$COL_EMAIL = ? AND $COL_PASSWORD = ?",
            arrayOf(email, pass),
            null, null, null
        )

        val id = if (cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID))
        } else null

        cursor.close()
        db.close()
        return id
    }

    // UPDATE STOCK FOR ONE INVENTORY ROW
    fun updateInventoryStock(invId: Int, newStock: Int) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_INV_STOCK, newStock)
        }
        db.update(TABLE_INVENTORY, values, "$COL_INV_ID = ?", arrayOf(invId.toString()))
        db.close()
    }

}
