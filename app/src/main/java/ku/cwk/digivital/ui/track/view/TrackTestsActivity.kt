package ku.cwk.digivital.ui.track.view

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.icu.text.DecimalFormat
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Pair
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import ku.cwk.digivital.R
import ku.cwk.digivital.databinding.ActivityTestScanBinding
import ku.cwk.digivital.interfaces.MLImageData
import ku.cwk.digivital.mlkit.BitmapUtils
import ku.cwk.digivital.mlkit.VisionImageProcessor
import ku.cwk.digivital.mlkit.textdetector.TextRecognitionProcessor
import ku.cwk.digivital.ui.common.BaseActivity
import ku.cwk.digivital.ui.track.view.adapter.TrackAdapter
import ku.cwk.digivital.ui.track.viewmodel.TrackViewModel
import ku.cwk.digivital.util.NetworkHandler
import ku.cwk.digivital.util.showAlert
import ku.cwk.jobsconnect.interfaces.TagDataListener
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@Suppress("DEPRECATION")
class TrackTestsActivity : BaseActivity(), MLImageData, TagDataListener {

    private lateinit var viewModel: TrackViewModel
    private lateinit var binding: ActivityTestScanBinding
    private lateinit var trackAdapter: TrackAdapter
    private var isLandScape = false
    private var imageUri: Uri? = null

    // Max width (portrait mode)
    private var imageMaxWidth = 0

    // Max height (portrait mode)
    private var imageMaxHeight = 0
    private var imageProcessor: VisionImageProcessor? = null

    private var selectedMode = TEXT_RECOGNITION_LATIN
    private var selectedSize: String? = SIZE_SCREEN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cameraBtn.setOnClickListener {
            startCameraIntentForResult()
        }

        binding.galleryBtn.setOnClickListener {
            startChooseImageIntentForResult()
        }
        binding.dateBtn.setOnClickListener {
            chooseDate()
        }
        isLandScape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        if (savedInstanceState != null) {
            imageUri = savedInstanceState.getParcelable(KEY_IMAGE_URI)
            imageMaxWidth = savedInstanceState.getInt(KEY_IMAGE_MAX_WIDTH)
            imageMaxHeight = savedInstanceState.getInt(KEY_IMAGE_MAX_HEIGHT)
            selectedSize = savedInstanceState.getString(KEY_SELECTED_SIZE)
        }

