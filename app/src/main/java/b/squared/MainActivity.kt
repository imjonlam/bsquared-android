package b.squared

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat

class MainActivity: AppCompatActivity() {
    /* Setup */
    private lateinit var btService: BTService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.tbMain))

        setupPermissions()
    }

    /* Create custom toolbar */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /* Request permissions at runtime */
    private fun setupPermissions() {
        val perms: Array<String> = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        fun hasPermissions(context: Context, vararg permissions: String): Boolean = permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!hasPermissions(this, *perms)) {
            ActivityCompat.requestPermissions(this, perms, Constants.MULTIPLE_PERMISSIONS)
        }
    }

    // TODO: add rejection handling
    /* Check if certain permissions were rejected */
    override fun onRequestPermissionsResult(requestCode: Int,
        permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            Constants.MULTIPLE_PERMISSIONS -> {
                for (i in permissions.indices) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Log.i(Constants.TAG, "${permissions[i]} denied")
                    } else {
                        Log.i(Constants.TAG, "${permissions[i]} granted")
                    }
                }
            }
        }
    }

    /* Handle custom toolbar items */
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            val getDeviceIntent = Intent(this, BTListActivity::class.java)
            startActivityForResult(getDeviceIntent, Constants.REQUEST_CONNECT_DEVICE)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    /* Handle intent responses */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            Constants.REQUEST_CONNECT_DEVICE -> {
                // start connection
                if (resultCode == Activity.RESULT_OK) {
                    val address = data?.extras?.getString(Constants.MESSAGE_DEVICE_ADDRESS)
                    address?.let {
                        btService = BTService(btHandler)
                        btService.start(it)
                    }
                }
            }
        }
    }

    /* Handler */
    private val btHandler = object: Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what) {
                Constants.HANDLER_STOP -> {
                    btService.stop()
                    Toast.makeText(this@MainActivity,
                            msg.data.getString(Constants.MESSAGE_TOAST),
                            Toast.LENGTH_SHORT).show()
                }
                Constants.HANDLER_STREAM -> {
                    Toast.makeText(this@MainActivity,
                            msg.data.getString(Constants.MESSAGE_INCOMING),
                            Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}  