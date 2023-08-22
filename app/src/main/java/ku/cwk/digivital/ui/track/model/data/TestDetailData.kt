package ku.cwk.digivital.ui.track.model.data

data class TestDetailData(

    var testName: String = "",
    var yCoordinate: Int = 0,
    var trackData: TestTrackData = TestTrackData(),
    var testInfoData: TestInfoData = TestInfoData()
)