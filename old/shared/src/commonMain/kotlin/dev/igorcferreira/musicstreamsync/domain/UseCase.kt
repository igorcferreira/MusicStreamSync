package dev.igorcferreira.musicstreamsync.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.native.HiddenFromObjC

abstract class UseCase {
    @HiddenFromObjC
    internal val _performing = MutableStateFlow(false)

    @HiddenFromObjC
    internal val _error = MutableStateFlow<Exception?>(null)

    val isPerforming: StateFlow<Boolean> = _performing.asStateFlow()
    val error: StateFlow<Exception?> = _error.asStateFlow()
}