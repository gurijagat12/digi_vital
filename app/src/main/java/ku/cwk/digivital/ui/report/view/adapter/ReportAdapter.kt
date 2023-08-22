package ku.cwk.digivital.ui.report.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ku.cwk.digivital.R
import ku.cwk.digivital.databinding.ItemReportListBinding
import ku.cwk.digivital.ui.track.model.data.TestReportData
import ku.cwk.digivital.util.convertDateFormat
import ku.cwk.jobsconnect.interfaces.TagDataListener

class ReportAdapter(
    private val dataList: ArrayList<TestReportData>,
    private val tagDataListener: TagDataListener
) : RecyclerView.Adapter<ReportAdapter.CustomViewHolder>() {

    inner class CustomViewHolder(private val binding: ItemReportListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val context = binding.root.context

        init {
            binding.cardView.setOnClickListener {
                tagDataListener.sendData("", dataList[adapterPosition])
            }
        }

        internal fun bind(data: TestReportData) {
            binding.apply {
                rangeTxt.text = context.getString(
                    R.string.ref_range_format,
                    data.refRangeI.toString(), data.refRangeII.toString()
                )
                val valueData = data.valueList[0]
                testTxt.text = data.testName
                valueTxt.text = valueData.valueText
                dateTxt.text = convertDateFormat(valueData.testDate)


                if (data.refRangeI == 0.0 && data.refRangeII == 0.0)
                    cardBgLay.setBackgroundColor(context.getColor(R.color.white))
                else if (valueData.value >= data.refRangeI && valueData.value <= data.refRangeII)
                    cardBgLay.setBackgroundColor(context.getColor(R.color.green_light))
                else
                    cardBgLay.setBackgroundColor(context.getColor(R.color.red_light))
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): CustomViewHolder {
        val itemView =
            ItemReportListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
}