package b.squared

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BTListActivity: AppCompatActivity(), RVAdapter.OnItemClickListener {
    /* Setup */
    private lateinit var rvAdapter: RVAdapter
    private lateinit var btAdapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setup window
        setContentView(R.layout.activity_btlist)
        setSupportActionBar(findViewById(R.id.tbConnect))
        supportActionBar?.apply {
            title = "Connect"
            setDisplayHomeAsUpEnabled(true)
        }

        // create recycler view
        rvAdapter = RVAdapter(mutableListOf(), this)
        findViewById<RecyclerView>(R.id.rvPaired).apply{
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(BTListActivity())
        }

        // start bluetooth adapter
        btAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!btAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_DEVICE)
        }

        // get paired bluetooth device list
        showDevices()
    }

    /**
     * Update bluetooth device list on restart
     */
    override fun onRestart() {
        super.onRestart()
        showDevices()
    }

    /**
     * Return to previous activity
     */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    /**
     * Handles recycler view item clicks
     */
    override fun onItemClick(btDevice: BTDevice) {
        // return the device address to MainActivity
        val returnDeviceIntent = Intent()
        returnDeviceIntent.putExtra(Constants.MESSAGE_DEVICE_ADDRESS, btDevice.address)
        setResult(Activity.RESULT_OK, returnDeviceIntent)
        finish()
    }

    /**
     * Retrieve list of paired devices and add to RecyclerView
     */
    private fun showDevices() {
        // retrieve list of paired devices and add to recycler view
        val pairedDevices: Set<BluetoothDevice>? = btAdapter.bondedDevices
        pairedDevices?.forEach {
            rvAdapter.addConnection(BTDevice(it.name, it.address))

//            if (it.name == Constants.VALID_DEVICE_NAME) {
//            }
        }
    }
}