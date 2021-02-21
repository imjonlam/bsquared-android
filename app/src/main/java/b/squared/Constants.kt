package b.squared

object Constants {
    // DEBUG
    const val TAG = "BSQUARED"

    // PERMISSIONS
    const val MULTIPLE_PERMISSIONS = 777

    // Intents
    const val REQUEST_CONNECT_DEVICE = 0
    const val REQUEST_ENABLE_DEVICE = 1

    // Bluetooth Socket States
    const val STATE_NONE = 0
    const val STATE_CONNECTING = 1
    const val STATE_CONNECTED = 2
    const val STATE_FAILED = 3
    const val STATE_LOST = 4

    // Bluetooth Device Attributes
    const val UUID = "00001101-0000-1000-8000-00805F9B34FB"
    val VALID_ADDRESSES = arrayOf("00:14:03:05:0B:A6")
    const val EXPECTED_LENGTH = 64

    // Intents
    const val MESSAGE_DEVICE_ADDRESS = "device_address"

    // Bundles
    const val HANDLER_STOP = 0
    const val HANDLER_CONNECTED = 1
    const val HANDLER_STREAM = 2
    const val MESSAGE_TOAST = "toast_message"
    const val MESSAGE_INCOMING = "incoming_message"
}
