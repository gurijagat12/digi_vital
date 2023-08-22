/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ku.cwk.digivital.mlkit.textdetector

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface
import ku.cwk.digivital.interfaces.MLImageData
import ku.cwk.digivital.mlkit.PreferenceUtils
import ku.cwk.digivital.mlkit.VisionProcessorBase
import ku.cwk.digivital.ui.track.model.data.TestDetailData

/** Processor for the text detector demo. */
class TextRecognitionProcessor(
    private val context: Context,
    textRecognizerOptions: TextRecognizerOptionsInterface
) : VisionProcessorBase<Text>(context) {
    private val textRecognizer: TextRecognizer = TextRecognition.getClient(textRecognizerOptions)
    private val shouldGroupRecognizedTextInBlocks: Boolean =
        PreferenceUtils.shouldGroupRecognizedTextInBlocks(context)
    private val showLanguageTag: Boolean = PreferenceUtils.showLanguageTag(context)
    private val showConfidence: Boolean = PreferenceUtils.shouldShowTextConfidence(context)

    override fun stop() {
        super.stop()
        textRecognizer.close()
    }

    override fun detectInImage(image: InputImage): Task<Text> {
        return textRecognizer.process(image)
    }

    override fun onSuccess(text: Text, mlImageData: MLImageData) {
        Log.d(TAG, "On-device Text detection successful")
        //logExtrasForTesting(text)
        //processText(text)
        mlImageData.sendImageData(text)
    }

    private fun processText(text: Text) {

        for (textBlock in text.textBlocks) { // Renders the text at the bottom of the box.
            //Log.d(TAG, "TextBlock text is: " + textBlock.text)
            for (line in textBlock.lines) {
                processTest(line)
                /*Log.d(TAG, "Line text is: " + line.text)
                for (point in line.cornerPoints!!) {
                    Log.v(
                        TAG,
                        String.format(
                            "Corner point for element %s is located at: x - %d, y = %d",
                            line.text,
                            point.x,
                            point.y
                        )
                    )
                }
                Log.d(TAG, "\n")*/
            }
        }
    }

    private val testList = mutableListOf<TestDetailData>()
    private var testIndex = 0

    private val testNamesList = listOf(

        "Platelet count",
        "Haematocrit",
        "Mean cell volume",
        "Mean cell haemoglobin level",
        "Mean cell haemoglobin conc",
        "Red blood cell distribution width",
        "Mean platelet count",
        "Neutrophil",
        "Monocyte",
        "Eosinophil",
        "Basophil",
        "Lymphocyte"
    )

    private val testUnitList = listOf(
        "g/L",
        "%",
        "ratio",
        "fL",
        "pg",
        "L/L"
    )
    var diffY = 0


    private fun processTest(lineData: Text.Line) {
        val text = lineData.text
        if (
            testNamesList.any { text.contains(it, true) }
        ) {
            val test = TestDetailData()
            //Insert test name
            test.testName = text.replace("|", "")
            //Y Coordinate of text
            test.yCoordinate = lineData.cornerPoints?.get(0)?.y ?: 0

            testList.add(test)
        }

        if (testNamesList.isNotEmpty()) {
            //Insert value
            val regex12 = Regex(pattern = "10\\W12/L", options = setOf(RegexOption.IGNORE_CASE))
            val regex9 = Regex(pattern = "10\\W9/L", options = setOf(RegexOption.IGNORE_CASE))

            //check exact unit
            val unitTextSplit = text.split(" ")

            if ((text.contains(regex12) ||
                        text.contains(regex9)) ||
                (unitTextSplit.size > 1 && testUnitList.any { unitTextSplit[1] == it })
            ) {
                if (testNamesList.size > 1) {
                    if (diffY == 0) {
                        testList.forEachIndexed { index, testDetailData ->

                            testDetailData.yCoordinate = testDetailData.yCoordinate - diffY

                            /*Log.v(
                                TAG,
                                String.format(
                                    "Corner point for element %s is located at: x - %d, y = %d",
                                    testDetailData.testName,
                                    testDetailData.yMin,
                                    testDetailData.yMax
                                )
                            )*/
                        }
                    }
                    /*Log.d(TAG, text)
                    val yCoordinate = lineData.cornerPoints?.get(0)?.y ?: 0
                    testList.find { yCoordinate > it.yCoordinate && yCoordinate < it.yMax }?.trackData?.valueText =
                        text.replace("|", "")*/
                } else
                    testList[0].trackData.valueText = text.replace("|", "")

                //Log.d(TAG, "\n")
            }
        }
    }

    override fun onFailure(e: Exception) {
        Log.w(TAG, "Text detection failed.$e")
    }

    companion object {
        private const val TAG = "TextRecProcessor"
        private fun logExtrasForTesting(text: Text?) {
            if (text != null) {
                Log.v(MANUAL_TESTING_LOG, "Detected text has : " + text.textBlocks.size + " blocks")
                for (i in text.textBlocks.indices) {
                    val lines = text.textBlocks[i].lines
                    Log.v(
                        MANUAL_TESTING_LOG,
                        String.format("Detected text block %d has %d lines", i, lines.size)
                    )
                    for (j in lines.indices) {
                        val elements = lines[j].elements
                        Log.v(
                            MANUAL_TESTING_LOG,
                            String.format("Detected text line %d has %d elements", j, elements.size)
                        )
                        for (k in elements.indices) {
                            val element = elements[k]
                            Log.v(
                                MANUAL_TESTING_LOG,
                                String.format("Detected text element %d says: %s", k, element.text)
                            )
                            Log.v(
                                MANUAL_TESTING_LOG,
                                String.format(
                                    "Detected text element %d has a bounding box: %s",
                                    k,
                                    element.boundingBox!!.flattenToString()
                                )
                            )
                            Log.v(
                                MANUAL_TESTING_LOG,
                                String.format(
                                    "Expected corner point size is 4, get %d",
                                    element.cornerPoints!!.size
                                )
                            )
                            for (point in element.cornerPoints!!) {
                                Log.v(
                                    MANUAL_TESTING_LOG,
                                    String.format(
                                        "Corner point for element %d is located at: x - %d, y = %d",
                                        k,
                                        point.x,
                                        point.y
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
