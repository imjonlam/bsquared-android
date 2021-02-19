package b.squared

import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

private const val TAG = "MY_APP_DEBUG_TAG"

const val MESSAGE_READ: Int = 0
const val MESSAGE_WRITE: Int = 1
const val MESSAGE_TOAST: Int = 2

class MyBluetoothService(private val handler: Handler) {

    private inner class ConnectedThread(private val mmSocket: BluetoothSocket): Thread() {
        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024)

        override fun run() {
            var numBytes: Int

            while(true) {
                numBytes = try {
                    mmInStream.read(mmBuffer)
                    Log.i(TAG, mmBuffer.toString())
                } catch(e: IOException) {
                    Log.d(TAG, "Input stream was disconnected", e)
                    break
                }

//                val readMsg = handler.obtainMessage(
//                    MESSAGE_READ, numBytes, -1, mmBuffer)
//                readMsg.sendToTarget()
            }
        }

        fun write(bytes: ByteArray) {}

        fun cancel() {
            try {
                mmSocket.close()
            } catch(e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }
}