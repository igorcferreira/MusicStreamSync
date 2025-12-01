package dev.igorcferreira.musicstreamsync.domain

import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.native.HiddenFromObjC

abstract class UseCase {
    @HiddenFromObjC
    internal val _performing = MutableStateFlow(false)

    @HiddenFromObjC
    internal val _error = MutableStateFlow<Exception?>(null)

    @NativeCoroutinesState
    val isPerforming: StateFlow<Boolean>
        get() = _performing.asStateFlow()
    @NativeCoroutinesState
    val error: StateFlow<Exception?>
        get() = _error.asStateFlow()
}
