package ku.cwk.digivital.ui.common

import android.app.Application
import androidx.preference.PreferenceManager
import ku.cwk.digivital.util.Constants
import java.util.Locale

class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val languageCode =
            sharedPreferences.getString(Constants.APP_PREF_LANGUAGE, "en") ?: "en"

        BaseActivity.dLocale = Locale(languageCode) //set any locale here
    }
}