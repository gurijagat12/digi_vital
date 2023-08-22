package ku.cwk.digivital.interfaces

import com.google.mlkit.vision.text.Text

interface MLImageData {
    fun sendImageData(imageData: Text)
}