package b.squared

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BTListActivity: AppCompatActivity(), BTAdapter.OnItemClickListener {
    /* Setup */
    private lateinit var pairedAdapter: BTAdapter
    private lateinit var bluetoothAdapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setup window
        setContentView(R.layout.activity_btlist)
        setSupportActionBar(findViewById(R.id.tbConnect))

        supportActionBar?.apply {
            title = "Connect"
            setDisplayHomeAsUpEnabled(true)
        }

        // initialize paired device adapter lists
        pairedAdapter = BTAdapter(mutableListOf(), this)

        // create recycler view
        findViewById<RecyclerView>(R.id.rvPaired).apply{
            adapter = pairedAdapter
            layoutManager = LinearLayoutManager(BTListActivity())
        }

        // initialize local bluetooth adapter
        // TODO: add check if bluetooth supported
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // enable bluetooth if OFF
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 102)
        }

        // retrieve list of paired devices and add to recycler view
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
        pairedDevices?.forEach { device ->
            val newDevice = BTDevice(device.name, device.address)
            pairedAdapter.addConnection(newDevice)
        }
    }

    /* Return to previous activity */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    /* Handling recycler view item clicks */
    override fun onItemClick(selected: BTDevice) {
        // attempt to connect with device
        val btDevice = bluetoothAdapter.getRemoteDevice(selected.address)
        val btService = BTService()
        btService.connect(btDevice)
    }

}