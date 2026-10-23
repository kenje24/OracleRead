package eu.kanade.presentation.reader.appbars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import eu.kanade.tachiyomi.ui.reader.setting.ReaderOrientation
import eu.kanade.tachiyomi.ui.reader.setting.ReadingMode
import mihon.icons.custommaterialsymbols.CustomMaterialSymbols
import mihon.icons.custommaterialsymbols.rounded.CropOff
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Crop
import mihon.icons.materialsymbols.rounded.Settings
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

/**
 * Quick reader settings. [ReaderAppBars] places this inside a floating glass capsule, so it draws no
 * background of its own.
 */
@Composable
fun ReaderBottomBar(
    readingMode: ReadingMode,
    onClickReadingMode: () -> Unit,
    orientation: ReaderOrientation,
    onClickOrientation: () -> Unit,
    cropEnabled: Boolean,
    onClickCropBorder: () -> Unit,
    onClickSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .pointerInput(Unit) {}
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClickReadingMode) {
                Icon(
                    imageVector = readingMode.icon,
                    contentDescription = stringResource(MR.strings.viewer),
                )
            }

            IconButton(onClick = onClickOrientation) {
                Icon(
                    imageVector = orientation.icon,
                    contentDescription = stringResource(MR.strings.rotation_type),
                )
            }

            IconButton(onClick = onClickCropBorder) {
                Icon(
                    imageVector = if (cropEnabled) {
                        MaterialSymbols.Rounded.Crop
                    } else {
                        CustomMaterialSymbols.Rounded.CropOff
                    },
                    contentDescription = stringResource(MR.strings.pref_crop_borders),
                    // The icon shape already tells on from off; the accent only reinforces it
                    tint = if (cropEnabled) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                )
            }

            IconButton(onClick = onClickSettings) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.Settings,
                    contentDescription = stringResource(MR.strings.action_settings),
                )
            }
        }
    }
}
