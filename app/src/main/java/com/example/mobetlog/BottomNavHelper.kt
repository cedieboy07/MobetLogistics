package com.example.mobetlog

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.LinearLayout

object BottomNavHelper {

    enum class NavItem {
        CHAT, INVENTORY, PROFILE, ORDERS, DELIVERIES
    }

    fun setup(activity: Activity, selectedItem: NavItem) {
        val navChat = activity.findViewById<LinearLayout>(R.id.navChat)
        val navInventory = activity.findViewById<LinearLayout>(R.id.navInventory)
        val navProfile = activity.findViewById<LinearLayout>(R.id.navProfile)
        val navOrders = activity.findViewById<LinearLayout>(R.id.navOrders)
        val navDeliveries = activity.findViewById<LinearLayout>(R.id.navDeliveries)

        // In case some screen doesn't have the bar
        if (navChat == null || navInventory == null ||
            navProfile == null || navOrders == null || navDeliveries == null
        ) return

        fun highlightSelected(item: NavItem) {
            fun setSelected(view: View, isSelected: Boolean) {
                view.alpha = if (isSelected) 1f else 0.6f
            }
            setSelected(navChat,       item == NavItem.CHAT)
            setSelected(navInventory,  item == NavItem.INVENTORY)
            setSelected(navProfile,    item == NavItem.PROFILE)
            setSelected(navOrders,     item == NavItem.ORDERS)
            setSelected(navDeliveries, item == NavItem.DELIVERIES)
        }

        highlightSelected(selectedItem)

        navChat.setOnClickListener {
            if (selectedItem != NavItem.CHAT) {
                activity.startActivity(Intent(activity, ChatsActivity::class.java))
                activity.overridePendingTransition(0, 0)
            }
        }

        navInventory.setOnClickListener {
            val intent = Intent(activity, Inventory_Dashboard::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(intent)
            activity.overridePendingTransition(0, 0)
        }


        navProfile.setOnClickListener {
            if (selectedItem != NavItem.PROFILE) {
                activity.startActivity(Intent(activity, Profile::class.java))
                activity.overridePendingTransition(0, 0)
            }
        }

        navOrders.setOnClickListener {
            if (selectedItem != NavItem.ORDERS) {
                activity.startActivity(Intent(activity, OrdersActivity::class.java))
                activity.overridePendingTransition(0, 0)
            }
        }

        navDeliveries.setOnClickListener {
            if (selectedItem != NavItem.DELIVERIES) {
                activity.startActivity(Intent(activity, DeliveriesActivity::class.java))
                activity.overridePendingTransition(0, 0)
            }
        }
    }
}
