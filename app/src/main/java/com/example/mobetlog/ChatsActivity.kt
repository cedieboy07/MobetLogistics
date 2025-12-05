package com.example.mobetlog

import ChatMessageAdapter
import ChatMessageModel
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView

class ChatsActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var edtMessage: EditText

    private lateinit var adapter: ChatMessageAdapter
    private lateinit var db: DatabaseHelper

    private var currentUserId = -1
    private var partnerId = -1   // TODO — choose a partner from sidebar list later

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chats)

        db = DatabaseHelper(this)

        val prefs = getSharedPreferences("mobet_prefs", MODE_PRIVATE)
        currentUserId = prefs.getInt("USER_ID", -1)

        // TEMP for demo
        partnerId = 1  // replace with actual selected partner

        recycler = findViewById(R.id.recyclerChat)
        edtMessage = findViewById(R.id.editTextText2)

        adapter = ChatMessageAdapter(
            db.getChatMessages(currentUserId, partnerId),
            currentUserId
        )

        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this)

        findViewById<ImageView>(R.id.imageView34).setOnClickListener {
            sendMessage()
        }

        BottomNavHelper.setup(
            activity = this,
            selectedItem = BottomNavHelper.NavItem.CHAT
        )
    }

    private fun sendMessage() {
        val text = edtMessage.text.toString().trim()
        if (text.isEmpty()) return

        db.sendChatMessage(currentUserId, partnerId, text)

        adapter.addMessage(
            ChatMessageModel(
                senderId = currentUserId,
                receiverId = partnerId,
                message = text,
                timestamp = System.currentTimeMillis()
            )
        )


        adapter.notifyItemInserted(adapter.itemCount - 1)
        recycler.scrollToPosition(adapter.itemCount - 1)


        edtMessage.setText("")
    }
}
