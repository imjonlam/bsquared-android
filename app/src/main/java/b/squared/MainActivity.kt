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
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import org.w3c.dom.Text
import java.lang.NumberFormatException
import kotlin.math.roundToInt

class MainActivity: AppCompatActivity() {
    /* Setup */
    private lateinit var btService: BTService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.tbMain))

        setupPermissions()
    }

    /* Create custom toolbar */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /* Request permissions at runtime */
    private fun setupPermissions() {
        val perms: Array<String> = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        fun hasPermissions(context: Context, vararg permissions: String): Boolean = permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!hasPermissions(this, *perms)) {
            ActivityCompat.requestPermissions(this, perms, Constants.MULTIPLE_PERMISSIONS)
        }
    }

    // TODO: add rejection handling
    /* Check if certain permissions were rejected */
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

    /* Handle custom toolbar items */
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            val getDeviceIntent = Intent(this, BTListActivity::class.java)
            startActivityForResult(getDeviceIntent, Constants.REQUEST_CONNECT_DEVICE)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    /* Handle intent responses */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            Constants.REQUEST_CONNECT_DEVICE -> {
                // start connection
                if (resultCode == Activity.RESULT_OK) {
                    val address = data?.extras?.getString(Constants.MESSAGE_DEVICE_ADDRESS)
                    address?.let {
                        btService = BTService(btHandler)
                        btService.start(it)
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
                Constants.HANDLER_STOP -> {
                    btService.stop()
                    Toast.makeText(this@MainActivity,
                            msg.data.getString(Constants.MESSAGE_TOAST),
                            Toast.LENGTH_LONG).show()
                }
                Constants.HANDLER_CONNECTED -> {
                    Toast.makeText(this@MainActivity,
                            msg.data.getString(Constants.MESSAGE_TOAST),
                            Toast.LENGTH_SHORT).show()
                }
                Constants.HANDLER_STREAM -> {
                    val stream = msg.data.getString(Constants.MESSAGE_INCOMING)
                    stream?.let { getTemps(it) }
                }
            }
        }
    }

    /* Display tire temperatures */
    private fun displayTemps(ids: HashMap<String, Int>, temps: List<Int>) {
        val tireView: TextView = findViewById<TextView>(ids["tireID"]!!)
        val innerView: TextView = findViewById<TextView>(ids["innerID"]!!)
        val middleView: TextView = findViewById<TextView>(ids["middleID"]!!)
        val outerView: TextView = findViewById<TextView>(ids["outerID"]!!)
        val maxView: TextView = findViewById<TextView>(ids["maxID"]!!)
        val minView: TextView = findViewById<TextView>(ids["minID"]!!)

        maxView.text = temps.max().toString()
        minView.text = temps.min().toString()
        tireView.text = temps.average().roundToInt().toString()
        innerView.text = temps.slice(0..4).average().roundToInt().toString()
        middleView.text = temps.slice(5..10).average().roundToInt().toString()
        outerView.text = temps.slice(11..15).average().roundToInt().toString()

        val gradient = temps.map { temp ->
            when(temp) {
                in 0..25 -> Color.parseColor("#00ff11")
                in 26..50 -> Color.parseColor("#7ce800")
                in 51..75 -> Color.parseColor("#a9d000")
                in 76..100 -> Color.parseColor("#c9b500")
                in 101..125 -> Color.parseColor("#e19800")
                in 126..150 -> Color.parseColor("#f27700")
                in 151..175 -> Color.parseColor("#fc4f00")
                in 176..200 -> Color.parseColor("#ff0000")
                else -> Color.parseColor("#000000")
            }
        }

        val gd = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                gradient.toIntArray()
        )
        gd.cornerRadius = 5f
        tireView.background = gd
    }

    /* Prepare tire temperature data */
    private fun getTemps(message: String) {
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


        val temps = message.split(",").map { it.toInt() }
        displayTemps(flIDSet, temps.slice(0..15))
        displayTemps(frIDSet, temps.slice(16..31))
        displayTemps(rlIDSet, temps.slice(32..47))
        displayTemps(rrIDSet, temps.slice(48..63))
    }
}