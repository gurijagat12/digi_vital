package ku.cwk.digivital.ui.track.model.repo

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.mlkit.vision.text.Text
import kotlinx.coroutines.tasks.await
import ku.cwk.digivital.ui.track.model.data.TestDetailData
import ku.cwk.digivital.ui.track.model.data.TestInfoData
import ku.cwk.digivital.ui.track.model.data.TestReportData
import ku.cwk.digivital.ui.track.model.data.TestReportValueData
import ku.cwk.digivital.ui.track.model.data.TestTrackData
import ku.cwk.digivital.util.Constants
import ku.cwk.digivital.util.FirebaseConstants
import kotlin.math.abs

class TestInfoRepository(private val firestoreDb: FirebaseFirestore) {
    //list of all tests
    private lateinit var testDataList: MutableList<TestDetailData>

    //list for storing test result values
    private lateinit var testTrackList: MutableList<TestDetailData>

    //list of test's units
    private lateinit var testUnitList: MutableList<String>
    private lateinit var testUnitRegexList: MutableList<String>

    suspend fun fetchTestInfo(): MutableList<TestDetailData>? {

        val testInfoAwait = firestoreDb.collection(FirebaseConstants.COL_BLOOD_TEST).get().await()

        if (!testInfoAwait.isEmpty) {
            testDataList = mutableListOf()
            testUnitList = mutableListOf()
            testUnitRegexList = mutableListOf()

            for (document in testInfoAwait.documents) {
                val testData = document.toObject<TestInfoData>()!!
                val testDetailData = TestDetailData(testName = document.id)
                testDetailData.testInfoData = testData

                //Add unique Units to a list
                testData.testUnitList.forEach {
                    if (!testUnitList.contains(it))
                        this.testUnitList.add(it)
                }
                //Add all unique Regex to list
                if (!testUnitRegexList.contains(testData.testRegex) &&
                    testData.testRegex.isNotEmpty()
                )
                    this.testUnitRegexList.add(testData.testRegex)

                testDataList.add(testDetailData)

                //Log.d(Constants.DIGI_PRINT, "${document.id} => ${document.data}")
            }

            //Log.d(Constants.DIGI_PRINT, "" + testDataList)
            //Log.d(Constants.DIGI_PRINT, testUnitList.toString())
            //Log.d(Constants.DIGI_PRINT, testUnitRegexList.toString())

            return testDataList
        } else
            Log.w(Constants.DIGI_PRINT, "Error getting documents.")
        //addTestFirebase()
        return null
    }

    /*fun addTestFirebase() {
        val testData = hashMapOf(
            "test_unit" to listOf("g/L")
        )

        firestoreDb.collection(FirebaseConstants.COL_BLOOD_TEST)
            .document("Mean cell haemoglobin")
            .set(testData)
            .addOnSuccessListener {
                Log.d(
                    Constants.DIGI_PRINT,
                    "DocumentSnapshot successfully written!"
                )
            }
            .addOnFailureListener { e -> Log.w(Constants.DIGI_PRINT, "Error writing document", e) }
    }*/

    fun processText(text: Text): MutableList<TestDetailData> {
        testTrackList = mutableListOf()
        for (textBlock in text.textBlocks) { // Renders the text at the bottom of the box.
            //Log.d(TAG, "TextBlock text is: " + textBlock.text)
            for (line in textBlock.lines) {
                processTest(line)

                /*Log.d(Constants.DIGI_PRINT, "Line text is: " + line.text)
                for (point in line.cornerPoints!!) {
                    Log.v(
                        Constants.DIGI_PRINT,
                        String.format(
                            "Corner point for element %s is located at: x - %d, y = %d",
                            line.text,
                            point.x,
                            point.y
                        )
                    )
                }
                Log.d(Constants.DIGI_PRINT, "\n")*/
            }
        }
        testTrackList.removeAll { it.trackData.valueText.isEmpty() }

        for (test in testTrackList) {
            Log.d(
                Constants.DIGI_PRINT, String.format(
                    "Test: %s -> %s",
                    test.testName,
                    test.trackData.value.toString() +
                            " (${test.trackData.refRangeI} - ${test.trackData.refRangeII})"
                )
            )
            /*Log.d(Constants.DIGI_PRINT, "Value: " + test.trackData.valueText)
            Log.d(Constants.DIGI_PRINT, "Min: " + test.yMin)
            Log.d(Constants.DIGI_PRINT, "Max: " + test.yMax)*/
        }
        //insertTestDataFirebaseDb()
        return testTrackList
    }

