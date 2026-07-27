package github.boxiaolanya2008.lanv_tool.app

import android.app.Application
import github.boxiaolanya2008.lanv_tool.data.repository.PropertyRepository
import github.boxiaolanya2008.lanv_tool.data.repository.PropertyRepositoryImpl
import github.boxiaolanya2008.lanv_tool.data.repository.SettingsRepository
import github.boxiaolanya2008.lanv_tool.data.repository.SettingsRepositoryImpl
import github.boxiaolanya2008.lanv_tool.domain.usecase.ApplyPropertyUseCase
import github.boxiaolanya2008.lanv_tool.domain.usecase.CheckRootUseCase
import github.boxiaolanya2008.lanv_tool.domain.usecase.GetSettingsUseCase
import github.boxiaolanya2008.lanv_tool.domain.usecase.ObservePropertiesUseCase
import github.boxiaolanya2008.lanv_tool.domain.usecase.RefreshPropertiesUseCase
import github.boxiaolanya2008.lanv_tool.domain.usecase.RestoreAllUseCase
import github.boxiaolanya2008.lanv_tool.domain.usecase.RestorePropertyUseCase
import github.boxiaolanya2008.lanv_tool.domain.usecase.UpdateSettingsUseCase

class LanVToolApp : Application() {

    lateinit var propertyRepository: PropertyRepository
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var refreshPropertiesUseCase: RefreshPropertiesUseCase
        private set

    lateinit var applyPropertyUseCase: ApplyPropertyUseCase
        private set

    lateinit var restorePropertyUseCase: RestorePropertyUseCase
        private set

    lateinit var restoreAllUseCase: RestoreAllUseCase
        private set

    lateinit var checkRootUseCase: CheckRootUseCase
        private set

    lateinit var observePropertiesUseCase: ObservePropertiesUseCase
        private set

    lateinit var getSettingsUseCase: GetSettingsUseCase
        private set

    lateinit var updateSettingsUseCase: UpdateSettingsUseCase
        private set

    override fun onCreate() {
        super.onCreate()

        propertyRepository = PropertyRepositoryImpl(this)
        settingsRepository = SettingsRepositoryImpl(this)

        refreshPropertiesUseCase = RefreshPropertiesUseCase(propertyRepository)
        applyPropertyUseCase = ApplyPropertyUseCase(propertyRepository)
        restorePropertyUseCase = RestorePropertyUseCase(propertyRepository)
        restoreAllUseCase = RestoreAllUseCase(propertyRepository)
        checkRootUseCase = CheckRootUseCase(propertyRepository)
        observePropertiesUseCase = ObservePropertiesUseCase(propertyRepository)
        getSettingsUseCase = GetSettingsUseCase(settingsRepository)
        updateSettingsUseCase = UpdateSettingsUseCase(settingsRepository)
    }

    companion object {
        lateinit var instance: LanVToolApp
    }
}
