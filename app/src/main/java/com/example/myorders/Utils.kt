package com.example.myorders

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

fun String.showAsToast(context: Context) {
    if (this.isNotEmpty())
        Toast.makeText(context, this, Toast.LENGTH_LONG).show()
}

fun RecyclerView.hideFabOnScroll(fab: View) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            recyclerView.run { if (!canScrollVertically(-1)) fab.visibility = View.VISIBLE }
            super.onScrollStateChanged(recyclerView, newState)
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dy > 10) fab.visibility = View.GONE
            else if (dy < -75) fab.visibility = View.VISIBLE
        }
    })
}

fun Date.changeToString(): String? {
    val dateFormat = SimpleDateFormat("EEE, " + "dd MMM yyyy", Locale.ENGLISH)
    return try {
        dateFormat.format(this)
    } catch (e: Exception) {
        null
    }
}