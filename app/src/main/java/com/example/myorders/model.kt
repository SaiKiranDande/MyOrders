package com.example.myorders

import com.chibatching.kotpref.KotprefModel
import java.util.*

//POJO/data class
class OrderDetails(
    val order_number: String? = null,
    val order_name: String = "",
    val order_due_date: Date? = null,
    val customer_name: String = "",
    val customer_address: String = "",
    val customer_phone: Long? = null,
    val order_total: String = ""
)


//KotprefModel kind of (shared preference in java)
object UserPreference : KotprefModel() {
    var userEmail: String by stringPrefVar("")
    var password: String by stringPrefVar("")
    var rememberUser: Boolean by booleanPrefVar(false)
}

enum class OrderAction{EDIT,DELETE}