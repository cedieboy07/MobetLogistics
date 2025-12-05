package com.example.mobetlog

data class UserProfileModel(
    val userId: Int,
    val username: String,
    val shopName: String?,
    val contact: String?,
    val address: String?,
    val email: String?,
    val firstName: String?,
    val middleName: String?,
    val lastName: String?,
    val sex: String?,
    val birthdate: String?,
    val age: Int?
)
