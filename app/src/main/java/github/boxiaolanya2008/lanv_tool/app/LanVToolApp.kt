package github.boxiaolanya2008.lanv_tool.app

import android.app.Application
import github.boxiaolanya2008.lanv_tool.data.repository.PropertyRepository
import github.boxiaolanya2008.lanv_tool.data.repository.SettingsRepository

class LanVToolApp : Application() {

    lateinit var propertyRepository: PropertyRepository
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        propertyRepository = PropertyRepository(this)
        settingsRepository = SettingsRepository(this)

        if (propertyRepository.isFirstBoot()) {
            Thread {
                propertyRepository.refreshProperties()
            }.start()
        }
    }

    companion object {
        lateinit var instance: LanVToolApp
            private set
    }
}
