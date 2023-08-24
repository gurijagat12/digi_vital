package ku.cwk.digivital.ui.common

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ku.cwk.digivital.R

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