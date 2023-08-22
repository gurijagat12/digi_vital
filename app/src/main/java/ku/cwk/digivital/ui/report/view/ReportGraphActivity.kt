package ku.cwk.digivital.ui.report.view

import android.graphics.Color
import android.os.Bundle
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.gson.Gson
import ku.cwk.digivital.R
import ku.cwk.digivital.databinding.ActivityReportGraphBinding
import ku.cwk.digivital.ui.common.BaseActivity
import ku.cwk.digivital.ui.track.model.data.TestReportData
import ku.cwk.digivital.util.Constants
import ku.cwk.digivital.util.convertDateFormat
import java.text.DecimalFormat


class ReportGraphActivity : BaseActivity() {

    private lateinit var binding: ActivityReportGraphBinding
    private lateinit var chart: LineChart
    private lateinit var reportData: TestReportData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityReportGraphBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
        initGraph()
        showGraph()
    }

    private fun initUI() {
        reportData = Gson().fromJson(
            intent.getStringExtra(Constants.INTENT_DATA),
            TestReportData::class.java
        )
    }

    //Initiate line graph
    private fun initGraph() {
        chart = binding.lineChart

        // Chart Style

        // background color
        chart.setBackgroundColor(Color.WHITE)

        // disable description text
        chart.description.isEnabled = false

        // enable touch gestures
        chart.setTouchEnabled(false)
        chart.setDrawGridBackground(false)
        // enable scaling and dragging
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        // force pinch zoom along both axis
        chart.setPinchZoom(true)
    }

    private fun showGraph() {
// // X-Axis Style // //
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.axisMinimum = -0.03f
        xAxis.granularity = 1f
        //xAxis.textColor = resources.getColor(R.color.teal_200, null)

        val listData = reportData.valueList
        listData.reverse()

        xAxis.setLabelCount(listData.size, true)
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val pos = value.toInt()
                return if (pos >= listData.size || pos < 0)
                    ""
                else
                    convertDateFormat(listData[pos].testDate, targetFormat = "dd\nMMM")
            }
        }

        // // Y-Axis Style // //
        chart.axisLeft

        // disable dual axis (only use LEFT axis)
        chart.axisRight.isEnabled = false

        chart.axisLeft.isEnabled = true
        //chart.axisLeft.axisMinimum = 20f

        ////////////////////////////////////////////////////////////////

        val ll2 = LimitLine(reportData.refRangeI.toFloat(), getString(R.string.ref_range_low))
        ll2.lineWidth = 2f
        ll2.enableDashedLine(25f, 10f, 0f)
        ll2.labelPosition = LimitLabelPosition.RIGHT_BOTTOM
        ll2.textSize = 8f

        val ll1 = LimitLine(reportData.refRangeII.toFloat(), getString(R.string.ref_range_high))
        ll1.lineWidth = 2f
        ll1.enableDashedLine(25f, 10f, 0f)
        ll1.labelPosition = LimitLabelPosition.RIGHT_TOP
        ll1.textSize = 8f
        //ll1.typeface = tfRegular
        //ll2.typeface = tfRegular

        // draw limit lines behind data instead of on top

        // draw limit lines behind data instead of on top
        chart.axisLeft.setDrawLimitLinesBehindData(true)
        xAxis.setDrawLimitLinesBehindData(true)

        // add limit lines
        chart.axisLeft.addLimitLine(ll1)
        chart.axisLeft.addLimitLine(ll2)
        //xAxis.addLimitLine(llXAxis)

        chart.axisLeft.axisMinimum = reportData.refRangeI.toFloat() - 10f
        chart.axisLeft.axisMaximum = reportData.refRangeII.toFloat() + 10f

        //////////////////////////////////////////////////////////////////

        //setData
        val values = ArrayList<Entry>()
        for (i in listData.indices) {
            values.add(Entry(i.toFloat(), (listData[i].value.toFloat())))
        }

        val set1 = LineDataSet(values, null)
        //val color = getColor(R.color.colorGraphWeight)
        val color = getColor(R.color.purple_700)
        set1.color = color
        set1.setCircleColor(color)
        // line thickness and point size
        set1.lineWidth = 3f
        set1.circleRadius = 6f
        // draw points as solid circles
        set1.setDrawCircleHole(false)
        // text size of values
        set1.valueTextSize = 12f
        set1.valueFormatter = DecimalFormatter()
        //TODO set1.valueTypeface = typeFaceGraphLine
        set1.valueTextColor = resources.getColor(R.color.teal_700, null)

        // set the filled area
        set1.setDrawFilled(false)
        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(set1) // add the data sets
        // create a data object with the data sets
        val data = LineData(dataSets)

        // set data
        chart.data = data

        // draw points over time
        chart.animateX(1000)

        // get the legend (only possible after setting data)
        chart.legend.isEnabled = false
    }

    class DecimalFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return DecimalFormat("#.#").format(value)
        }
    }
}