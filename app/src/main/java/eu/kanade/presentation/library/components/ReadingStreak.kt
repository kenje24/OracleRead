package eu.kanade.presentation.library.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.oracle.OracleGlass
import eu.kanade.presentation.oracle.rememberAmbientMotion
import eu.kanade.presentation.oracle.rememberAmbientSeconds
import mihon.app.di.appGraph
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.LocalFireDepartment
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.util.collectAsState
import java.time.LocalDate
import kotlin.math.PI
import kotlin.math.sin

private const val FLAME_BREATH_RADIANS_PER_SECOND = (2 * PI / 2.2).toFloat()

/**
 * Days in a row the user has read something. The flame breathes gently while the streak is alive and the
 * streak resets once a full day passes without reading. Updated by the reader on chapter progress.
 */
@Composable
fun ReadingStreak(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val libraryPreferences = remember { context.appGraph.libraryPreferences }
    val streakDays by libraryPreferences.readingStreakDays.collectAsState()
    val streakDate by libraryPreferences.readingStreakDate.collectAsState()

    LaunchedEffect(streakDate) {
        val lastReadDate = runCatching { LocalDate.parse(streakDate) }.getOrNull()
        if (lastReadDate == null || lastReadDate.isBefore(LocalDate.now().minusDays(1))) {
            libraryPreferences.readingStreakDays.set(0)
        }
    }

    // On the shared ambient clock and read in the layer, so the breathing flame neither recomposes the streak nor
    // redraws the screen more often than the rest of the ambience. A 2.2 s breath between 92% and 108%.
    val breath = rememberAmbientSeconds(active = streakDays > 0 && rememberAmbientMotion(), initialSeconds = 0f)
    val description = pluralStringResource(MR.plurals.reading_streak_days, streakDays, streakDays)

    OracleGlass(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = description },
        shape = CircleShape,
        alpha = 0.7f,
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = MaterialSymbols.Rounded.LocalFireDepartment,
                contentDescription = null,
                tint = if (streakDays > 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer {
                        val scale = 1f + 0.08f * sin(breath.floatValue * FLAME_BREATH_RADIANS_PER_SECOND)
                        scaleX = scale
                        scaleY = scale
                    },
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "$streakDays",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
