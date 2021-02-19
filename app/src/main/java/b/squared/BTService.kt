package b.squared

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.util.*

class BTService() {
    /* Setup */
    private var currState = Constants.STATE_NONE
    private val btAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private lateinit var connectThread: ConnectThread

    /* Connect to given device */
    @Synchronized fun connect(device: BluetoothDevice) {
        connectThread = ConnectThread(device)
        connectThread.start()
    }

    /* Process successful connection */
    @Synchronized fun connected() {
        // TODO: Null check and close threads

    }

    private inner class ConnectThread(device: BluetoothDevice) : Thread() {
        /* Setup */
        private val uuid: UUID = device.uuids?.get(0)!!.uuid
        private val btSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(uuid)
        }

        init {
            currState = Constants.STATE_CONNECTING
        }

        override fun run() {
            btAdapter.cancelDiscovery()

            btSocket?.use { socket ->
                socket.connect()
            }
        }

        fun cancel() {
            try {
                btSocket?.close()
            } catch (e: IOException) {
                Log.e(Constants.TAG, "Unable to close the client socket", e)
            }
        }
    }
}