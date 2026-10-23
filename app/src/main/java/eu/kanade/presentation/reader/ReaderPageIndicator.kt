package eu.kanade.presentation.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.oracle.oracleGlassColor
import eu.kanade.presentation.oracle.oracleHairline
import eu.kanade.presentation.theme.TachiyomiPreviewTheme

/**
 * Small glass pill with the page position, shown over the page while the reader chrome is hidden. A theme
 * surface sits behind the text so it stays readable on both white and dark pages. It is drawn without a
 * Surface on purpose: it must not swallow taps meant for the reader's tap zones.
 */
@Composable
fun ReaderPageIndicator(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier,
) {
    if (currentPage <= 0 || totalPages <= 0) return

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(bottom = 6.dp)
            .background(oracleGlassColor(alpha = 0.78f), CircleShape)
            .border(oracleHairline(), CircleShape)
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(
            text = "$currentPage / $totalPages",
            style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@PreviewLightDark
@Composable
private fun ReaderPageIndicatorPreview() {
    TachiyomiPreviewTheme {
        Surface {
            ReaderPageIndicator(currentPage = 10, totalPages = 69)
        }
    }
}
