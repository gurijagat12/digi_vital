package ku.cwk.digivital.ui.settings.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.content.Context
import ku.cwk.digivital.databinding.FragmentSettingsBinding
import ku.cwk.digivital.ui.common.BaseFragment
import ku.cwk.digivital.ui.home.view.MainActivity
import ku.cwk.digivital.util.Constants

class SettingsFragment : BaseFragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val languages = arrayOf("English", "French", "German", "Hindi")
    private val languageCodes = arrayOf("en", "fr", "de", "hi")
    private var languageCode = ""
    private var newLanguageCode = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ArrayAdapter(parentActivity, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1)

        languageCode = fetchLanguage()

        binding.apply {
            rbEn.text = languages[0]
            rbFr.text = languages[1]
            rbDe.text = languages[2]
            rbHi.text = languages[3]


            rbGroup.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    rbEn.id -> newLanguageCode = languageCodes[0]
                    rbFr.id -> newLanguageCode = languageCodes[1]
                    rbDe.id -> newLanguageCode = languageCodes[2]
                    rbHi.id -> newLanguageCode = languageCodes[3]
                }
                saveButton.isEnabled = newLanguageCode != languageCode
            }

            when (languageCode) {
                languageCodes[0] -> rbEn.isChecked = true
                languageCodes[1] -> rbFr.isChecked = true
                languageCodes[2] -> rbDe.isChecked = true
                languageCodes[3] -> rbHi.isChecked = true
            }

            saveButton.setOnClickListener {
                saveLanguage(newLanguageCode)
            }
        }
    }

    //Save and Fetch Language Code

    private fun saveLanguage(languageCode: String) {
        val sharedPref = parentActivity.getPreferences(Context.MODE_PRIVATE)
        sharedPref?.edit()?.putString(Constants.APP_PREF_LANGUAGE, languageCode)?.apply()
        parentActivity.startActivity(
            Intent(parentActivity, MainActivity::class.java)
        )
        parentActivity.finish()
    }

    private fun fetchLanguage(): String {
        val sharedPref = parentActivity.getPreferences(Context.MODE_PRIVATE)
        return sharedPref?.getString(Constants.APP_PREF_LANGUAGE, "en") ?: "en"
    }
}