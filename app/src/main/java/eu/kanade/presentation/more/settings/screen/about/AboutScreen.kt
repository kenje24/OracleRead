package eu.kanade.presentation.more.settings.screen.about

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.more.LogoHeader
import eu.kanade.presentation.more.settings.widget.OracleCardTitle
import eu.kanade.presentation.more.settings.widget.OracleMenuRow
import eu.kanade.presentation.more.settings.widget.OracleSettingsCard
import eu.kanade.presentation.util.LocalBackPress
import eu.kanade.presentation.util.Screen
import eu.kanade.presentation.util.rememberIs24HourSystem
import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.data.updater.RELEASE_URL
import eu.kanade.tachiyomi.ui.more.NewUpdateScreen
import eu.kanade.tachiyomi.util.lang.toDateTimestampString
import eu.kanade.tachiyomi.util.system.copyToClipboard
import eu.kanade.tachiyomi.util.system.isFossBuildType
import eu.kanade.tachiyomi.util.system.isNightlyBuildType
import eu.kanade.tachiyomi.util.system.toast
import eu.kanade.tachiyomi.util.system.updaterEnabled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import logcat.LogPriority
import mihon.app.di.appGraph
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.OpenInNew
import mihon.icons.materialsymbols.rounded.Autorenew
import mihon.icons.materialsymbols.rounded.Book
import mihon.icons.materialsymbols.rounded.Info
import mihon.icons.materialsymbols.rounded.Newspaper
import mihon.icons.simpleicons.Github
import mihon.icons.simpleicons.SimpleIcons
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.release.interactor.GetApplicationRelease
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.util.Locale
import kotlin.time.Instant

object AboutScreen : Screen() {

    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        val uriHandler = LocalUriHandler.current
        val handleBack = LocalBackPress.current
        val navigator = LocalNavigator.currentOrThrow
        var isCheckingUpdates by remember { mutableStateOf(false) }
        val crashLogUtil = remember { context.appGraph.crashLogUtil }
        val is24HourSystem = rememberIs24HourSystem()

