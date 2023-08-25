package ku.cwk.digivital.ui.report.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import ku.cwk.digivital.R
import ku.cwk.digivital.databinding.FragmentReportListBinding
import ku.cwk.digivital.ui.common.BaseFragment
import ku.cwk.digivital.ui.report.view.adapter.ReportAdapter
import ku.cwk.digivital.ui.report.viewmodel.ReportViewModel
import ku.cwk.digivital.ui.track.model.data.TestReportData
import ku.cwk.digivital.ui.track.view.TrackTestsActivity
import ku.cwk.digivital.util.Constants
import ku.cwk.digivital.util.NetworkHandler
import ku.cwk.digivital.util.showAlert
import ku.cwk.jobsconnect.interfaces.TagDataListener

class ReportListFragment : BaseFragment(), TagDataListener {

    private var _binding: FragmentReportListBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ReportViewModel
    private val trackResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK)
                viewModel.fetchReportData()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel =
            ViewModelProvider(parentActivity)[ReportViewModel::class.java]
        _binding = FragmentReportListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        viewModel = ViewModelProvider(this)[ReportViewModel::class.java]
        viewModel.apply {
            reportStatus.observe(parentActivity, dataObserver)
            fetchReportData()
        }
        binding.addReportBtn.setOnClickListener {
            trackResult.launch(
                Intent(
                    parentActivity, TrackTestsActivity::class.java
                )
            )
        }
    }

    private val dataObserver: Observer<String> = Observer { status ->
        when (status) {
            NetworkHandler.STATUS_NONE -> {
            }

            NetworkHandler.STATUS_LOADING -> {
                showProgressDialog()
            }

            NetworkHandler.STATUS_SUCCESS -> {
                hideProgressDialog()
                binding.apply {
                    reportRec.adapter =
                        ReportAdapter(viewModel.reportList, this@ReportListFragment)
                }
            }

            else -> {
                hideProgressDialog()
                parentActivity.showAlert(
                    message = getString(R.string.sorry_something_went_wrong_try),
                    isError = false
                )
            }
        }
    }

    override fun sendData(tag: String, data: Any?) {
        parentActivity.startActivity(
            Intent(
                parentActivity, ReportGraphActivity::class.java
            ).apply {
                putExtra(Constants.INTENT_DATA, Gson().toJson(data as TestReportData))
            })
    }
}