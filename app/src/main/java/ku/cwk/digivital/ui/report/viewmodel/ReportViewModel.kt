package ku.cwk.digivital.ui.report.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import ku.cwk.digivital.ui.report.model.repo.ReportRepository
import ku.cwk.digivital.ui.track.model.data.TestReportData
import ku.cwk.digivital.ui.track.model.repo.TestInfoRepository
import ku.cwk.digivital.util.NetworkHandler

class ReportViewModel(app: Application) : AndroidViewModel(app) {
    //Initialise repository for blood reports from cloud
    private val repo = ReportRepository(Firebase.firestore)

    //Status of data communication calls to Cloud DB
    var reportStatus = MutableLiveData(NetworkHandler.STATUS_NONE)

    //test report data list
    lateinit var reportList: ArrayList<TestReportData>

    fun fetchReportData() {
        viewModelScope.launch {
            reportStatus.value = NetworkHandler.STATUS_LOADING
            val reportCloudList = repo.fetchTestData()
            if (reportCloudList != null) {
                reportList = reportCloudList
                reportStatus.value = NetworkHandler.STATUS_SUCCESS
            } else
                reportStatus.value = NetworkHandler.STATUS_ERROR
        }
    }
}