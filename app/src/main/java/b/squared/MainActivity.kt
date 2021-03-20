package b.squared

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlin.math.roundToInt

class MainActivity: AppCompatActivity() {
    /* Setup */
    private lateinit var btService: BTService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.tbMain))

        setupPermissions()
        btService = BTService(btHandler)
    }

    /**
     * Stop the bluetooth service
     */
    override fun onDestroy() {
        super.onDestroy()
        btService.stop()
    }

    /**
     * Creates a custom toolbar
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * Request permissions at runtime
     */
    private fun setupPermissions() {
        val perms: Array<String> = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )

        fun hasPermissions(context: Context, vararg permissions: String): Boolean = permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!hasPermissions(this, *perms)) {
            ActivityCompat.requestPermissions(this, perms, Constants.MULTIPLE_PERMISSIONS)
        }
    }

    /**
     * Check if any required permissions were granted/denied
     * TODO: add rejection handling
     */
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

    /**
     * Handle custom toolbar menu options
     */
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            val getDeviceIntent = Intent(this, BTListActivity::class.java)
            startActivityForResult(getDeviceIntent, Constants.REQUEST_CONNECT_DEVICE)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    /**
     * Handle intent responses such as starting a new bluetooth service
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            Constants.REQUEST_CONNECT_DEVICE -> {
                // start connection
                if (resultCode == Activity.RESULT_OK) {
                    val connected = btService.getAddress()
                    Log.i(Constants.TAG, "PREVIOUS ADDRESS $connected")
                    val address = data?.extras?.getString(Constants.MESSAGE_DEVICE_ADDRESS)
                    address?.let {
                        if (connected.isNullOrEmpty() || connected != address) {
                            btService.stop()
                            btService.start(it)
                        }
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
                // notify user bluetooth service has stopped
                Constants.HANDLER_STOP -> {
                    btService.stop()
                    Toast.makeText(this@MainActivity,
                            msg.data.getString(Constants.MESSAGE_TOAST),
                            Toast.LENGTH_LONG).show()
                }
                // notify user bluetooth service is connecting/connected
                Constants.HANDLER_CONNECTION -> {
                    Toast.makeText(this@MainActivity,
                            msg.data.getString(Constants.MESSAGE_TOAST),
                            Toast.LENGTH_LONG).show()
                }
                // stream data from Arduino
                Constants.HANDLER_STREAM -> {
                    val stream = msg.data.getString(Constants.MESSAGE_INCOMING)
                    stream?.let { getTemps(it) }
                }
            }
        }
    }

    /**
     * Display on screen the parsed tire temperatures
     */
    private fun displayTemps(ids: HashMap<String, Int>, temps: List<Int>) {
        /**
         * Removes all Constants.BAD_DATA from list, applies average.
         * Returns result as string or "NaN" if list is empty being filtered.
         */
        fun cleanedAverage(lst: List<Int>): String {
            val cleaned = lst.filter { it != Constants.BAD_DATA }
            val count = cleaned.count()

            return if (count == 0) Constants.NaN else cleaned.sum().div(count).toString()
        }

        // get all required layout items
        val tireView: TextView = findViewById<TextView>(ids["tireID"]!!)
        val innerView: TextView = findViewById<TextView>(ids["innerID"]!!)
        val middleView: TextView = findViewById<TextView>(ids["middleID"]!!)
        val outerView: TextView = findViewById<TextView>(ids["outerID"]!!)
        val maxView: TextView = findViewById<TextView>(ids["maxID"]!!)
        val minView: TextView = findViewById<TextView>(ids["minID"]!!)

        // filter out padding
        val cleaned = temps.filter { t -> t != Constants.BAD_DATA }

        // set values
        innerView.text = cleanedAverage(temps.slice(0..4))
        middleView.text = cleanedAverage(temps.slice(5..10))
        outerView.text = cleanedAverage(temps.slice(11..15))
        maxView.text = if (cleaned.isEmpty()) Constants.NaN else cleaned.max().toString()
        minView.text = if (cleaned.isEmpty()) Constants.NaN else cleaned.min().toString()
        tireView.text = if (cleaned.isEmpty()) Constants.NaN else
            cleaned.average().roundToInt().toString()

        // apply colours
        val gradient = temps.map { temp ->
            when(temp) {
                in -30..69 -> Color.parseColor("#007B13")
                in 70..99 -> Color.parseColor("#907100")
                in 100..200 -> Color.parseColor("#FF0000")
                else -> Color.parseColor("#808080")
            }
        }

        // display the colour gradients
        val gd = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                gradient.toIntArray()
        )
        gd.cornerRadius = 5f
        tireView.background = gd
    }

    /**
     * Parse the streamed tire temperature data
     */
    private fun getTemps(message: String) {
        // setup associated layout item IDs
        val flIDSet = hashMapOf("tireID" to R.id.tvFL, "innerID" to R.id.tvFLI,
                "middleID" to R.id.tvFLM, "outerID" to R.id.tvFLO,
                "maxID" to R.id.tvFLMax, "minID" to R.id.tvFLMin)
        val frIDSet = hashMapOf("tireID" to R.id.tvFR, "innerID" to R.id.tvFRI,
                "middleID" to R.id.tvFRM, "outerID" to R.id.tvFRO,
                "maxID" to R.id.tvFRMax, "minID" to R.id.tvFRMin)
        val rlIDSet = hashMapOf("tireID" to R.id.tvRL, "innerID" to R.id.tvRLI,
                "middleID" to R.id.tvRLM, "outerID" to R.id.tvRLO,
                "maxID" to R.id.tvRLMax, "minID" to R.id.tvRLMin)
        val rrIDSet = hashMapOf("tireID" to R.id.tvRR, "innerID" to R.id.tvRRI,
                "middleID" to R.id.tvRRM, "outerID" to R.id.tvRRO,
                "maxID" to R.id.tvRRMax, "minID" to R.id.tvRRMin)

        // convert streamed data from string to an array of integers
        val padding = List(64){Constants.BAD_DATA}
        val data = message.replace("^,|,$".toRegex(), "")
                .split(",")
                .map { it.toInt() }
        val temps = (data + padding).slice(0..63)

        // display each corner's information onto the screen
        displayTemps(flIDSet, temps.slice(0..15))
        displayTemps(frIDSet, temps.slice(16..31))
        displayTemps(rlIDSet, temps.slice(32..47))
        displayTemps(rrIDSet, temps.slice(48..63))
    }
}