package b.squared

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PairedAdapter(private val items: MutableList<PairedDevice>,
                    private val listener: OnItemClickListener
):
    RecyclerView.Adapter<PairedAdapter.ViewHolder>() {

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener{
        val textView: TextView = view.findViewById(R.id.tvDeviceName)

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
                R.layout.paired_device,
                parent,
                false
            )
        )
    }

    fun addConnection(device: PairedDevice) {
        items.add(device)
        notifyItemInserted(items.size - 1)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = items[position].name
    }

    interface OnItemClickListener {
        fun onItemClick(device: PairedDevice)
    }
}