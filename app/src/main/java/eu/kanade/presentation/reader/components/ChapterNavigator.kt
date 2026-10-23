package eu.kanade.presentation.reader.components

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalSlider
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.oracle.OracleGlass
import eu.kanade.presentation.oracle.oracleGlassColor
import eu.kanade.presentation.oracle.oracleHairline
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import eu.kanade.presentation.util.isTabletUi
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.SkipNext
import mihon.icons.materialsymbols.rounded.SkipPrevious
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import kotlin.math.roundToInt

enum class ChapterNavigatorType {
    HORIZONTAL_LTR,
    HORIZONTAL_RTL,
    VERTICAL_LEFT,
    VERTICAL_RIGHT,
    ;

    fun isHorizontal() = this in setOf(HORIZONTAL_LTR, HORIZONTAL_RTL)
}

@Composable
fun ChapterNavigator(
    type: ChapterNavigatorType,
    onNextChapter: () -> Unit,
    enabledNext: Boolean,
    onPreviousChapter: () -> Unit,
    enabledPrevious: Boolean,
    currentPage: Int,
    totalPages: Int,
    onPageIndexChange: (Int) -> Unit,
    onPageIndexChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    val state = if (totalPages > 1) {
        key(totalPages) {
            rememberSliderState(
                value = currentPage.toFloat(),
                steps = totalPages - 2,
                trackRange = 1f..totalPages.toFloat(),
            )
        }
            .also {
                it.value = currentPage.toFloat()
            }
    } else {
        null
    }

    val interactionSource = remember { MutableInteractionSource() }
    val sliderDragged by interactionSource.collectIsDraggedAsState()
    LaunchedEffect(currentPage) {
        if (sliderDragged) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val isTabletUi = isTabletUi()

    val onPageChange: (Int) -> Unit = { onPageIndexChange(it - 1) }

    if (type.isHorizontal()) {
        HorizontalChapterNavigator(
            isRtl = type == ChapterNavigatorType.HORIZONTAL_RTL,
            state = state,
            onNextChapter = onNextChapter,
            enabledNext = enabledNext,
            onPreviousChapter = onPreviousChapter,
            enabledPrevious = enabledPrevious,
            currentPage = currentPage,
            totalPages = totalPages,
            onPageChange = onPageChange,
            onPageChangeFinished = onPageIndexChangeFinished,
            interactionSource = interactionSource,
            // The reader chrome already keeps a margin from the screen edges
            mainAxisPadding = if (isTabletUi) 24.dp else 0.dp,
            modifier = modifier,
        )
    } else {
        VerticalChapterNavigator(
            state = state,
            onNextChapter = onNextChapter,
            enabledNext = enabledNext,
            onPreviousChapter = onPreviousChapter,
            enabledPrevious = enabledPrevious,
            currentPage = currentPage,
            totalPages = totalPages,
            onPageChange = onPageChange,
            onPageChangeFinished = onPageIndexChangeFinished,
            interactionSource = interactionSource,
            mainAxisPadding = if (isTabletUi) 24.dp else 8.dp,
            modifier = modifier,
        )
    }
}

@Composable
private fun HorizontalChapterNavigator(
    isRtl: Boolean,
    state: SliderState?,
    onNextChapter: () -> Unit,
    enabledNext: Boolean,
    onPreviousChapter: () -> Unit,
    enabledPrevious: Boolean,
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit,
    onPageChangeFinished: () -> Unit,
    interactionSource: MutableInteractionSource,
    mainAxisPadding: Dp,
    modifier: Modifier = Modifier,
) {
    val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    // We explicitly handle direction based on the reader viewer rather than the system direction
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = mainAxisPadding),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChapterNavigatorButton(
                enabled = if (isRtl) enabledNext else enabledPrevious,
                onClick = if (isRtl) onNextChapter else onPreviousChapter,
                imageVector = MaterialSymbols.Rounded.SkipPrevious,
                contentDescription = stringResource(
                    if (isRtl) MR.strings.action_next_chapter else MR.strings.action_previous_chapter,
                ),
            )

            if (state != null) {
                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    OracleGlass(
                        modifier = Modifier.weight(1f),
                        shape = CircleShape,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Reserves the width of the total so the slider doesn't shift while paging
                            PageNumberPill(page = currentPage, widthReference = totalPages, emphasized = true)

                            Slider(
                                state = state,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp),
                                onValueChange = { onPageChange(it.roundToInt()) },
                                onValueChangeFinished = onPageChangeFinished,
                                colors = readerSliderColors(),
                                interactionSource = interactionSource,
                            )

                            PageNumberPill(page = totalPages, widthReference = null, emphasized = false)
                        }
                    }
                }
            } else {
                Spacer(Modifier.weight(1f))
            }

            ChapterNavigatorButton(
                enabled = if (isRtl) enabledPrevious else enabledNext,
                onClick = if (isRtl) onPreviousChapter else onNextChapter,
                imageVector = MaterialSymbols.Rounded.SkipNext,
                contentDescription = stringResource(
                    if (isRtl) MR.strings.action_previous_chapter else MR.strings.action_next_chapter,
                ),
            )
        }
    }
}

