package com.example.myorders

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import permissions.dispatcher.*

@RuntimePermissions
class LoginActivity : AppCompatActivity(), MyLocationListener.SetonLocationListner {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()
        LoginActivityPermissionsDispatcher.onGetLocationWithCheck(this)
        initViews()
    }

    /**
     * Initials the views and button click listeners on all validation success launches MyOrders Actvitiy
     */
    private fun initViews() {
        initTextChangeListeners()
        login_tv.setOnClickListener {
            val email = input_email_et.text.toString().trim()
            val password = input_password_et.text.toString().trim()
            if (validateData(email, password)) {
                UserPreference.run {
                    userEmail = email
                    this.password = password
                    rememberUser = remember_me_check_box.isChecked
                }
                startActivity(Intent(this, OrdersActivity::class.java))
                finish()
            }
        }

        remember_me_parent.setOnClickListener {
            remember_me_check_box.isChecked = !remember_me_check_box.isChecked
        }
    }

    private fun initTextChangeListeners() {
        arrayOf(input_email_et, input_password_et).forEach {
            it.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    //It just hides dose not change the error text
                    input_password_tl.error = null
                    input_email_tl.error = null
                }
            })
        }
    }

    /**
     * Check for validations for email and password
     */
    private fun validateData(email: String, password: String): Boolean {
        return if (email.isEmpty()) {
            input_email_tl.error = "Enter email"
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            input_email_tl.error = "Enter valid email"
            false
        } else if (password.isEmpty()) {
            input_password_tl.error = "Enter password"
            false
        } else if (password.length < 6) {
            input_password_tl.error = "Password can't be less that 6 characters"
            false
        } else {
            true
        }
    }

    //Checks for location permission
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

    override fun onSetLocation(city: String, state: String) {
        Log.i("Login", "onSetLocation $state")
    }
}
