package com.example.mobetlog

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
        private const val DATABASE_NAME = "mobet_logistics.db" // File name in Assets
        private const val DATABASE_VERSION = 1
        private var DB_PATH = ""

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

        // 7. TABLE: DELIVERY
        const val TABLE_DELIVERY = "delivery"
        const val COL_DEL_ID = "delivery_id"
        const val COL_DEL_ORDER_ID = "order_id"
        const val COL_DEL_STATUS = "status"
        const val COL_DEL_UPDATE = "last_update"

        // 8. TABLE: CHAT
        const val TABLE_CHAT = "chat"
        const val COL_MSG_ID = "message_id"
        const val COL_MSG_SENDER = "sender_id"
        const val COL_MSG_RECEIVER = "receiver_id"
        const val COL_MSG_TEXT = "message"
        const val COL_MSG_TIME = "timestamp"

        // 9. TABLE: INVENTORY LOG
        const val TABLE_LOG = "inventory_log"
        const val COL_LOG_ID = "log_id"
        const val COL_LOG_USER = "user_id"
        const val COL_LOG_PROD = "product_id"
        const val COL_LOG_TYPE = "change_type"
        const val COL_LOG_AMOUNT = "amount"
        const val COL_LOG_TIME = "timestamp"
    }

    // INITIALIZATION & COPY LOGIC
    init {
        DB_PATH = context.applicationInfo.dataDir + "/databases/"

        // If database does not exist, copy it from assets
        if (!checkDatabase()) {
            this.readableDatabase // Create empty DB shell
            this.close()
            try {
                copyDatabase()
            } catch (e: IOException) {
                throw RuntimeException("Error copying database from assets", e)
            }
        }
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        // CRITICAL: Enable Foreign Key support for cascade deletes
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Empty because we are copying a pre-made database
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (newVersion > oldVersion) {
            try {
                copyDatabase() // Overwrite with new version if needed
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun checkDatabase(): Boolean {
        val dbFile = context.getDatabasePath(DATABASE_NAME)
        return dbFile.exists()
    }

    @Throws(IOException::class)
    private fun copyDatabase() {
        val myInput: InputStream = context.assets.open(DATABASE_NAME)
        val outFileName = DB_PATH + DATABASE_NAME
        val myOutput: OutputStream = FileOutputStream(outFileName)

        val buffer = ByteArray(1024)
        var length: Int
        while (myInput.read(buffer).also { length = it } > 0) {
            myOutput.write(buffer, 0, length)
        }

        myOutput.flush()
        myOutput.close()
        myInput.close()
    }

    // DATA ACCESS FUNCTIONS (QUERIES)

    // 1. GET ALL ORDERS (Matches your OrderModel)
    fun getAllOrders(): ArrayList<OrderModel> {
        val orderList = ArrayList<OrderModel>()
        val db = this.readableDatabase

        val cursor = db.rawQuery("SELECT * FROM $TABLE_ORDERS", null)

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
        db.close()
        return orderList
    }

    // 2. EXAMPLE: GET ORDERS BY STATUS (Pending vs History)
    fun getOrdersByStatus(status: String): ArrayList<OrderModel> {
        val orderList = ArrayList<OrderModel>()
        val db = this.readableDatabase

        // Use ? as a placeholder for security (prevents SQL Injection)
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ORDERS WHERE $COL_ORDER_STATUS = ?", arrayOf(status))

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
        db.close()
        return orderList
    }
}