        Scaffold(
            topBar = { scrollBehavior ->
                AppBar(
                    title = stringResource(MR.strings.pref_category_about),
                    navigateUp = if (handleBack != null) handleBack::invoke else null,
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { contentPadding ->
            ScrollbarLazyColumn(
                contentPadding = contentPadding,
            ) {
                item {
                    LogoHeader(
                        tagline = stringResource(MR.strings.oracle_more_tagline),
                        supporting = remember { getVersionName(withBuildDate = false) },
                    )
                }

                item {
                    OracleSettingsCard {
                        OracleMenuRow(
                            title = stringResource(MR.strings.version),
                            // Keyed on the system 12/24h setting, which can change while the app is backgrounded.
                            // Date/time prefs only change in Appearance, which recreates this screen on return.
                            subtitle = remember(is24HourSystem) { getVersionName(withBuildDate = true) },
                            icon = MaterialSymbols.Rounded.Info,
                            onClick = {
                                val deviceInfo = crashLogUtil.getDebugInfo()
                                context.copyToClipboard("Debug information", deviceInfo)
                            },
                        )

                        if (updaterEnabled) {
                            OracleMenuRow(
                                title = stringResource(MR.strings.check_for_updates),
                                icon = MaterialSymbols.Rounded.Autorenew,
                                trailing = {
                                    AnimatedVisibility(visible = isCheckingUpdates) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(28.dp),
                                            strokeWidth = 3.dp,
                                        )
                                    }
                                },
                                onClick = {
                                    if (!isCheckingUpdates) {
                                        scope.launch {
                                            isCheckingUpdates = true

                                            checkVersion(
                                                context = context,
                                                onAvailableUpdate = { result ->
                                                    val updateScreen = NewUpdateScreen(
                                                        versionName = result.release.version,
                                                        changelogInfo = result.release.info,
                                                        releaseLink = result.release.releaseLink,
                                                        downloadLink = result.release.downloadLink,
                                                    )
                                                    navigator.push(updateScreen)
                                                },
                                                onFinish = {
                                                    isCheckingUpdates = false
                                                },
                                            )
                                        }
                                    }
                                },
                            )
                        }

                        if (!BuildConfig.DEBUG) {
                            OracleMenuRow(
                                title = stringResource(MR.strings.whats_new),
                                icon = MaterialSymbols.Rounded.Newspaper,
                                onClick = { uriHandler.openUri(RELEASE_URL) },
                            )
                        }

                        OracleMenuRow(
                            title = stringResource(MR.strings.licenses),
                            icon = MaterialSymbols.Rounded.Book,
                            onClick = { navigator.push(OpenSourceLicensesScreen()) },
                        )
                    }
                }

                item {
                    OracleSettingsCard {
                        OracleCardTitle(text = stringResource(MR.strings.oracle_about_credits))
                        AboutCardBody {
                            Text(
                                text = stringResource(MR.strings.app_name),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            AboutParagraph(stringResource(MR.strings.oracle_more_tagline))
                            AboutParagraph(stringResource(MR.strings.oracle_about_credits_built_upon))
                            Text(
                                text = stringResource(MR.strings.oracle_about_credits_thanks),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                            AboutBullet(stringResource(MR.strings.oracle_about_credits_mihon))
                            AboutBullet(stringResource(MR.strings.oracle_about_credits_tachiyomi))
                            AboutBullet(stringResource(MR.strings.oracle_about_credits_extensions))
                            AboutBullet(stringResource(MR.strings.oracle_about_credits_open_source))
                        }
                    }
                }

                item {
                    OracleSettingsCard {
                        OracleCardTitle(text = stringResource(MR.strings.oracle_about_lineage))
                        AboutCardBody {
                            Text(
                                text = stringResource(MR.strings.oracle_about_lineage_chain),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            AboutParagraph(stringResource(MR.strings.oracle_about_lineage_summary))
                        }
                    }
                }

                item {
                    OracleSettingsCard {
                        OracleCardTitle(text = stringResource(MR.strings.oracle_about_legal))
                        AboutCardBody {
                            AboutParagraph(stringResource(MR.strings.oracle_about_legal_no_hosting))
                            AboutParagraph(stringResource(MR.strings.oracle_about_legal_third_party))
                            AboutParagraph(stringResource(MR.strings.oracle_about_legal_independent))
                        }
                    }
                }

                item {
                    OracleSettingsCard(modifier = Modifier.padding(bottom = 16.dp)) {
                        OracleCardTitle(text = stringResource(MR.strings.oracle_about_links))
                        OracleMenuRow(
                            title = stringResource(MR.strings.oracle_about_repository),
                            subtitle = "github.com/kenje24/OracleRead",
                            icon = SimpleIcons.Github,
                            trailing = { OpenInNewIndicator() },
                            onClick = { uriHandler.openUri(ORACLEREAD_REPO_URL) },
                        )
                        OracleMenuRow(
                            title = stringResource(MR.strings.oracle_about_mihon_github),
                            subtitle = "github.com/mihonapp/mihon",
                            icon = SimpleIcons.Github,
                            trailing = { OpenInNewIndicator() },
                            onClick = { uriHandler.openUri(MIHON_REPO_URL) },
                        )
                    }
                }
            }
        }
    }

    private const val ORACLEREAD_REPO_URL = "https://github.com/kenje24/OracleRead"
    private const val MIHON_REPO_URL = "https://github.com/mihonapp/mihon"

    @Composable
    private fun AboutCardBody(content: @Composable ColumnScope.() -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            content = content,
        )
    }

    @Composable
    private fun AboutParagraph(text: String) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    @Composable
    private fun AboutBullet(text: String) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }

    @Composable
    private fun OpenInNewIndicator() {
        Icon(
            imageVector = MaterialSymbols.AutoMirroredRounded.OpenInNew,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }

    /**
     * Checks version and shows a user prompt if an update is available.
     */
    private suspend fun checkVersion(
        context: Context,
        onAvailableUpdate: (GetApplicationRelease.Result.NewUpdate) -> Unit,
        onFinish: () -> Unit,
    ) {
        val updateChecker = context.appGraph.updateChecker
        withContext(Dispatchers.Main) {
            try {
                when (val result = updateChecker.checkForUpdate(forceCheck = true)) {
                    is GetApplicationRelease.Result.NewUpdate -> {
                        onAvailableUpdate(result)
                    }
                    is GetApplicationRelease.Result.NoNewUpdate -> {
                        context.toast(MR.strings.update_check_no_new_updates)
                    }
                    is GetApplicationRelease.Result.OsTooOld -> {
                        context.toast(MR.strings.update_check_eol)
                    }
                }
            } catch (e: Exception) {
                context.toast(e.message)
                logcat(LogPriority.ERROR, e)
            } finally {
                onFinish()
            }
        }
    }

    fun getVersionName(withBuildDate: Boolean): String {
        return when {
            BuildConfig.DEBUG -> {
                "Debug ${BuildConfig.COMMIT_SHA}".let {
                    if (withBuildDate) {
                        "$it (${getFormattedBuildTime()})"
                    } else {
                        it
                    }
                }
            }
            isNightlyBuildType -> {
                "Nightly r${BuildConfig.COMMIT_COUNT}".let {
                    if (withBuildDate) {
                        "$it (${BuildConfig.COMMIT_SHA}, ${getFormattedBuildTime()})"
                    } else {
                        "$it (${BuildConfig.COMMIT_SHA})"
                    }
                }
            }
            else -> {
                val channel = if (isFossBuildType) "FOSS" else "Stable"
                "$channel v${BuildConfig.VERSION_NAME}".let {
                    if (withBuildDate) {
                        "$it (${getFormattedBuildTime()})"
                    } else {
                        it
                    }
                }
            }
        }
    }

    internal fun getFormattedBuildTime(): String {
        return try {
            val context = Injekt.get<Context>()
            val uiPreferences = context.appGraph.uiPreferences
            Instant.parse(BuildConfig.BUILD_TIME)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .toDateTimestampString(
                    UiPreferences.dateTimeFormat(
                        uiPreferences.dateFormat.get(),
                        uiPreferences.timeFormat.get(),
                        Locale.getDefault(),
                        DateFormat.is24HourFormat(context),
                    ),
                )
        } catch (_: Exception) {
            BuildConfig.BUILD_TIME
        }
    }
}