        val rootView = binding.root
        rootView.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    rootView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    imageMaxWidth = rootView.width
                    imageMaxHeight = rootView.height - binding.scanLay.height
                    if (SIZE_SCREEN == selectedSize) {
                        tryReloadAndDetectInImage()
                    }
                }
            }
        )
        fetchTestInfo()
    }

    public override fun onResume() {
        super.onResume()
        createImageProcessor()
        tryReloadAndDetectInImage()
    }

    public override fun onPause() {
        super.onPause()
        imageProcessor?.run { this.stop() }
    }

    public override fun onDestroy() {
        super.onDestroy()
        imageProcessor?.run { this.stop() }
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_IMAGE_URI, imageUri)
        outState.putInt(KEY_IMAGE_MAX_WIDTH, imageMaxWidth)
        outState.putInt(KEY_IMAGE_MAX_HEIGHT, imageMaxHeight)
        outState.putString(KEY_SELECTED_SIZE, selectedSize)
    }

    private fun startCameraIntentForResult() { // Clean up last time's image
        imageUri = null
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "New Picture")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
            imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun startChooseImageIntentForResult() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CHOOSE_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            tryReloadAndDetectInImage()
        } else if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == Activity.RESULT_OK) {
            // In this case, imageUri is returned by the chooser, save it.
            imageUri = data!!.data
            tryReloadAndDetectInImage()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun tryReloadAndDetectInImage() {
        try {
            if (imageUri == null) {
                return
            }

            if (SIZE_SCREEN == selectedSize && imageMaxWidth == 0) {
                // UI layout has not finished yet, will reload once it's ready.
                return
            }

            val imageBitmap =
                BitmapUtils.getBitmapFromContentUri(contentResolver, imageUri) ?: return

            val resizedBitmap: Bitmap = if (selectedSize == SIZE_ORIGINAL) {
                imageBitmap
            } else {
                // Get the dimensions of the image view
                val targetedSize: Pair<Int, Int> = targetedWidthHeight

                // Determine how much to scale down the image
                val scaleFactor =
                    (imageBitmap.width.toFloat() / targetedSize.first.toFloat()).coerceAtLeast(
                        imageBitmap.height.toFloat() / targetedSize.second.toFloat()
                    )
                Bitmap.createScaledBitmap(
                    imageBitmap,
                    (imageBitmap.width / scaleFactor).toInt(),
                    (imageBitmap.height / scaleFactor).toInt(),
                    true
                )
            }

            if (imageProcessor != null) {
                imageProcessor!!.processBitmap(resizedBitmap, this)
            }
        } catch (e: IOException) {
            imageUri = null
            e.printStackTrace()
        }
    }

    private val targetedWidthHeight: Pair<Int, Int>
        get() {
            val targetWidth: Int
            val targetHeight: Int
            when (selectedSize) {
                SIZE_SCREEN -> {
                    targetWidth = imageMaxWidth
                    targetHeight = imageMaxHeight
                }

                SIZE_640_480 -> {
                    targetWidth = if (isLandScape) 640 else 480
                    targetHeight = if (isLandScape) 480 else 640
                }

                SIZE_1024_768 -> {
                    targetWidth = if (isLandScape) 1024 else 768
                    targetHeight = if (isLandScape) 768 else 1024
                }

                else -> throw IllegalStateException("Unknown size")
            }
            return Pair(targetWidth, targetHeight)
        }

    private fun createImageProcessor() {
        try {
            when (selectedMode) {
                TEXT_RECOGNITION_LATIN ->
                    imageProcessor =
                        TextRecognitionProcessor(this, TextRecognizerOptions.Builder().build())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getText() {
        //imageProcessor.
    }

    private fun fetchTestInfo() {
        viewModel = ViewModelProvider(this)[TrackViewModel::class.java]
        viewModel.fetchTestInfo()
        viewModel.trackStatus.observe(this, dataObserver)
        viewModel.cloudStatus.observe(this, cloudObserver)
    }

    private val dataObserver: Observer<String> = Observer { status ->
        when (status) {
            NetworkHandler.STATUS_NONE -> {
            }

            NetworkHandler.STATUS_LOADING -> {
                showProgressDialog()
            }

            NetworkHandler.STATUS_SUCCESS -> {
                hideProgressDialog()
                trackAdapter = TrackAdapter(viewModel.testTrackList, this)
                binding.apply {
                    infoTxt.visibility = View.GONE
                    recLay.visibility = View.VISIBLE
                    scanRec.adapter = trackAdapter

                }
            }

            else -> {
                hideProgressDialog()
                binding.apply {
                    infoTxt.visibility = View.VISIBLE
                    recLay.visibility = View.GONE
                }
                showAlert(
                    message = getString(R.string.no_results),
                    isError = false
                )
            }
        }
    }

    private val cloudObserver: Observer<String> = Observer { status ->
        when (status) {
            NetworkHandler.STATUS_NONE -> {
            }

            NetworkHandler.STATUS_LOADING -> {
                showProgressDialog()
            }

            NetworkHandler.STATUS_SUCCESS -> {
                hideProgressDialog()
                val dialog = AlertDialog.Builder(this)
                    .setMessage(getString(R.string.data_saved))
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok) { _, _ ->
                        setResult(RESULT_OK)
                        finish()
                    }
                    .create()
                dialog.show()
            }

            else -> {
                hideProgressDialog()
                showAlert(
                    message = getString(R.string.sorry_something_went_wrong_try),
                    isError = false
                )
            }
        }
    }

    companion object {
        private const val TEXT_RECOGNITION_LATIN = "Text Recognition Latin"

        private const val SIZE_SCREEN = "w:screen" // Match screen width
        private const val SIZE_1024_768 = "w:1024" // ~1024*768 in a normal ratio
        private const val SIZE_640_480 = "w:640" // ~640*480 in a normal ratio
        private const val SIZE_ORIGINAL = "w:original" // Original image size
        private const val KEY_IMAGE_URI = "com.google.mlkit.vision.demo.KEY_IMAGE_URI"
        private const val KEY_IMAGE_MAX_WIDTH = "com.google.mlkit.vision.demo.KEY_IMAGE_MAX_WIDTH"
        private const val KEY_IMAGE_MAX_HEIGHT = "com.google.mlkit.vision.demo.KEY_IMAGE_MAX_HEIGHT"
        private const val KEY_SELECTED_SIZE = "com.google.mlkit.vision.demo.KEY_SELECTED_SIZE"
        private const val REQUEST_IMAGE_CAPTURE = 1001
        private const val REQUEST_CHOOSE_IMAGE = 1002
    }

    override fun sendImageData(imageData: Text) {
        viewModel.processMLImageData(imageData)
    }

    override fun sendData(tag: String, data: Any?) {
        val testData = viewModel.testTrackList[data as Int]

        val inputEditTextField = TextInputEditText(this)
        inputEditTextField.hint = getString(R.string.value_hint)
        inputEditTextField.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        val dialog = AlertDialog.Builder(this)
            .setTitle(testData.testName)
            .setMessage(getString(R.string.value_format, testData.trackData.value.toString()))
            .setView(inputEditTextField)
            .setPositiveButton(R.string.ok) { _, _ ->
                val editTextInput = inputEditTextField.text.toString()
                testData.trackData.valueText = testData.trackData.valueText.replace(
                    DecimalFormat("0.#").format(testData.trackData.value),
                    editTextInput
                )
                testData.trackData.value = editTextInput.toDouble()
                trackAdapter.notifyItemChanged(data)
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
        dialog.show()
    }

    private fun chooseDate() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)


        val dpd = DatePickerDialog(this, { _, yearPicker, monthOfYear, dayOfMonth ->
            c.set(yearPicker, monthOfYear, dayOfMonth)
            val testDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(c.time)
            viewModel.saveTrackData(testDate)
        }, year, month, day)

        dpd.show()
    }
}