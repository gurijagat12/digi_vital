package ku.cwk.digivital.ui.track.model.data

import com.google.firebase.firestore.PropertyName

data class TestReportValueData(
    var value: Double = 0.0,
    @PropertyName("value_text") @JvmField
    var valueText: String = "",
    @PropertyName("test_date") @JvmField
    var testDate: String = ""
)