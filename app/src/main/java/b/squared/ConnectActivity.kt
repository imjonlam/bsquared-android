package b.squared

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ConnectActivity : AppCompatActivity() {

    private lateinit var pairedAdapter: PairedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)
        setSupportActionBar(findViewById(R.id.tbConnect))

        supportActionBar?.apply {
            title = "Connect"
            setDisplayHomeAsUpEnabled(true)
        }

        pairedAdapter = PairedAdapter(mutableListOf())

        findViewById<RecyclerView>(R.id.rvPaired).apply{
            adapter = pairedAdapter
            layoutManager = LinearLayoutManager(ConnectActivity())
        }

        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Log.e("ERROR", "BLUETOOTH NOT SUPPORTED")
        }
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 102)
        }

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            val newDevice = PairedDevice(device.name, device.address)
            pairedAdapter.addConnection(newDevice)
            Log.i("DEBUG", pairedAdapter.itemCount.toString())
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}