package dev.igorcferreira.musicstreamsync.domain

import android.app.Activity
import android.app.Application
import android.content.ActivityNotFoundException
import android.os.Bundle
import android.security.keystore.UserNotAuthenticatedException
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.apple.android.sdk.authentication.AuthenticationFactory
import com.liftric.kvault.KVault
import java.lang.ref.WeakReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class MusicUserTokenProvider : UserTokenProvider, Application.ActivityLifecycleCallbacks {
    private var _currentActivity = WeakReference<ComponentActivity>(null)
    private lateinit var vault: KVault

    fun init(application: Application) {
        application
            .registerActivityLifecycleCallbacks(this)
        vault = KVault(application)
    }

    override suspend fun getUserToken(
        developerToken: String
    ): String = getLocalToken() ?: getExternalToken(developerToken)

    private fun getLocalToken(): String? = vault.string(VAULT_KEY)

    private suspend fun getExternalToken(
        developerToken: String
    ): String {
        val currentActivity = _currentActivity.get() ?: throw ActivityNotFoundException()
        val authenticationManager = AuthenticationFactory
            .createAuthenticationManager(currentActivity.applicationContext)

        val intent = authenticationManager
            .createIntentBuilder(developerToken)
            .build()

        val token = suspendCoroutine { continuation ->
            currentActivity.activityResultRegistry.register(
                "token_request",
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode != Activity.RESULT_OK) {
                    continuation.resumeWithException(UserNotAuthenticatedException())
                } else if (result.data == null) {
                    continuation.resumeWithException(UserNotAuthenticatedException())
                } else {
                    val token = authenticationManager.handleTokenResult(result.data)
                    if (token.isError) {
                        continuation.resumeWithException(UserNotAuthenticatedException())
                    } else {
                        continuation.resume(token.musicUserToken)
                    }
                }
            }.launch(intent)
        }

        vault.set(VAULT_KEY, token)
        return token
    }

    override fun onActivityStarted(activity: Activity) {
        (activity as? ComponentActivity)?.let {
            _currentActivity = WeakReference(it)
        }
    }

    override fun onActivityResumed(activity: Activity) {
        (activity as? ComponentActivity)?.let {
            _currentActivity = WeakReference(it)
        }
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {}
    override fun onActivityPaused(p0: Activity) {}
    override fun onActivityStopped(p0: Activity) {}
    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}
    override fun onActivityDestroyed(p0: Activity) {}

    companion object {
        private const val VAULT_KEY = "MusicUserTokenProvider"
        val shared = MusicUserTokenProvider()
    }
}

@Suppress("ACTUAL_ANNOTATIONS_NOT_MATCH_EXPECT")
internal actual fun createUserTokenProvider(): UserTokenProvider = MusicUserTokenProvider.shared