package ku.cwk.digivital.ui.track.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ku.cwk.digivital.R
import ku.cwk.digivital.databinding.ItemReportTrackBinding
import ku.cwk.digivital.ui.track.model.data.TestDetailData
import ku.cwk.jobsconnect.interfaces.TagDataListener

class TrackAdapter(
    private val dataList: MutableList<TestDetailData>,
    private val tagDataListener: TagDataListener
) : RecyclerView.Adapter<TrackAdapter.CustomViewHolder>() {

    inner class CustomViewHolder(private val binding: ItemReportTrackBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val context = binding.root.context

        init {
            binding.editImg.setOnClickListener {
                tagDataListener.sendData("", adapterPosition)
            }
        }

        internal fun bind(data: TestDetailData) {
            binding.apply {

                val valueData = data.trackData
                testTxt.text = data.testName
                valueTxt.text = valueData.valueText
                rangeTxt.text = context.getString(
                    R.string.ref_range_format,
                    valueData.refRangeI.toString(), valueData.refRangeII.toString()
                )
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): CustomViewHolder {
        val itemView =
            ItemReportTrackBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
}