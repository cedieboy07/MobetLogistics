package com.example.mobetlog

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import CategoryModel
import ChatMessageModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

        // 1b. TABLE: USER PROFILE (extended fields)
        const val TABLE_USER_PROFILE = "user_profile"
        const val COL_UP_USER_ID = "user_id"
        const val COL_UP_FIRST_NAME = "first_name"
        const val COL_UP_MIDDLE_NAME = "middle_name"
        const val COL_UP_LAST_NAME = "last_name"
        const val COL_UP_SEX = "sex"
        const val COL_UP_BIRTHDATE = "birthdate"
        const val COL_UP_AGE = "age"

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

        // 7. TABLE: INVENTORY HISTORY
        const val TABLE_INV_HISTORY = "inventory_history"
        const val COL_LOG_ID = "log_id"
        const val COL_LOG_USER_ID = "user_id"
        const val COL_LOG_PRODUCT_NAME = "product_name"
        const val COL_LOG_ACTION = "action"
        const val COL_LOG_QTY_CHANGE = "qty_change"
        const val COL_LOG_TIMESTAMP = "timestamp"

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
            ensureHistoryTable()
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

        // Safe to call every time; doesn't touch existing user data
        ensureUserProfileTable()
        ensureHistoryTable()
        ensureChatTableExists()

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

    private fun ensureUserProfileTable() {
        val db = this.writableDatabase
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_USER_PROFILE (
                $COL_UP_USER_ID INTEGER PRIMARY KEY,
                $COL_UP_FIRST_NAME TEXT,
                $COL_UP_MIDDLE_NAME TEXT,
                $COL_UP_LAST_NAME TEXT,
                $COL_UP_SEX TEXT,
                $COL_UP_BIRTHDATE TEXT,
                $COL_UP_AGE INTEGER,
                FOREIGN KEY($COL_UP_USER_ID) REFERENCES $TABLE_USERS($COL_USER_ID)
            )
            """.trimIndent()
        )
        db.close()
    }

    // Create inventory history table if it doesn't exist yet
    private fun ensureHistoryTable() {
        val db = this.writableDatabase
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_INV_HISTORY (
                $COL_LOG_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_LOG_USER_ID INTEGER,
                $COL_LOG_PRODUCT_NAME TEXT,
                $COL_LOG_ACTION TEXT,
                $COL_LOG_QTY_CHANGE INTEGER,
                $COL_LOG_TIMESTAMP TEXT
            )
            """.trimIndent()
        )
        db.close()
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
                "Juan's Department Store", "0917-000-1111", "Makati City, Metro Manila"
            )
            val buyer2Id = insertUser(
                "Maria Santos", "maria@example.com", "password",
                "Maria Pharmacy", "0917-222-3333", "Cebu City"
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

        val query = """
        SELECT i.$COL_INV_ID, i.$COL_INV_PROD_ID, p.$COL_PROD_NAME,
               i.$COL_INV_STOCK, i.$COL_INV_THRESHOLD
        FROM $TABLE_INVENTORY i
        JOIN $TABLE_PRODUCTS p ON i.$COL_INV_PROD_ID = p.$COL_PROD_ID
    """

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                invList.add(
                    InventoryModel(
                        invId = cursor.getInt(0),
                        productId = cursor.getInt(1),
                        productName = cursor.getString(2),
                        stock = cursor.getInt(3),
                        threshold = cursor.getInt(4)
                    )
                )
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
        SELECT i.$COL_INV_ID, i.$COL_INV_PROD_ID, p.$COL_PROD_NAME,
               i.$COL_INV_STOCK, i.$COL_INV_THRESHOLD
        FROM $TABLE_INVENTORY i
        JOIN $TABLE_PRODUCTS p ON i.$COL_INV_PROD_ID = p.$COL_PROD_ID
        WHERE i.$COL_INV_USER_ID = ?
    """

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        if (cursor.moveToFirst()) {
            do {
                invList.add(
                    InventoryModel(
                        invId = cursor.getInt(0),
                        productId = cursor.getInt(1),
                        productName = cursor.getString(2),
                        stock = cursor.getInt(3),
                        threshold = cursor.getInt(4)
                    )
                )
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

    // Get (or create) a default category for a user
    fun getDefaultCategoryIdForUser(userId: Int): Long {
        val db = this.writableDatabase

        // Try to find existing category for this user
        val cursor = db.rawQuery(
            "SELECT $COL_CAT_ID FROM $TABLE_CATEGORIES WHERE $COL_CAT_OWNER_ID = ? LIMIT 1",
            arrayOf(userId.toString())
        )

        val existingId = if (cursor.moveToFirst()) {
            cursor.getLong(0)
        } else {
            null
        }
        cursor.close()

        if (existingId != null) return existingId

        // If none, create "General"
        val cv = ContentValues().apply {
            put(COL_CAT_OWNER_ID, userId)
            put(COL_CAT_NAME, "General")
            put(COL_CAT_DESC, "Default category")
        }
        return db.insert(TABLE_CATEGORIES, null, cv)
    }

    // Get the current category_id of a product, or null if none
    private fun getCategoryIdForProduct(productId: Int): Int? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_PROD_CAT_ID FROM $TABLE_PRODUCTS WHERE $COL_PROD_ID = ? LIMIT 1",
            arrayOf(productId.toString())
        )

        val categoryId: Int? = if (cursor.moveToFirst() && !cursor.isNull(0)) {
            cursor.getInt(0)
        } else {
            null
        }

        cursor.close()
        db.close()
        return categoryId
    }


    // Get all products for a given user, including stock & category name
    fun getProductsForUser(userId: Int): ArrayList<ProductModel> {
        val list = ArrayList<ProductModel>()
        val db = this.readableDatabase

        val query = """
        SELECT p.$COL_PROD_ID,
               p.$COL_PROD_NAME,
               p.$COL_PROD_PRICE,
               IFNULL(i.$COL_INV_STOCK, 0) AS stock,
               IFNULL(c.$COL_CAT_NAME, 'Uncategorized') AS cat_name
        FROM $TABLE_PRODUCTS p
        LEFT JOIN $TABLE_INVENTORY i ON i.$COL_INV_PROD_ID = p.$COL_PROD_ID
        LEFT JOIN $TABLE_CATEGORIES c ON p.$COL_PROD_CAT_ID = c.$COL_CAT_ID
        WHERE p.$COL_PROD_OWNER_ID = ?
        ORDER BY p.$COL_PROD_ID ASC
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val product = ProductModel(
                    productId = cursor.getInt(0),
                    name = cursor.getString(1),
                    price = cursor.getDouble(2),
                    qty = cursor.getInt(3),
                    categoryName = cursor.getString(4)
                )
                list.add(product)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return list
    }

    fun addProductForUser(
        userId: Int,
        name: String,
        price: Double,
        qty: Int,
        categoryId: Int
    ): Boolean {
        val db = this.writableDatabase
        db.beginTransaction()
        return try {
            val prodValues = ContentValues().apply {
                put(COL_PROD_OWNER_ID, userId)
                put(COL_PROD_CAT_ID, categoryId)
                put(COL_PROD_NAME, name)
                put(COL_PROD_DESC, "")
                put(COL_PROD_PRICE, price)
            }
            val prodId = db.insert(TABLE_PRODUCTS, null, prodValues)
            if (prodId == -1L) return false

            val invValues = ContentValues().apply {
                put(COL_INV_USER_ID, userId)
                put(COL_INV_PROD_ID, prodId)
                put(COL_INV_STOCK, qty)
                put(COL_INV_THRESHOLD, 10)
                put(COL_INV_AUTO_RESTOCK, 0)
            }
            val invResult = db.insert(TABLE_INVENTORY, null, invValues)
            if (invResult == -1L) return false

            db.setTransactionSuccessful()
            true
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun addProductForUser(
        userId: Int,
        name: String,
        price: Double,
        qty: Int
    ): Boolean {
        val defaultCategoryId = getDefaultCategoryIdForUser(userId).toInt()
        return addProductForUser(userId, name, price, qty, defaultCategoryId)
    }

    fun updateProductForUser(
        userId: Int,
        productId: Int,
        name: String,
        price: Double,
        qty: Int,
        categoryId: Int
    ): Boolean {
        val db = this.writableDatabase
        db.beginTransaction()
        return try {
            val prodValues = ContentValues().apply {
                put(COL_PROD_NAME, name)
                put(COL_PROD_PRICE, price)
                put(COL_PROD_CAT_ID, categoryId)
            }
            val prodRows = db.update(
                TABLE_PRODUCTS,
                prodValues,
                "$COL_PROD_ID = ? AND $COL_PROD_OWNER_ID = ?",
                arrayOf(productId.toString(), userId.toString())
            )

            val invValues = ContentValues().apply {
                put(COL_INV_STOCK, qty)
            }
            val invRows = db.update(
                TABLE_INVENTORY,
                invValues,
                "$COL_INV_PROD_ID = ? AND $COL_INV_USER_ID = ?",
                arrayOf(productId.toString(), userId.toString())
            )

            val success = (prodRows > 0 && invRows > 0)
            if (success) db.setTransactionSuccessful()
            success
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun updateProductForUser(
        userId: Int,
        productId: Int,
        name: String,
        price: Double,
        qty: Int
    ): Boolean {
        val currentCategoryId = getCategoryIdForProduct(productId)
            ?: getDefaultCategoryIdForUser(userId).toInt()

        return updateProductForUser(userId, productId, name, price, qty, currentCategoryId)
    }

    // Delete product (and its inventory) by ID for this user
    fun deleteProductForUser(userId: Int, productId: Int): Boolean {
        val db = this.writableDatabase
        db.beginTransaction()
        return try {
            db.delete(
                TABLE_INVENTORY,
                "$COL_INV_PROD_ID = ? AND $COL_INV_USER_ID = ?",
                arrayOf(productId.toString(), userId.toString())
            )
            val prodRows = db.delete(
                TABLE_PRODUCTS,
                "$COL_PROD_ID = ? AND $COL_PROD_OWNER_ID = ?",
                arrayOf(productId.toString(), userId.toString())
            )
            val success = prodRows > 0
            if (success) db.setTransactionSuccessful()
            success
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    // Get all categories for a given user
    fun getCategoriesForUser(userId: Int): ArrayList<CategoryModel> {
        val result = ArrayList<CategoryModel>()
        val db = readableDatabase

        val query = """
        SELECT c.$COL_CAT_ID,
               c.$COL_CAT_NAME,
               c.$COL_CAT_DESC,
               (SELECT COUNT(*) FROM $TABLE_PRODUCTS p
                WHERE p.$COL_PROD_CAT_ID = c.$COL_CAT_ID) AS prod_count
        FROM $TABLE_CATEGORIES c
        WHERE c.$COL_CAT_OWNER_ID = ?
        ORDER BY c.$COL_CAT_NAME
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(0)
                val name = cursor.getString(1)

                val descIndex = cursor.getColumnIndexOrThrow(COL_CAT_DESC)
                val desc = if (!cursor.isNull(descIndex)) cursor.getString(descIndex) else ""

                val count = cursor.getInt(3)

                result.add(
                    CategoryModel(
                        id = id,
                        name = name,
                        desc = desc,
                        productCount = count
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return result
    }

    // Add new category for a user
    fun addCategoryForUser(userId: Int, name: String, description: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CAT_OWNER_ID, userId)
            put(COL_CAT_NAME, name)
            put(COL_CAT_DESC, description)
        }
        val result = db.insert(TABLE_CATEGORIES, null, values)
        db.close()
        return result != -1L
    }

    // Update existing category
    fun updateCategory(categoryId: Int, name: String, description: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CAT_NAME, name)
            put(COL_CAT_DESC, description)
        }
        val rows = db.update(
            TABLE_CATEGORIES,
            values,
            "$COL_CAT_ID = ?",
            arrayOf(categoryId.toString())
        )
        db.close()
        return rows > 0
    }

    // Delete category (and optionally its products)
    fun deleteCategory(categoryId: Int): Boolean {
        val db = writableDatabase

        // optional: remove products in that category
        db.delete(
            TABLE_PRODUCTS,
            "$COL_PROD_CAT_ID = ?",
            arrayOf(categoryId.toString())
        )

        val rows = db.delete(
            TABLE_CATEGORIES,
            "$COL_CAT_ID = ?",
            arrayOf(categoryId.toString())
        )
        db.close()
        return rows > 0
    }

    // ===================== INVENTORY HISTORY =====================

    fun insertInventoryLog(
        userId: Int,
        productName: String,
        action: String,
        qtyChange: Int
    ) {
        val db = this.writableDatabase

        val timestamp = SimpleDateFormat(
            "MMM dd, yyyy hh:mm a",
            Locale.getDefault()
        ).format(Date())

        val values = ContentValues().apply {
            put(COL_LOG_USER_ID, userId)
            put(COL_LOG_PRODUCT_NAME, productName)
            put(COL_LOG_ACTION, action)
            put(COL_LOG_QTY_CHANGE, qtyChange)
            put(COL_LOG_TIMESTAMP, timestamp)
        }

        db.insert(TABLE_INV_HISTORY, null, values)
        db.close()
    }

    fun getInventoryLogsForUser(userId: Int): ArrayList<InventoryHistoryModel> {
        val list = ArrayList<InventoryHistoryModel>()
        val db = this.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT $COL_LOG_ID,
                   $COL_LOG_TIMESTAMP,
                   $COL_LOG_PRODUCT_NAME,
                   $COL_LOG_ACTION,
                   $COL_LOG_QTY_CHANGE
            FROM $TABLE_INV_HISTORY
            WHERE $COL_LOG_USER_ID = ?
            ORDER BY $COL_LOG_ID DESC
            """.trimIndent(),
            arrayOf(userId.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                list.add(
                    InventoryHistoryModel(
                        id = cursor.getInt(0),
                        timestamp = cursor.getString(1),
                        productName = cursor.getString(2),
                        action = cursor.getString(3),
                        qtyChange = cursor.getInt(4)
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return list
    }

    fun clearInventoryLogsForUser(userId: Int): Boolean {
        val db = this.writableDatabase
        val rows = db.delete(
            TABLE_INV_HISTORY,
            "$COL_LOG_USER_ID = ?",
            arrayOf(userId.toString())
        )
        db.close()
        return rows > 0
    }

    // ===================== USER PROFILE (EXTENDED FIELDS) =====================

    fun getUserProfile(userId: Int): UserProfileModel? {
        val db = this.readableDatabase

        val query = """
            SELECT u.$COL_USER_ID,
                   u.$COL_USERNAME,
                   u.$COL_SHOP_NAME,
                   u.$COL_CONTACT,
                   u.$COL_USER_ADDRESS,
                   u.$COL_EMAIL,
                   p.$COL_UP_FIRST_NAME,
                   p.$COL_UP_MIDDLE_NAME,
                   p.$COL_UP_LAST_NAME,
                   p.$COL_UP_SEX,
                   p.$COL_UP_BIRTHDATE,
                   p.$COL_UP_AGE
            FROM $TABLE_USERS u
            LEFT JOIN $TABLE_USER_PROFILE p
              ON u.$COL_USER_ID = p.$COL_UP_USER_ID
            WHERE u.$COL_USER_ID = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        var result: UserProfileModel? = null
        if (cursor.moveToFirst()) {
            result = UserProfileModel(
                userId = cursor.getInt(0),
                username = cursor.getString(1),
                shopName = cursor.getString(2),
                contact = cursor.getString(3),
                address = cursor.getString(4),
                email = cursor.getString(5),
                firstName = cursor.getString(6),
                middleName = cursor.getString(7),
                lastName = cursor.getString(8),
                sex = cursor.getString(9),
                birthdate = cursor.getString(10),
                age = if (!cursor.isNull(11)) cursor.getInt(11) else null
            )
        }

        cursor.close()
        db.close()
        return result
    }

    fun upsertUserProfile(profile: UserProfileModel): Boolean {
        val db = this.writableDatabase

        val values = ContentValues().apply {
            put(COL_UP_USER_ID, profile.userId)
            put(COL_UP_FIRST_NAME, profile.firstName)
            put(COL_UP_MIDDLE_NAME, profile.middleName)
            put(COL_UP_LAST_NAME, profile.lastName)
            put(COL_UP_SEX, profile.sex)
            put(COL_UP_BIRTHDATE, profile.birthdate)
            put(COL_UP_AGE, profile.age)
        }

        // Try update first
        val rowsUpdated = db.update(
            TABLE_USER_PROFILE,
            values,
            "$COL_UP_USER_ID = ?",
            arrayOf(profile.userId.toString())
        )

        if (rowsUpdated == 0) {
            // No existing row → insert
            db.insert(TABLE_USER_PROFILE, null, values)
        }

        db.close()
        return true
    }

    fun getAllCompaniesExcept(userId: Int): List<Pair<Int, String>> {
        val result = mutableListOf<Pair<Int, String>>()
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT $COL_USER_ID, $COL_SHOP_NAME, $COL_USERNAME FROM $TABLE_USERS WHERE $COL_USER_ID != ?",
            arrayOf(userId.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(0)
                val shop = cursor.getString(1)
                val username = cursor.getString(2)

                result.add(Pair(id, "$shop ($username)"))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return result
    }

    fun createOrder(
        buyerId: Int,
        sellerId: Int,
        address: String,
        productId: Int,
        qty: Int
    ): Long {

        val db = writableDatabase
        db.beginTransaction()

        return try {
            val priceCursor = db.rawQuery(
                "SELECT $COL_PROD_PRICE FROM $TABLE_PRODUCTS WHERE $COL_PROD_ID = ?",
                arrayOf(productId.toString())
            )

            var price = 0.0
            if (priceCursor.moveToFirst()) {
                price = priceCursor.getDouble(0)
            }
            priceCursor.close()

            val totalPrice = price * qty

            val orderValues = ContentValues().apply {
                put(COL_ORDER_BUYER_ID, buyerId)
                put(COL_ORDER_SELLER_ID, sellerId)
                put(COL_ORDER_ADDRESS, address)
                put(COL_ORDER_STATUS, "Pending")
                put(COL_ORDER_TOTAL_PRICE, totalPrice)
                put(COL_ORDER_DATE, System.currentTimeMillis().toString())
            }

            val orderId = db.insert(TABLE_ORDERS, null, orderValues)

            if (orderId == -1L) throw Exception("Order insert failed")

            val itemValues = ContentValues().apply {
                put(COL_ITEM_ORDER_ID, orderId)
                put(COL_ITEM_PROD_ID, productId)
                put(COL_ITEM_QTY, qty)
                put(COL_ITEM_PRICE, price)
            }

            val itemId = db.insert(TABLE_ORDER_ITEMS, null, itemValues)
            if (itemId == -1L) throw Exception("Order item insert failed")

            db.setTransactionSuccessful()
            orderId

        } catch (e: Exception) {
            -1L
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun getOrdersForUserHistory(userId: Int): List<OrderModel> {
        val orders = mutableListOf<OrderModel>()
        val db = readableDatabase

        val cursor = db.rawQuery(
            """
        SELECT * FROM $TABLE_ORDERS
        WHERE ($COL_ORDER_BUYER_ID = ? OR $COL_ORDER_SELLER_ID = ?)
        AND $COL_ORDER_STATUS IN ('On Delivery', 'Completed', 'Cancelled')
        ORDER BY $COL_ORDER_DATE DESC
        """.trimIndent(),
            arrayOf(userId.toString(), userId.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                orders.add(
                    OrderModel(
                        orderId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ORDER_ID)),
                        buyerId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ORDER_BUYER_ID)),
                        sellerId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ORDER_SELLER_ID)),
                        shippingAddress = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_ADDRESS)),
                        status = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_STATUS)),
                        totalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_ORDER_TOTAL_PRICE)),
                        orderDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_DATE))
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return orders
    }

    // Returns lines like: "2x Product A\n1x Product B"
    // All non-pending orders for History tab (Completed / Cancelled / On Delivery, etc.)
    fun getOrdersForHistory(): ArrayList<OrderModel> {
        val orderList = ArrayList<OrderModel>()
        val db = this.readableDatabase

        try {
            val cursor = db.rawQuery(
                "SELECT * FROM $TABLE_ORDERS WHERE $COL_ORDER_STATUS <> ? ORDER BY $COL_ORDER_DATE DESC",
                arrayOf("Pending")
            )

            if (cursor.moveToFirst()) {
                do {
                    orderList.add(
                        OrderModel(
                            orderId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ORDER_ID)),
                            buyerId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ORDER_BUYER_ID)),
                            sellerId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ORDER_SELLER_ID)),
                            shippingAddress = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_ADDRESS)),
                            status = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_STATUS)),
                            orderDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_DATE)),
                            totalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_ORDER_TOTAL_PRICE))
                        )
                    )
                } while (cursor.moveToNext())
            }

            cursor.close()
        } finally {
            db.close()
        }

        return orderList
    }

    // Update status when cancelling / packaging
    fun updateOrderStatus(orderId: Int, newStatus: String): Boolean {
        val db = this.writableDatabase
        return try {
            val values = android.content.ContentValues().apply {
                put(COL_ORDER_STATUS, newStatus)
            }
            val rows = db.update(
                TABLE_ORDERS,
                values,
                "$COL_ORDER_ID = ?",
                arrayOf(orderId.toString())
            )
            rows > 0
        } finally {
            db.close()
        }
    }

    // Build "1x Product A\n3x Product B" text
    fun getOrderItemsSummary(orderId: Int): String {
        val db = readableDatabase
        val sb = StringBuilder()

        val query = """
        SELECT oi.$COL_ITEM_QTY, p.$COL_PROD_NAME
        FROM $TABLE_ORDER_ITEMS oi
        JOIN $TABLE_PRODUCTS p ON oi.$COL_ITEM_PROD_ID = p.$COL_PROD_ID
        WHERE oi.$COL_ITEM_ORDER_ID = ?
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(orderId.toString()))
        try {
            if (cursor.moveToFirst()) {
                do {
                    val qty = cursor.getInt(0)
                    val name = cursor.getString(1)

                    if (sb.isNotEmpty()) sb.append("\n")
                    sb.append("${qty}x $name")

                } while (cursor.moveToNext())
            }
        } finally {
            cursor.close()
            db.close()
        }

        return sb.toString()
    }

    fun sendChatMessage(senderId: Int, receiverId: Int, message: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("sender_id", senderId)
            put("receiver_id", receiverId)
            put("message", message)
            put("timestamp", System.currentTimeMillis())
        }
        return db.insert("chat_messages", null, values)
    }

    fun getChatMessages(user1: Int, user2: Int): MutableList<ChatMessageModel> {
        val db = readableDatabase
        val list = mutableListOf<ChatMessageModel>()

        val cursor = db.rawQuery(
            """SELECT * FROM chat_messages 
           WHERE (sender_id = ? AND receiver_id = ?) 
              OR (sender_id = ? AND receiver_id = ?)
           ORDER BY timestamp ASC""",
            arrayOf(user1.toString(), user2.toString(), user2.toString(), user1.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                list.add(
                    ChatMessageModel(
                        id = cursor.getInt(0),
                        senderId = cursor.getInt(1),
                        receiverId = cursor.getInt(2),
                        message = cursor.getString(3),
                        timestamp = cursor.getLong(4)
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        return list
    }

    private fun ensureChatTableExists() {
        val db = this.writableDatabase
        db.execSQL("""
        CREATE TABLE IF NOT EXISTS chat_messages (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            sender_id INTEGER,
            receiver_id INTEGER,
            message TEXT,
            timestamp LONG
        )
    """)
        db.close()
    }



}
