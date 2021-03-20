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
    private var worker: ServiceWorker? = null

    /**
     * Start service worker for given bluetooth device address
     */
    @Synchronized fun start(address: String) {
        val btDevice: BluetoothDevice = btAdapter.getRemoteDevice(address)

        worker = ServiceWorker(btDevice)
        worker!!.start()
    }

    /**
     * Stop service worker
     */
    @Synchronized fun stop() {
        currState = Constants.STATE_NONE
        worker?.cancel()
        worker = null
    }

    /**
     * Returns the address of connected device
     */
    @Synchronized fun getAddress(): String? {
        return worker?.getAddress()
    }

    /**
     * Sends notification to main activity
     */
    @Synchronized private fun notifyActivity(state: Int, handler: Int) {
        val bundle = Bundle()
        val message = btHandler.obtainMessage(handler)

        bundle.putString(Constants.MESSAGE_TOAST, when(state) {
            Constants.STATE_CONNECTING -> "Connecting"
            Constants.STATE_CONNECTED -> "Connected"
            Constants.STATE_FAILED -> "Unable to connect with device"
            Constants.STATE_LOST -> "Lost connection to device"
            Constants.STATE_CLOSED -> "Connection closed"
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
        private var reader: BufferedReader? = null

        init {
            currState = Constants.STATE_CONNECTING
        }

        override fun run() {
            notifyActivity(Constants.STATE_CONNECTING, Constants.HANDLER_CONNECTION)
            // connect to the device
            try {
                btSocket?.connect()
                currState = Constants.STATE_CONNECTED
                notifyActivity(Constants.STATE_CONNECTED, Constants.HANDLER_CONNECTION)
            } catch (e: IOException) {
                Log.e(Constants.TAG, "unable to connect to device, closing socket", e)
                notifyActivity(Constants.STATE_FAILED, Constants.HANDLER_STOP)
                close()
                return
            }

            // get input stream and read in data
            val rx = btSocket?.inputStream
            reader = BufferedReader(InputStreamReader(rx!!))

            try {
                while (!currentThread().isInterrupted) {
                    stream(reader!!.readLine())
                }

                if (interrupted()) {
                    throw InterruptedException("thread was interrupted")
                }

            } catch (e: InterruptedException) {
                Log.i(Constants.TAG, "connection closed", e)
                notifyActivity(Constants.STATE_CLOSED, Constants.HANDLER_STOP)
            } catch (e: IOException) {
                Log.e(Constants.TAG, "lost connection, closing socket", e)
                notifyActivity(Constants.STATE_LOST, Constants.HANDLER_STOP)
            } finally {
                close()
            }
        }

        private fun close() {
            btSocket?.close()
            reader?.close()
        }

        /**
         * Returns the address of the connected device
         */
        fun getAddress(): String? {
            return btSocket?.remoteDevice?.address
        }

        /**
         * Stops the thread and closes open bluetooth socket, StreamReaders
         */
        fun cancel() {
            interrupt()
        }
    }
}