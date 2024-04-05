package amaterek.util.ui.navigation.sample

import amaterek.util.log.Log
import amaterek.util.log.getDefaultLogger
import android.app.Application

class App : Application() {

    init {
        Log.setLogger(getDefaultLogger())
    }
}