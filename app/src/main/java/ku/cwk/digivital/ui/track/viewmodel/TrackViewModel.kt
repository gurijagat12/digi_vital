package ku.cwk.digivital.ui.track.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.text.Text
import kotlinx.coroutines.launch
import ku.cwk.digivital.mlkit.textdetector.TextRecognitionProcessor
import ku.cwk.digivital.ui.track.model.data.TestDetailData
import ku.cwk.digivital.ui.track.model.repo.TestInfoRepository
import ku.cwk.digivital.util.NetworkHandler

class TrackViewModel(app: Application) : AndroidViewModel(app) {
    //Initialise repository for blood scans
    private val repo = TestInfoRepository(Firebase.firestore)

    //list for storing test result values
    lateinit var testTrackList: MutableList<TestDetailData>

    //Status of report processing
    var trackStatus = MutableLiveData(NetworkHandler.STATUS_NONE)

    //Status of data communication calls to Cloud DB
    var cloudStatus = MutableLiveData(NetworkHandler.STATUS_NONE)

    fun fetchTestInfo() {
        viewModelScope.launch {
            repo.fetchTestInfo()
            //repo.addTestFirebase()
        }
    }

    fun processMLImageData(imageData: Text) {
        viewModelScope.launch {
            trackStatus.value = NetworkHandler.STATUS_LOADING
            testTrackList = repo.processText(imageData)

            trackStatus.value =
                if (testTrackList.isNotEmpty())
                    NetworkHandler.STATUS_SUCCESS
                else NetworkHandler.STATUS_EMPTY
        }
    }

    fun saveTrackData(testDate: String) {
        viewModelScope.launch {
            cloudStatus.value = NetworkHandler.STATUS_LOADING
            repo.insertTestDataFirebaseDb(testDate)

            cloudStatus.value = NetworkHandler.STATUS_SUCCESS
        }
    }
}