    private fun processTest(lineData: Text.Line) {
        var text = lineData.text
        // Check if the string starts and ends with "|" which is part of the table
        if (text.isNotEmpty()) {
            if (text.first() == '|')
                text = text.substring(1)

            if (text.last() == '|')
                text = text.substring(0, text.length - 1)
        }

        if (testDataList.any { text.contains(it.testName, true) }) {
            val test = TestDetailData()
            //Insert test name
            test.testName = text.replace("|", "")
            //Y Coordinate of text
            test.yCoordinate = lineData.cornerPoints?.get(0)?.y ?: 0

            //add test names scanned from document
            testTrackList.add(test)
        }

        if (testTrackList.isNotEmpty()) {

            //check exact unit
            val unitTextSplit = text.split(" ")

            //check ref range
            val textRange = text.replace(" ", "")
            val regexPattern = Regex("\\W(\\d+(\\.\\d+)?)-(\\d+(\\.\\d+)?)\\W")
            val matchResult = regexPattern.find(textRange)

            if (testUnitRegexList.any {
                    text.contains(Regex(pattern = it, options = setOf(RegexOption.IGNORE_CASE)))
                } ||
                (unitTextSplit.size > 1 && testUnitList.any { unitTextSplit[1] == it } ||
                        matchResult != null)
            ) {
                val yCoordinate = lineData.cornerPoints?.get(0)?.y ?: 0

                //Find nearest y Coordinate
                val tracker = testTrackList.minBy { abs(yCoordinate - it.yCoordinate) }

                //Step I - match name
                val testInfo =
                    testDataList.find { tracker.testName.contains(it.testName) }

                if (testInfo != null) {
                    //Step II - match unit
                    if (testInfo.testInfoData.testUnitList.any { it == unitTextSplit[1] } ||
                        text.contains(
                            Regex(
                                pattern = testInfo.testInfoData.testRegex,
                                options = setOf(RegexOption.IGNORE_CASE)
                            )
                        )
                    ) {
                        try {
                            tracker.trackData.value = unitTextSplit[0].toDouble()
                            tracker.trackData.testNameDocument = testInfo.testName
                            if (matchResult != null) {
                                tracker.trackData.valueText =
                                    "${unitTextSplit[0]} ${unitTextSplit[1]}"
                                checkRefRange(matchResult, tracker.trackData)
                            } else
                                tracker.trackData.valueText = text

                        } catch (e: NumberFormatException) {
                            e.printStackTrace()
                        }

                    } else if (tracker.trackData.value != 0.0)
                        checkRefRange(matchResult, tracker.trackData)
                }
            }
        }
    }

    private fun checkRefRange(matchResult: MatchResult?, trackData: TestTrackData) {
        if (matchResult != null) {
            val rangeStart = matchResult.groupValues[1]
            val rangeEnd = matchResult.groupValues[3]
            try {
                trackData.refRangeI = rangeStart.toDouble()
                trackData.refRangeII = rangeEnd.toDouble()
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
        }
    }

    //Insert scanned test values into Firestore Cloud DB
    suspend fun insertTestDataFirebaseDb(testDate: String) {

        testTrackList.forEach { trackTest ->
            //Extract scanned track data
            val trackData = trackTest.trackData

            //data object to be saved on cloud
            val testReportData = TestReportData()
            testReportData.refRangeI = trackData.refRangeI
            testReportData.refRangeII = trackData.refRangeII
            testReportData.testName = trackTest.testName

            //Fetch already existed values from cloud DB
            val testValueDocAwait = firestoreDb.collection(FirebaseConstants.COL_TRACK_TEST)
                .document(trackData.testNameDocument).get().await()
            //If values exist insert to tracked object to save previous values along-with new values
            if (testValueDocAwait.exists()) {
                val testData = testValueDocAwait.toObject<TestReportData>()!!
                testReportData.valueList = testData.valueList
            }

            //check if value already exists for the entered date
            if (!testReportData.valueList.any {
                    it.testDate == testDate && it.value == trackData.value
                }) {
                testReportData.valueList.add(
                    TestReportValueData(
                        value = trackData.value,
                        valueText = trackData.valueText,
                        testDate = testDate
                    )
                )
                //Sort tracked values by date
                testReportData.valueList.sortBy { it.testDate }
                //Insert values in cloud DB
                firestoreDb.collection(FirebaseConstants.COL_TRACK_TEST)
                    .document(trackData.testNameDocument)
                    .set(testReportData)
            }
        }
    }
}