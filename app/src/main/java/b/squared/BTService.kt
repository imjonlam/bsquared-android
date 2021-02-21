package b.squared

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.os.Handler
import android.util.Log
import java.io.*
import java.util.*

class BTService(private val btHandler: Handler) {
    /* Setup */
    private var currState = Constants.STATE_NONE
    private val btAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private lateinit var worker: ServiceWorker

    /* Start worker */
    @Synchronized fun start(address: String) {
        val btDevice: BluetoothDevice = btAdapter.getRemoteDevice(address)

        worker = ServiceWorker(btDevice)
        worker.start()
    }

    /* Stop worker */
    @Synchronized fun stop() {
        currState = Constants.STATE_NONE
        worker.cancel() // todo: change to nullable
    }

    @Synchronized fun notifyConnected() {
        val bundle = Bundle()
        val message = btHandler.obtainMessage(Constants.HANDLER_CONNECTED)
        bundle.putString(Constants.MESSAGE_TOAST, "Connected")
        message.data = bundle
        btHandler.sendMessage(message)
    }

    @Synchronized fun notifyStop(state: Int) {
        // notify activity
        val bundle = Bundle()
        val message = btHandler.obtainMessage(Constants.HANDLER_STOP)

        bundle.putString(Constants.MESSAGE_TOAST, when(state) {
            Constants.STATE_FAILED -> "Unable to connect with device"
            Constants.STATE_LOST -> "Lost connection to device"
            else -> "Bluetooth service has ended"
        })
        message.data = bundle
        btHandler.sendMessage(message)
    }

    /* Stream data to activity */
    @Synchronized fun stream(data: String) {
        val bundle = Bundle()
        val message = btHandler.obtainMessage(Constants.HANDLER_STREAM)
        bundle.putString(Constants.MESSAGE_INCOMING, data)
        message.data = bundle
        btHandler.sendMessage(message)
    }

    private inner class ServiceWorker(btDevice: BluetoothDevice): Thread() {
        /* Setup */
        private val uuid: UUID = UUID.fromString(Constants.UUID)
        private val btSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            btDevice.createRfcommSocketToServiceRecord(uuid)
        }

        init {
            currState = Constants.STATE_CONNECTING
        }

        override fun run() {
            try {
                btSocket?.connect()
                currState = Constants.STATE_CONNECTED
                notifyConnected()
            } catch (e: IOException) {
                Log.e(Constants.TAG, "unable to connect to device, closing socket", e)
                cancel()
                notifyStop(Constants.STATE_FAILED)
                return
            }

            val rx = btSocket?.inputStream
            val reader = BufferedReader(InputStreamReader(rx!!))
            try { // todo: throws IOError on timeout (sleep)
                reader.forEachLine {
                    if (it.split(",").size == Constants.EXPECTED_LENGTH) {
                        stream(it) // todo: figure out bug where half the stream is read after reconnection
                    }
                }
            } catch (e: IOException) {
                Log.e(Constants.TAG, "lost connection, closing socket", e)
                cancel()
                notifyStop(Constants.STATE_LOST)
            }
        }

        fun cancel() {
            try {
                btSocket?.close()
            } catch (e: IOException) {
                Log.e(Constants.TAG, "unable to close socket", e)
            }
        }
    }
}