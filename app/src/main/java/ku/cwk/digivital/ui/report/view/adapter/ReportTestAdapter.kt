package ku.cwk.digivital.ui.report.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ku.cwk.digivital.R
import ku.cwk.digivital.databinding.ItemReportTestBinding
import ku.cwk.digivital.ui.track.model.data.TestReportValueData
import ku.cwk.digivital.util.convertDateFormat

class ReportTestAdapter(
    private val dataList: ArrayList<TestReportValueData>,
    private val refRangeI: Double,
    private val refRangeII: Double
) : RecyclerView.Adapter<ReportTestAdapter.CustomViewHolder>() {

    inner class CustomViewHolder(private val binding: ItemReportTestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        internal fun bind(valueData: TestReportValueData) {
            binding.apply {
                valueTxt.text = valueData.valueText
                dateTxt.text = convertDateFormat(
                    valueData.testDate,
                    targetFormat = "dd MMMM yyyy"
                )

                if (refRangeI == 0.0 && refRangeII == 0.0)
                    binding.rangeImg.setImageResource(0)
                else if (valueData.value in refRangeI..refRangeII)
                    binding.rangeImg.setImageResource(R.drawable.round_check_circle)
                else
                    binding.rangeImg.setImageResource(R.drawable.round_cancel)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): CustomViewHolder {
        val itemView =
            ItemReportTestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
}