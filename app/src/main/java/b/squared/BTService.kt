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

    /**
     * Start service worker for given bluetooth device address
     */
    @Synchronized fun start(address: String) {
        val btDevice: BluetoothDevice = btAdapter.getRemoteDevice(address)

        worker = ServiceWorker(btDevice)
        worker.start()
    }

    /**
     * Stop service worker
     */
    @Synchronized fun stop() {
        currState = Constants.STATE_NONE
        worker.cancel()
    }

    /**
     * Notify user that the device is connected
     */
    @Synchronized fun notifyConnected() {
        val bundle = Bundle()
        val message = btHandler.obtainMessage(Constants.HANDLER_CONNECTED)
        bundle.putString(Constants.MESSAGE_TOAST, "Connected")
        message.data = bundle
        btHandler.sendMessage(message)
    }

    /**
     * Notify user that the device has stopped
     */
    @Synchronized fun notifyStop(state: Int) {
        // stop worker
        worker.cancel()

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

    /**
     * Stream received data to main activity
     */
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
        private lateinit var reader: BufferedReader

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
                notifyStop(Constants.STATE_FAILED)
                return
            }

            // get input stream and read in data
            val rx = btSocket?.inputStream
            reader = BufferedReader(InputStreamReader(rx!!))
            try {
                reader.forEachLine {
                    stream(it)
                }
            } catch (e: IOException) {
                Log.e(Constants.TAG, "lost connection, closing socket", e)
                notifyStop(Constants.STATE_LOST)
            }
        }

        /**
         * Stops the thread and closes open bluetooth socket, StreamReaders
         */
        fun cancel() {
            try {
                btSocket?.close()
                reader.close()
            } catch (e: IOException) {
                Log.e(Constants.TAG, "unable to close socket", e)
            }
        }
    }
}