data class ChatMessageModel(
    val id: Int = 0,
    val senderId: Int,
    val receiverId: Int,
    val message: String,
    val timestamp: Long
)
