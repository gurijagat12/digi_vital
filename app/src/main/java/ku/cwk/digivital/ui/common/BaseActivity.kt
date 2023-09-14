package ku.cwk.digivital.ui.common

import android.content.res.Configuration
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ku.cwk.digivital.R
import java.util.Locale

@Suppress("DEPRECATION")
open class BaseActivity : AppCompatActivity() {
    private lateinit var dialogBuilder: AlertDialog.Builder
    private lateinit var progressDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    companion object {
        var dLocale: Locale? = null
    }

    init {
        updateConfig(this)
    }

    fun updateConfig(wrapper: ContextThemeWrapper) {
        if (dLocale == Locale("")) // Do nothing if dLocale is null
            return

        Locale.setDefault(dLocale)
        val configuration = Configuration()
        configuration.setLocale(dLocale)
        wrapper.applyOverrideConfiguration(configuration)
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