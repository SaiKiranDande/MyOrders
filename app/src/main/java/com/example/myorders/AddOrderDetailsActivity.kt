package com.example.myorders

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_add_order_details.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import permissions.dispatcher.*
import java.util.*

/**
 * In this activity we are adding and updating the Order details
 * And check for location access, it will take the user current geolocation and adds to edit text view
 */

@RuntimePermissions
class AddOrderDetailsActivity : AppCompatActivity(), SetTime.SetonDateTimeSelectListener,
    MyLocationListener.SetonLocationListner {

    val TAG = AddOrderDetailsActivity::class.java.simpleName
    private val database = FirebaseDatabase.getInstance()
    lateinit var eventBus: EventBus
    private var editOrder: OrderDetails? = null
    var maxId: Long = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_order_details)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        eventBus = EventBus.getDefault()
        AddOrderDetailsActivityPermissionsDispatcher.onGetLocationWithCheck(this)
    }

    override fun onStart() {
        super.onStart()
        if (!eventBus.isRegistered(this))
            eventBus.register(this)
    }

    override fun onStop() {
        super.onStop()
        if (eventBus.isRegistered(this))
            eventBus.unregister(this)
    }

    override fun onResume() {
        super.onResume()
        title = if (editOrder == null)
            "Add Order details"
        else "Update Order details"
        locationStatusCheck()
        checkForInternetConection()
        setUpDate()
        initFirebase()
    }

    //Calls setTime
    private fun setUpDate() {
        val minDate = if (editOrder != null) {
            if (editOrder!!.order_due_date!!.time <= Calendar.getInstance().time.time) {
                editOrder!!.order_due_date!!.time
            } else {
                Calendar.getInstance().time.time
            }
        } else {
            Calendar.getInstance().time.time
        }
        SetTime(
            order_due_date_et,
            this,
            minDate
        )
    }

    /**
     * Initialise the fire base and add the data object to the required table
     * if editOrder is not null the updated the required node by unique child key
     */
    private fun initFirebase() {
        val myRef = database.getReference("OrderDetails")
        Log.i(TAG, "initFirebase edit = $editOrder")
        if (editOrder == null) {
            myRef.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists())
                        maxId = dataSnapshot.childrenCount

                }
            })
        }
        order_save_btn.setOnClickListener {
            val orderName = order_name_et.text.toString().trim()
            val cusName = customer_name_et.text.toString().trim()
            val cusAddress = customer_address_et.text.toString().trim()
            val cusPhone = customer_phone_et.text.toString().trim()
            val orderDate = order_due_date_et.text.toString().trim()
            val orderTotal = order_total_et.text.toString().trim()
            Log.i(TAG, "initFirebase onclick edit = $editOrder")
            if (validateData(orderName, cusName, cusAddress, cusPhone, orderDate, orderTotal)) {
                val orderDetails = OrderDetails(
                    order_number = if (editOrder == null) System.currentTimeMillis().toString() else editOrder?.order_number,
                    order_name = orderName,
                    order_due_date = Date(orderDate),
                    customer_name = cusName,
                    customer_address = cusAddress,
                    customer_phone = cusPhone.toLong(),
                    order_total = orderTotal
                )
                if (editOrder == null) {
                    myRef.child((maxId + 1).toString()).setValue(orderDetails)
                    "Order Added".showAsToast(this@AddOrderDetailsActivity)

                } else {
                    val query = myRef.orderByChild("order_number")
                        .equalTo(editOrder!!.order_number)
                    query.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                        }

                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            Log.i(TAG, "update dataChange ${dataSnapshot.children.toMutableSet().size}")
                            for (data in dataSnapshot.getChildren()) {
                                Log.i(TAG, "update value ${dataSnapshot.children.toMutableSet().map { it.key }}")
                                myRef.child(data.key!!).setValue(orderDetails)
                                "Order updated".showAsToast(this@AddOrderDetailsActivity)
                            }
                        }

                    })
                }
                finish()
            }
        }
    }

    //populates the data on edit functionality
    private fun populateData() {
        if (editOrder != null) {
            order_save_btn.text = "Update"
        }
        editOrder?.run {
            order_name_et.setText(order_name)
            customer_name_et.setText(customer_name)
            customer_address_et.setText(customer_address)
            customer_phone_et.setText(customer_phone.toString())
            order_due_date_et.setText(order_due_date?.changeToString())
            order_total_et.setText(order_total)
        }
    }

    //Checks for each value validations
    private fun validateData(
        orderName: String, customerName: String, customerAddress: String, customerPhone: String,
        orderDate: String, orderTotal: String
    ): Boolean {
        when {
            orderName.isEmpty() -> {
                "Please enter order name".showAsToast(this)
                return false
            }
            customerName.isEmpty() -> {
                "Please enter customer name".showAsToast(this)
                return false
            }
            customerAddress.isEmpty() -> {
                "Please enable location for address".showAsToast(this)
                return false
            }
            customerPhone.isEmpty() -> {
                "Please enter phone number".showAsToast(this)
                return false
            }
            orderDate.isEmpty() -> {
                "Please enter order date".showAsToast(this)
                return false
            }
            orderTotal.isEmpty() -> {
                "Please enter order date".showAsToast(this)
                return false
            }
            else -> return true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
        }
        return false
    }

    @Subscribe(sticky = true)
    fun onEditDetailsEvent(event: EditDetailsEvent) {
        editOrder = event.orderDetails
        eventBus.removeStickyEvent(event)
        populateData()
    }


    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    fun onGetLocation() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            ) {
                val mLocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000,
                    10f,
                    MyLocationListener(this.applicationContext, this)
                );
            }
        }
    }

    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    fun showRationaleForFile(request: PermissionRequest) {
        AlertDialog.Builder(this)
            .setMessage("Need permission to access your location")
            .setPositiveButton("Allow") { dialog, which -> request.proceed() }
            .setNegativeButton("Deny") { dialog, button -> request.cancel() }
            .show()
    }

    @OnPermissionDenied(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    fun showDeniedForFile() {
        "Location permission denied".showAsToast(this)
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    fun showNeverAskForFile() {
        "Please go to app settings and allow permission for location".showAsToast(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        AddOrderDetailsActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults)
    }

    override fun onDateTimeSelected(calendar: Calendar, view: View) {
        Log.i(TAG, "calender time = ${calendar.time}")
        val et = view as EditText
        et.setText(calendar.time.changeToString())
    }

    override fun onSetLocation(city: String, state: String) {
        Log.i(TAG, "onSetLocation city $city")
        val address = "$city, $state"
        if (editOrder == null)
            customer_address_et.setText(address)
    }

    //Check for location GPS status
    private fun locationStatusCheck() {
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        }
    }

    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Your GPS seems to be disabled\nplease enable to continue")
            .setCancelable(false)
            .setPositiveButton("Yes",
                DialogInterface.OnClickListener { dialog, id -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) })
        val alert = builder.create()
        alert.show()
    }

    //Checks for network connection
    private fun isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return cm.activeNetworkInfo != null
    }

    private fun checkForInternetConection() {
        if (!isNetworkConnected())
            androidx.appcompat.app.AlertDialog.Builder(this).setMessage("Please check your internet connection")
                .setPositiveButton("Yes") { dialog, which -> dialog.dismiss() }
                .setCancelable(false)
                .show()
    }

}
