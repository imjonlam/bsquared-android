package b.squared

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RVAdapter(private val items: MutableList<BTDevice>,
                private val listener: OnItemClickListener
):
    RecyclerView.Adapter<RVAdapter.ViewHolder>() {

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener{
        val deviceTV: TextView = view.findViewById(R.id.tvDeviceName)
        val addressTV: TextView = view.findViewById(R.id.tvDeviceAddress)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            if (adapterPosition != RecyclerView.NO_POSITION) {
                listener.onItemClick(items[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.bt_device,
                parent,
                false
            )
        )
    }

    /**
     * Adds a new device to the list of paired devices
     */
    fun addConnection(device: BTDevice) {
        items.add(device)
        notifyItemInserted(items.size - 1)
    }

    /**
     * Returns the number of items in the list
     */
    override fun getItemCount(): Int {
        return items.size
    }

    /**
     * Display the name of the device in the ViewHolder
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.deviceTV.text = items[position].name
        holder.addressTV.text = items[position].address
    }

    /**
     * Proposes an onclick function to each item in the list
     */
    interface OnItemClickListener {
        fun onItemClick(device: BTDevice)
    }

}