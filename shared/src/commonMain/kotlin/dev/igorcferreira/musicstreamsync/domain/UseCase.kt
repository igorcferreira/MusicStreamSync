package dev.igorcferreira.musicstreamsync.domain

import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.native.HiddenFromObjC

abstract class UseCase {
    @HiddenFromObjC
    internal val mutablePerforming = MutableStateFlow(false)

    @HiddenFromObjC
    internal val mutableError = MutableStateFlow<Exception?>(null)

    @NativeCoroutinesState
    val isPerforming: StateFlow<Boolean>
        get() = mutablePerforming.asStateFlow()

    @NativeCoroutinesState
    val error: StateFlow<Exception?>
        get() = mutableError.asStateFlow()
}
