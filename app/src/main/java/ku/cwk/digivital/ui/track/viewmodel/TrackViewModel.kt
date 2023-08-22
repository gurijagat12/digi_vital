package ku.cwk.digivital.ui.track.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.text.Text
import kotlinx.coroutines.launch
import ku.cwk.digivital.mlkit.textdetector.TextRecognitionProcessor
import ku.cwk.digivital.ui.track.model.repo.TestInfoRepository

class TrackViewModel(app: Application) : AndroidViewModel(app) {
    //Initialise repository for blood scans
    private val repo = TestInfoRepository(Firebase.firestore)

    fun fetchTestInfo() {
        viewModelScope.launch {
            repo.fetchTestInfo()
            //repo.addTestFirebase()
        }
    }

    fun processMLImageData(imageData: Text) {
        viewModelScope.launch {
            repo.processText(imageData)
        }
    }
}