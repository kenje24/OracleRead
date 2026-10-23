package eu.kanade.tachiyomi.extension.installer

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.IntentSanitizer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.notification.Notifications
import eu.kanade.tachiyomi.extension.model.InstallStep
import eu.kanade.tachiyomi.util.lang.use
import eu.kanade.tachiyomi.util.system.cancelNotification
import eu.kanade.tachiyomi.util.system.getParcelableExtraCompat
import eu.kanade.tachiyomi.util.system.getUriSize
import eu.kanade.tachiyomi.util.system.notify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import logcat.LogPriority
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.core.common.util.system.logcat
import tachiyomi.i18n.MR

class PackageInstallerInstaller(private val service: Service) : Installer(service) {

    private val packageInstaller = service.packageManager.packageInstaller

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val packageActionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
            when (status) {
                PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                    val userAction = intent.getParcelableExtraCompat<Intent>(Intent.EXTRA_INTENT)
                        ?.run {
                            // Doesn't actually needed as the receiver is actually not exported
                            // But the warnings can't be suppressed without this
                            IntentSanitizer.Builder()
                                .allowAction(this.action!!)
                                .allowExtra(PackageInstaller.EXTRA_SESSION_ID) { id -> id == activeSession?.second }
                                .allowAnyComponent()
                                .allowPackage {
                                    // There is no way to check the actual installer name so allow all.
                                    true
                                }
                                .build()
                                .sanitizeByFiltering(this)
                        }
                    if (userAction == null) {
                        logcat(LogPriority.INFO) {
                            "The system installer asked for an action it didn't provide: $intent"
                        }
                        continueQueue(InstallStep.Error)
                        return
                    }
                    userAction.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    try {
                        confirmWhenVisible(userAction)
                    } catch (e: ActivityNotFoundException) {
                        // Some devices have the system installer's confirmation screen disabled or removed
                        logcat(LogPriority.WARN, e) { "Nothing can confirm the install" }
                        activeSession?.let { (_, sessionId) ->
                            runCatching { packageInstaller.abandonSession(sessionId) }
                        }
                        continueQueue(InstallStep.Error)
                    }
                }
                PackageInstaller.STATUS_FAILURE_ABORTED -> {
                    clearPendingConfirmation()
                    continueQueue(InstallStep.Idle)
                }
                PackageInstaller.STATUS_SUCCESS -> {
                    clearPendingConfirmation()
                    continueQueue(InstallStep.Installed)
                }
                else -> {
                    clearPendingConfirmation()
                    logcat(LogPriority.INFO) {
                        "The system installer failed ($status): ${intent.getStringExtra(
                            PackageInstaller.EXTRA_STATUS_MESSAGE,
                        )}"
                    }
                    continueQueue(InstallStep.Error)
                }
            }
        }
    }

    @Volatile
    private var activeSession: Pair<Entry, Int>? = null

    // A confirmation waiting for the app to come back to the foreground
    private var pendingConfirmation: LifecycleEventObserver? = null

    /**
     * Shows the system installer's confirmation. Android blocks activity starts from the background (the app
     * left the foreground while the extension downloaded), which used to drop the confirmation silently and
     * leave the extension stuck at "Installing". Then the confirmation is offered as a notification and opens
     * by itself as soon as the app is back in the foreground.
     */
    private fun confirmWhenVisible(userAction: Intent) {
        val lifecycle = ProcessLifecycleOwner.get().lifecycle
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            service.startActivity(userAction)
            return
        }
        logcat(LogPriority.INFO) { "App is in the background, deferring the install confirmation" }
        clearPendingConfirmation()
        service.notify(
            Notifications.ID_EXTENSION_INSTALL_CONFIRM,
            Notifications.CHANNEL_EXTENSIONS_UPDATE,
        ) {
            setSmallIcon(R.drawable.ic_mihon)
            setContentTitle(service.stringResource(MR.strings.ext_install_confirm_notif_title))
            setContentText(service.stringResource(MR.strings.ext_install_confirm_notif_text))
            setAutoCancel(true)
            setContentIntent(
                PendingIntent.getActivity(
                    service,
                    activeSession?.second ?: 0,
                    userAction,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                ),
            )
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                clearPendingConfirmation()
                try {
                    service.startActivity(userAction)
                } catch (e: ActivityNotFoundException) {
                    logcat(LogPriority.WARN, e) { "Nothing can confirm the install" }
                    activeSession?.let { (_, sessionId) -> runCatching { packageInstaller.abandonSession(sessionId) } }
                    continueQueue(InstallStep.Error)
                }
            }
        }
        pendingConfirmation = observer
        lifecycle.addObserver(observer)
    }

    private fun clearPendingConfirmation() {
        pendingConfirmation?.let { ProcessLifecycleOwner.get().lifecycle.removeObserver(it) }
        pendingConfirmation = null
        service.cancelNotification(Notifications.ID_EXTENSION_INSTALL_CONFIRM)
    }

    // Always ready
    override var ready = true

    override fun processEntry(entry: Entry) {
        super.processEntry(entry)
        activeSession = null
        scope.launch {
            try {
                val installParams = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    installParams.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
                }
                activeSession = entry to packageInstaller.createSession(installParams)
                val fileSize = service.getUriSize(entry.uri) ?: throw IllegalStateException()
                installParams.setSize(fileSize)

                val inputStream = service.contentResolver.openInputStream(entry.uri) ?: throw IllegalStateException()
                val session = packageInstaller.openSession(activeSession!!.second)
                val outputStream = session.openWrite(entry.downloadId.toString(), 0, fileSize)
                session.use {
                    arrayOf(inputStream, outputStream).use {
                        inputStream.copyTo(outputStream)
                        session.fsync(outputStream)
                    }
                    service.contentResolver.delete(entry.uri, null, null)

                    val intentSender = PendingIntent.getBroadcast(
                        service,
                        activeSession!!.second,
                        Intent(INSTALL_ACTION).setPackage(service.packageName),
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0,
                    ).intentSender
                    @SuppressLint("RequestInstallPackagesPolicy")
                    session.commit(intentSender)
                }
            } catch (e: Exception) {
                logcat(LogPriority.INFO, e) { "Failed to install extension ${entry.downloadId} ${entry.uri}" }
                activeSession?.let { (_, sessionId) ->
                    // Already abandoned when the entry was cancelled mid-copy
                    runCatching { packageInstaller.abandonSession(sessionId) }
                }
                if (getActiveEntry() == entry) continueQueue(InstallStep.Error)
            }
        }
    }

    override fun cancelEntry(entry: Entry): Boolean {
        activeSession?.let { (activeEntry, sessionId) ->
            if (activeEntry == entry) {
                return try {
                    packageInstaller.abandonSession(sessionId)
                    false
                } catch (_: SecurityException) {
                    // Highly likely the session has succeeded
                    true
                }
            }
        }
        return true
    }

    override fun onDestroy() {
        clearPendingConfirmation()
        service.unregisterReceiver(packageActionReceiver)
        scope.cancel()
        super.onDestroy()
    }

    init {
        ContextCompat.registerReceiver(
            service,
            packageActionReceiver,
            IntentFilter(INSTALL_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }
}

private const val INSTALL_ACTION = "PackageInstallerInstaller.INSTALL_ACTION"