@Composable
private fun VerticalChapterNavigator(
    state: SliderState?,
    onNextChapter: () -> Unit,
    enabledNext: Boolean,
    onPreviousChapter: () -> Unit,
    enabledPrevious: Boolean,
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit,
    onPageChangeFinished: () -> Unit,
    interactionSource: MutableInteractionSource,
    mainAxisPadding: Dp,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = mainAxisPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ChapterNavigatorButton(
            enabled = enabledPrevious,
            onClick = onPreviousChapter,
            imageVector = MaterialSymbols.Rounded.SkipPrevious,
            contentDescription = stringResource(MR.strings.action_previous_chapter),
            iconModifier = Modifier.rotate(90f),
        )

        if (state != null) {
            OracleGlass(
                modifier = Modifier.weight(1f),
                shape = CircleShape,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PageNumberPill(page = currentPage, widthReference = totalPages, emphasized = true)

                    VerticalSlider(
                        state = state,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        onValueChange = { onPageChange(it.roundToInt()) },
                        onValueChangeFinished = onPageChangeFinished,
                        colors = readerSliderColors(),
                        interactionSource = interactionSource,
                    )

                    PageNumberPill(page = totalPages, widthReference = null, emphasized = false)
                }
            }
        } else {
            Spacer(Modifier.weight(1f))
        }

        ChapterNavigatorButton(
            enabled = enabledNext,
            onClick = onNextChapter,
            imageVector = MaterialSymbols.Rounded.SkipNext,
            contentDescription = stringResource(MR.strings.action_next_chapter),
            iconModifier = Modifier.rotate(90f),
        )
    }
}

/** Round glass button that jumps to the previous or next chapter. */
@Composable
private fun ChapterNavigatorButton(
    enabled: Boolean,
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String,
    iconModifier: Modifier = Modifier,
) {
    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(ChapterNavigatorButtonSize)
            .border(oracleHairline(), CircleShape),
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = oracleGlassColor(),
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = oracleGlassColor(alpha = 0.6f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        ),
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = iconModifier,
        )
    }
}

/**
 * Page number in a pill. The current page is filled with the accent and the total stays quiet.
 * [widthReference] reserves the width of that number so the pill doesn't resize while paging.
 */
@Composable
private fun PageNumberPill(
    page: Int,
    widthReference: Int?,
    emphasized: Boolean,
) {
    val style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum")
    Surface(
        shape = CircleShape,
        color = if (emphasized) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        contentColor = if (emphasized) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
    ) {
        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = 32.dp)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = page.toString(), style = style)
            widthReference?.let {
                Text(
                    text = it.toString(),
                    style = style,
                    modifier = Modifier
                        .alpha(0f)
                        .clearAndSetSemantics {},
                )
            }
        }
    }
}

@Composable
private fun readerSliderColors(): SliderColors {
    val colors = MaterialTheme.colorScheme
    return SliderDefaults.colors(
        thumbColor = colors.primary,
        activeTrackColor = colors.primary,
        activeTickColor = colors.onPrimary.copy(alpha = 0.38f),
        inactiveTrackColor = colors.primary.copy(alpha = 0.24f),
        inactiveTickColor = colors.primary.copy(alpha = 0.6f),
    )
}

private val ChapterNavigatorButtonSize = 48.dp

@Preview
@Composable
private fun ChapterNavigatorPreview() {
    var currentPage by remember { mutableIntStateOf(1) }
    TachiyomiPreviewTheme {
        ChapterNavigator(
            type = ChapterNavigatorType.VERTICAL_RIGHT,
            onNextChapter = {},
            enabledNext = true,
            onPreviousChapter = {},
            enabledPrevious = true,
            currentPage = currentPage,
            totalPages = 10,
            onPageIndexChange = { currentPage = (it + 1) },
            onPageIndexChangeFinished = {},
        )
    }
}
