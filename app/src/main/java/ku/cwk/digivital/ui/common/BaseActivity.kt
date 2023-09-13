package ku.cwk.digivital.ui.common

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ku.cwk.digivital.R
import java.util.Locale
import android.content.Context
import ku.cwk.digivital.util.Constants

@Suppress("DEPRECATION")
open class BaseActivity : AppCompatActivity() {
    private lateinit var dialogBuilder: AlertDialog.Builder
    private lateinit var progressDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Apply language code to all screens
        val languageCode =
            getPreferences(Context.MODE_PRIVATE).getString(Constants.APP_PREF_LANGUAGE, "en") ?: "en"
        setLocale(languageCode)

        dialogBuilder = MaterialAlertDialogBuilder(this)
            .setView(R.layout.layout_loading)
            .setCancelable(false)
    }

    open fun showProgressDialog() {
        progressDialog = dialogBuilder.show()
    }

    open fun hideProgressDialog() {
        if (this::progressDialog.isInitialized && progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    /*fun handleError(status: String): Boolean {
        if (status == NetworkHandler.STATUS_EXPIRED) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.apply {
                putExtra(Constants.LOGOUT, true)
            }
            startActivity(intent)
            finishAffinity()
            return true
        }
        showAlert(isError = true)
        return false
    }*/
}