package dev.igorcferreira.musicstreamsync.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.native.HiddenFromObjC

abstract class UseCase {
    @HiddenFromObjC
    internal val mutablePerforming = MutableStateFlow(false)

    @HiddenFromObjC
    internal val mutableError = MutableStateFlow<Exception?>(null)

    val isPerforming: StateFlow<Boolean>
        get() = mutablePerforming.asStateFlow()

    val error: StateFlow<Exception?>
        get() = mutableError.asStateFlow()
}
