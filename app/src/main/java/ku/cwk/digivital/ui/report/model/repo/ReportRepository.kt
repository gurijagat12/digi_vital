package ku.cwk.digivital.ui.report.model.repo

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import ku.cwk.digivital.ui.track.model.data.TestDetailData
import ku.cwk.digivital.ui.track.model.data.TestInfoData
import ku.cwk.digivital.ui.track.model.data.TestReportData
import ku.cwk.digivital.util.FirebaseConstants

class ReportRepository(private val firestoreDb: FirebaseFirestore) {

    suspend fun fetchTestData(): ArrayList<TestReportData>? {

        val testInfoAwait = firestoreDb.collection(FirebaseConstants.COL_TRACK_TEST).get().await()

        if (!testInfoAwait.isEmpty) {
            val reportList = ArrayList<TestReportData>()

            for (document in testInfoAwait.documents) {
                val testData = document.toObject<TestReportData>()!!
                reportList.add(testData)
            }
            return reportList
        }
        return null
    }
}