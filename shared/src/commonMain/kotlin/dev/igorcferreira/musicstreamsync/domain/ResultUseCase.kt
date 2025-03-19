package dev.igorcferreira.musicstreamsync.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.native.HiddenFromObjC

abstract class ResultUseCase<T>(
    initialValue: T
) : UseCase() {
    @HiddenFromObjC
    private val _result = MutableStateFlow(initialValue)
    val result: StateFlow<T> = _result.asStateFlow()

    @Throws(Exception::class)
    suspend fun perform(): T {
        _error.update { null }
        _performing.update { true }
        try {
            val response = operate()
            _result.update { response }
        } catch (e: Exception) {
            _error.update { e }
            throw e
        } finally {
            _performing.update { false }
        }
        return _result.value
    }

    @HiddenFromObjC
    @Throws(Exception::class)
    internal abstract suspend fun operate(): T
}