package github.boxiaolanya2008.lanv_tool.domain.usecase

import github.boxiaolanya2008.lanv_tool.data.model.PropertyState
import github.boxiaolanya2008.lanv_tool.data.repository.PropertyRepository

class RefreshPropertiesUseCase(
    private val repository: PropertyRepository
) {
    suspend operator fun invoke(): Result<List<PropertyState>> {
        return repository.refreshProperties()
    }
}

class ApplyPropertyUseCase(
    private val repository: PropertyRepository
) {
    suspend operator fun invoke(key: String, value: String): Result<Unit> {
        if (value.isEmpty()) {
            val property = github.boxiaolanya2008.lanv_tool.data.model.PropertyRegistry.getByKey(key)
            if (property != null && property.type is github.boxiaolanya2008.lanv_tool.data.model.PropertyType.Selection) {
                if (!property.type.allowEmpty) {
                    return Result.failure(IllegalArgumentException("This property does not allow empty value"))
                }
            }
        }
        return repository.applyProperty(key, value)
    }
}

class RestorePropertyUseCase(
    private val repository: PropertyRepository
) {
    suspend operator fun invoke(key: String): Result<Unit> {
        return repository.restoreProperty(key)
    }
}

class RestoreAllUseCase(
    private val repository: PropertyRepository
) {
    suspend operator fun invoke(): Result<Map<String, Boolean>> {
        return repository.restoreAll()
    }
}

class CheckRootUseCase(
    private val repository: PropertyRepository
) {
    suspend operator fun invoke(): Boolean {
        return repository.checkRoot()
    }
}

class ObservePropertiesUseCase(
    private val repository: PropertyRepository
) {
    operator fun invoke() = repository.observeProperties()
}
