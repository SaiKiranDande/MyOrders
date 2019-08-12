package com.example.myorders

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import com.example.myorders.databinding.ItemOrderDetailsBinding
import com.github.nitrico.lastadapter.LastAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_orders.*
import kotlinx.android.synthetic.main.item_order_details.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.net.ConnectException

/**
 * In this activity will display the order details in a recyclerview
 */

class OrdersActivity : AppCompatActivity() {

    private val TAG = "Orders"
    private val lastAdapter: LastAdapter by lazy { populateList() }
    private lateinit var database: FirebaseDatabase
    private val listOfData: MutableList<OrderDetails> = mutableListOf()
    private val allList: MutableList<OrderDetails> = mutableListOf()
    private lateinit var eventBus: EventBus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)
        supportActionBar
        eventBus = EventBus.getDefault()
        database = FirebaseDatabase.getInstance()
        initViews()
    }

    //Resgister the event bus
    override fun onStart() {
        Log.i(TAG, "onStart")
        super.onStart()
        if (!eventBus.isRegistered(this))
            eventBus.register(this)
    }

    //Checks for connected to internet or not and call getData() method
    override fun onResume() {
        Log.i(TAG, "onResume")
        super.onResume()
        if (isNetworkConnected()) {
            order_progress.visibility = View.VISIBLE
            order_empty_tv.visibility = View.GONE
            getDataList()
        } else {
            AlertDialog.Builder(this).setMessage("Please check your internet connection")
                .setPositiveButton("Yes") { dialog, which -> dialog.dismiss() }
                .setCancelable(false)
                .show()

            order_empty_tv.text = "Please enable internet \n After that Click on refresh from more options menu"
            order_empty_tv.visibility = View.VISIBLE
        }
    }

    //In this method unregister the event bus
    override fun onStop() {
        Log.i(TAG, "onStop")
        super.onStop()
        if (eventBus.isRegistered(this))
            eventBus.unregister(this)
    }

    //Initialising the views and setting up adapter to recyclerview
    private fun initViews() {
        Log.i(TAG, "initViews")
        order_rv.adapter = lastAdapter
        order_rv.hideFabOnScroll(order_add_fab)
        order_add_fab.setOnClickListener {
            startActivity(Intent(this, AddOrderDetailsActivity::class.java))
        }
    }

    /**
     * In this method we are making calls for firebase database and fetching the data from OrderDetails table
     * On DataChange method adding the list and updating the adapter
     */
    private fun getDataList() {
        val dataRef = database.getReference("OrderDetails")
        Log.i(TAG, "getDataList")
        dataRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.i(TAG, "onCancelled")
                order_progress.visibility = View.GONE
                when (p0) {
                    is ConnectException -> "Please check your connection".showAsToast(this@OrdersActivity)
                    else -> "Something went wrong Please try again.".showAsToast(this@OrdersActivity)
                }
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.i(TAG, "onDataChange")
                listOfData.clear()
                allList.clear()
                if (dataSnapshot.children.toMutableList().isNotEmpty()) {
                    for (data in dataSnapshot.children) {
                        val orderDetails = data.getValue(OrderDetails::class.java)!!
                        listOfData.add(orderDetails)
                    }
                    allList.addAll(listOfData)
                    if (listOfData.isNotEmpty()) {
                        order_empty_tv.visibility = View.GONE
                    } else {
                        order_empty_tv.visibility = View.VISIBLE
                        order_empty_tv.text = "No results found"
                    }
                    lastAdapter.notifyDataSetChanged()
                }
                order_progress.visibility = View.GONE
                Log.i(TAG, "get onDataChange list Size = ${listOfData.size}")
            }

        })

    }

    /**
     * In this fun we are initialising the Last adapter and setting up the item views
     * binding the data to views
     */
    private fun populateList(): LastAdapter {
        return LastAdapter.with(listOfData, BR.orderDetails)
            .map<OrderDetails, ItemOrderDetailsBinding>(R.layout.item_order_details) {
                onBind {
                    val order = binding.orderDetails!!
                    view.item_customer_address_tv.text = order.customer_address
                    view.item_customer_phn_tv.text = order.customer_phone.toString()
                    view.item_order_date_tv.text = order.order_due_date?.changeToString()
                    val total = "Total : ${order.order_total}"
                    view.item_order_total_tv.text = total

                    view.item_order_more_iv.setOnClickListener {
                        view.setBackgroundColor(Color.parseColor("#EFF0F1"))
                        initPopupMenu(order, view, position)
                    }
                }
            }.into(order_rv)
    }

    //Initialing the popup menu options
    private fun initPopupMenu(order: OrderDetails, view: View, position: Int) {
        val popup = PopupMenu(this, view.item_order_more_iv, Gravity.CENTER)
        popup.menu.add(getString(R.string.item_menu_edit))
        popup.menu.add(getString(R.string.item_menu_delete))
        showPopup(popup, view, order, position)
    }

    //Displaying the popup menu and options click listeners
    private fun showPopup(
        menu: PopupMenu,
        view: View,
        order: OrderDetails,
        position: Int
    ) {
        menu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.title) {
                getString(R.string.item_menu_edit) -> {
                    onUpdateEvent(OrderAction.EDIT, order, position)
                }
                getString(R.string.item_menu_delete) -> {
                    onUpdateEvent(OrderAction.DELETE, order, position)
                }
            }
            true
        }
        menu.show()

        menu.setOnDismissListener {
            view.setBackgroundResource(android.R.color.white)
        }
    }

    //On updating the particular order details
    private fun onUpdateEvent(action: OrderAction, order: OrderDetails, position: Int) {
        Log.i(TAG, "fireEditEvent action = $action")
        if (action == OrderAction.DELETE) {
            deleteConfirmation(order, position)
        } else {
            eventBus.postSticky(EditDetailsEvent(orderDetails = order))
            startActivity(Intent(this, AddOrderDetailsActivity::class.java))
        }
    }

    //Dispalys a dialog message for delete confirmation
    private fun deleteConfirmation(order: OrderDetails, position: Int) {
        AlertDialog.Builder(this)
            .setMessage("Do you want to delete this order")
            .setPositiveButton("Yes") { dialogInterface, which ->
                deleteOrder(order, position)
                dialogInterface.dismiss()
            }
            .setNegativeButton("No") { dialogInterface, which ->
                dialogInterface.dismiss()
            }
            .show()
    }

    /**
     * On delete operation, making a call to firebase data and deleting particular node in that table
     * deletes the node by unique order number
     */
    private fun deleteOrder(order: OrderDetails, position: Int) {
        val dataRef = database.getReference()
        val query = dataRef.child("OrderDetails").orderByChild("order_number")
            .equalTo(order.order_number)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.i(TAG, "onCancelled error = ${p0.message}")
                when (p0) {
                    is ConnectException -> "Please check your connection".showAsToast(this@OrdersActivity)
                    else -> "Something went wrong Please try again.".showAsToast(this@OrdersActivity)
                }
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (orderSnapshot in dataSnapshot.children) {
                    Log.i(TAG, "onDataChange delete action")
                    orderSnapshot.ref.removeValue()
                    lastAdapter.notifyItemRemoved(position)
                }
                if (listOfData.size == 1) {
                    listOfData.removeAt(index = position)
                    lastAdapter.notifyItemRemoved(position)
                }
                "Order deleted".showAsToast(this@OrdersActivity)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_orders_activity, menu)
        initSearch(menu!!)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_refresh -> getDataList()
            R.id.menu_logout -> logOutDialog()
        }
        return false
    }

    //Search functionality
    private fun initSearch(menu: Menu) {
        val search = MenuItemCompat.getActionView(menu.findItem(R.id.menu_search)) as SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        search.setSearchableInfo(searchManager.getSearchableInfo(this.componentName))
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { filterList(it) }
                return true
            }
        })
    }

    //filters the list for given text in search field
    private fun filterList(text: String) {
        listOfData.clear()
        listOfData.addAll(allList.filter {
            it.customer_name.contains(text, true) || it.order_name.contains(text, true)
        })
        lastAdapter.notifyDataSetChanged()

        if (listOfData.isEmpty()) {
            order_empty_tv.visibility = View.VISIBLE
            order_empty_tv.text = "No results found"
        } else {
            order_empty_tv.visibility = View.GONE
        }
    }

    //Logout dialog on confirmation logout from app
    private fun logOutDialog() {
        AlertDialog.Builder(this).setMessage("Do you want to logout")
            .setPositiveButton("Yes") { dialog, which ->
                dialog.dismiss()
                logoutSession()
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }.show()
    }

    //Removes the user details from kotpref (Shared preference)
    private fun logoutSession() {
        UserPreference.clear()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    //Checks for network connection
    private fun isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return cm.activeNetworkInfo != null
    }

    //Required dummy subscribe event for eventbus
    @Subscribe
    fun onEvent(event: DummyEvent) {

    }
}

