package ku.cwk.digivital.util

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import ku.cwk.digivital.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Activity.showAlert(
    isError: Boolean = false,
    message: String = "",
    title: String? = null
) {
    val builder = AlertDialog.Builder(this)
    //set title for alert dialog
    builder.setTitle(title)
    //set message for alert dialog
    val msg = if (isError)
        getString(R.string.sorry_something_went_wrong_try)
    else
        message
    builder.setMessage(msg)
    //builder.setIcon(android.R.drawable.ic_dialog_alert)

    //performing positive action
    builder.setPositiveButton(getString(R.string.ok)) { dialogInterface, _ ->
        dialogInterface.dismiss()
    }
    /*//performing cancel action
    builder.setNeutralButton("Cancel") { dialogInterface, which ->
        dialogInterface.dismiss()
    }
    //performing negative action
    builder.setNegativeButton("No") { dialogInterface, which ->
        Toast.makeText(applicationContext, "clicked No", Toast.LENGTH_LONG).show()
    }*/
    // Create the AlertDialog
    val alertDialog: AlertDialog = builder.create()
    // Set other dialog properties
    alertDialog.setCancelable(false)
    alertDialog.show()
}

fun convertDateFormat(
    date: String,
    defaultFormat: String = "yyyy-MM-dd",
    targetFormat: String = "dd MMM yyyy"
): String {
    return try {
        val dateFormatPrev = SimpleDateFormat(defaultFormat, Locale.getDefault())
        val d: Date = dateFormatPrev.parse(date)!!
        val dateFormat = SimpleDateFormat(targetFormat, Locale.getDefault())
        dateFormat.format(d)
    } catch (e: java.lang.Exception) {
        ""
    }
}