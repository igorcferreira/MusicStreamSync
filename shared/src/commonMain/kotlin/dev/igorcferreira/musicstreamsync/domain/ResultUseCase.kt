package dev.igorcferreira.musicstreamsync.domain

import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.native.HiddenFromObjC

abstract class ResultUseCase<T>(
    initialValue: T,
) : UseCase() {
    @HiddenFromObjC
    private val _result = MutableStateFlow(initialValue)

    @NativeCoroutinesState
    val result: StateFlow<T>
        get() = _result.asStateFlow()

    @Throws(Exception::class)
    suspend fun perform(): T {
        mutableError.update { null }
        mutablePerforming.update { true }
        try {
            val response = operate()
            _result.update { response }
        } catch (e: Exception) {
            mutableError.update { e }
            throw e
        } finally {
            mutablePerforming.update { false }
        }
        return _result.value
    }

    @HiddenFromObjC
    @Throws(Exception::class)
    internal abstract suspend fun operate(): T
}
