package b.squared

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PairedAdapter(private val items: MutableList<PairedDevice>) : RecyclerView.Adapter<PairedAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.tvDeviceName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.paired_device,
                parent,
                false
            )
        )
    }

    fun addConnection(device: PairedDevice) {
        items.add(device)
        Log.i("DEBUG", device.name)
        notifyItemInserted(items.size - 1)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = items[position].name
    }
}