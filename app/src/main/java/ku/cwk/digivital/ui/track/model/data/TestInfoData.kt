package ku.cwk.digivital.ui.track.model.data

import com.google.firebase.firestore.PropertyName

data class TestInfoData(
    @PropertyName("test_regex") @JvmField
    val testRegex: String = "",
    @PropertyName("test_unit") @JvmField
    val testUnitList: List<String> = listOf()
)