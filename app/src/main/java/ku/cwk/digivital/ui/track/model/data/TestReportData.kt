package ku.cwk.digivital.ui.track.model.data

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class TestReportData(
    @PropertyName("ref_range_i") @JvmField
    var refRangeI: Double = 0.0,
    @PropertyName("ref_range_ii") @JvmField
    var refRangeII: Double = 0.0,
    @PropertyName("test_value") @JvmField
    var valueList: ArrayList<TestReportValueData> = arrayListOf(),
    @PropertyName("test_name") @JvmField
    var testName: String = ""
)
