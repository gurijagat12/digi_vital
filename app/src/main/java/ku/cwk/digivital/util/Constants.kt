package ku.cwk.digivital.util

class Constants {
    companion object {
        const val INTENT_DATA = "intent_data"

        //print objects
        const val DIGI_PRINT = "DigiVitalPrint"

        //Regex for range
        const val REGEX_REF_RANGE =
            "^\\W((\\d{1,2}\\.\\d\\-\\d{1,2}\\.\\d|\\d{2,3}\\-\\d{3})|\\d\\.\\d{3}\\-\\d\\.\\d{3})\\W\$"
    }
}

class FirebaseConstants {
    companion object {
        //Collections
        const val COL_BLOOD_TEST = "blood_test"
        const val COL_TRACK_TEST = "track_test"
    }
}

class NetworkHandler {
    companion object {
        const val STATUS_NONE = "NONE"
        const val STATUS_LOADING = "LOADING"
        const val STATUS_SUCCESS = "SUCCESS"
        const val STATUS_ERROR = "ERROR"
        const val STATUS_EMPTY = "EMPTY"
    }
}