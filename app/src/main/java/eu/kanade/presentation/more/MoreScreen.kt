package eu.kanade.presentation.more

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.more.settings.screen.about.AboutScreen
import eu.kanade.presentation.more.settings.widget.OracleCardMargin
import eu.kanade.presentation.more.settings.widget.OracleIconTile
import eu.kanade.presentation.more.settings.widget.OracleMenuRow
import eu.kanade.presentation.more.settings.widget.OracleSettingsCard
import eu.kanade.presentation.oracle.OracleGlass
import eu.kanade.tachiyomi.ui.more.DownloadQueueState
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.Help
import mihon.icons.materialsymbols.automirroredrounded.Label
import mihon.icons.materialsymbols.rounded.AccountCircle
import mihon.icons.materialsymbols.rounded.CloudOff
import mihon.icons.materialsymbols.rounded.Download
import mihon.icons.materialsymbols.rounded.Eyeglasses2
import mihon.icons.materialsymbols.rounded.Info
import mihon.icons.materialsymbols.rounded.QueryStats
import mihon.icons.materialsymbols.rounded.Settings
import mihon.icons.materialsymbols.rounded.Storage
import mihon.icons.materialsymbols.roundedfilled.VolunteerActivism
import tachiyomi.core.common.Constants
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun MoreScreen(
    downloadQueueStateProvider: () -> DownloadQueueState,
    downloadedOnly: Boolean,
    onDownloadedOnlyChange: (Boolean) -> Unit,
    incognitoMode: Boolean,
    onIncognitoModeChange: (Boolean) -> Unit,
    onClickDownloadQueue: () -> Unit,
    onClickCategories: () -> Unit,
    onClickStats: () -> Unit,
    onClickDataAndStorage: () -> Unit,
    onClickSettings: () -> Unit,
    onClickAccount: () -> Unit,
    onClickSupport: () -> Unit,
    onClickAbout: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current

    Scaffold { contentPadding ->
        ScrollbarLazyColumn(contentPadding = contentPadding) {
            item {
                val version = remember { AboutScreen.getVersionName(withBuildDate = false) }
                LogoHeader(
                    tagline = stringResource(MR.strings.oracle_more_tagline),
                    supporting = version,
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(horizontal = OracleCardMargin, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    GlassToggleCard(
                        title = stringResource(MR.strings.label_downloaded_only),
                        subtitle = stringResource(MR.strings.downloaded_only_summary),
                        icon = MaterialSymbols.Rounded.CloudOff,
                        checked = downloadedOnly,
                        onCheckedChange = onDownloadedOnlyChange,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                    GlassToggleCard(
                        title = stringResource(MR.strings.pref_incognito_mode),
                        subtitle = stringResource(MR.strings.pref_incognito_mode_summary),
                        icon = MaterialSymbols.Rounded.Eyeglasses2,
                        checked = incognitoMode,
                        onCheckedChange = onIncognitoModeChange,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
            }

            item {
                OracleSettingsCard(modifier = Modifier.padding(top = 6.dp)) {
                    val downloadQueueState = downloadQueueStateProvider()
                    OracleMenuRow(
                        title = stringResource(MR.strings.label_download_queue),
                        subtitle = when (downloadQueueState) {
                            DownloadQueueState.Stopped -> null
                            is DownloadQueueState.Paused -> {
                                val pending = downloadQueueState.pending
                                if (pending == 0) {
                                    stringResource(MR.strings.paused)
                                } else {
                                    "${stringResource(MR.strings.paused)} • ${
                                        pluralStringResource(
                                            MR.plurals.download_queue_summary,
                                            count = pending,
                                            pending,
                                        )
                                    }"
                                }
                            }
                            is DownloadQueueState.Downloading -> {
                                val pending = downloadQueueState.pending
                                pluralStringResource(MR.plurals.download_queue_summary, count = pending, pending)
                            }
                        },
                        icon = MaterialSymbols.Rounded.Download,
                        onClick = onClickDownloadQueue,
                    )
                    OracleMenuRow(
                        title = stringResource(MR.strings.categories),
                        icon = MaterialSymbols.AutoMirroredRounded.Label,
                        onClick = onClickCategories,
                    )
                    OracleMenuRow(
                        title = stringResource(MR.strings.label_stats),
                        icon = MaterialSymbols.Rounded.QueryStats,
                        onClick = onClickStats,
                    )
                    OracleMenuRow(
                        title = stringResource(MR.strings.label_data_storage),
                        icon = MaterialSymbols.Rounded.Storage,
                        onClick = onClickDataAndStorage,
                    )
                }
            }

            item {
                OracleSettingsCard {
                    OracleMenuRow(
                        title = stringResource(MR.strings.label_settings),
                        icon = MaterialSymbols.Rounded.Settings,
                        onClick = onClickSettings,
                    )
                    OracleMenuRow(
                        title = stringResource(MR.strings.oracle_more_account),
                        icon = MaterialSymbols.Rounded.AccountCircle,
                        onClick = onClickAccount,
                    )
                }
            }

            item {
                OracleSettingsCard(modifier = Modifier.padding(bottom = 16.dp)) {
                    OracleMenuRow(
                        title = stringResource(MR.strings.label_support_us),
                        icon = MaterialSymbols.RoundedFilled.VolunteerActivism,
                        onClick = onClickSupport,
                    )
                    OracleMenuRow(
                        title = stringResource(MR.strings.pref_category_about),
                        icon = MaterialSymbols.Rounded.Info,
                        onClick = onClickAbout,
                    )
                    OracleMenuRow(
                        title = stringResource(MR.strings.label_help),
                        icon = MaterialSymbols.AutoMirroredRounded.Help,
                        onClick = { uriHandler.openUri(Constants.URL_HELP) },
                    )
                }
            }
        }
    }
}

/** A glass card holding one of the quick toggles (downloaded only, incognito). The whole card toggles. */
@Composable
private fun GlassToggleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.large
    val tint by animateColorAsState(
        if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "toggleTint",
    )
    OracleGlass(
        modifier = modifier
            .clip(shape)
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            ),
        shape = shape,
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OracleIconTile(icon = icon, tint = tint)
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = checked,
                    onCheckedChange = null,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
