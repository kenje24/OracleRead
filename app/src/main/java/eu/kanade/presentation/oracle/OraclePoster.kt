package eu.kanade.presentation.oracle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.manga.components.MangaCover
import tachiyomi.presentation.core.theme.OracleCoverShape

/**
 * Cover card: rounded art with a soft bottom vignette, status pills on the art and the title underneath.
 * Selection draws a lantern ring around the art instead of tinting it, so the cover stays readable.
 */
@Composable
fun OraclePoster(
    coverData: Any?,
    title: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    isSelected: Boolean = false,
    dimmed: Boolean = false,
    titleInside: Boolean = false,
    badges: @Composable (Modifier) -> Unit = {},
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(MangaCover.Book.ratio)
                .then(
                    if (isSelected) {
                        Modifier.border(3.dp, MaterialTheme.colorScheme.primary, OracleCoverShape)
                    } else {
                        Modifier
                    },
                )
                .clip(OracleCoverShape),
        ) {
            MangaCover.Book(
                data = coverData,
                shape = OracleCoverShape,
                contentDescription = "",
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (dimmed) 0.38f else 1f),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (titleInside) PosterVignetteDeep else PosterVignetteLight),
            )
            badges(Modifier.align(Alignment.TopStart).padding(6.dp))
            if (titleInside) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                )
            }
            overlay()
        }
        if (!titleInside) {
            Column(modifier = Modifier.padding(horizontal = 2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** Small capsule used for counts and states on top of artwork (unread, downloaded, language, local). */
@Composable
fun OraclePill(
    text: String,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.primary,
    content: Color = MaterialTheme.colorScheme.onPrimary,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = content,
        maxLines = 1,
        modifier = modifier
            .clip(CircleShape)
            .background(container)
            .padding(horizontal = 7.dp, vertical = 2.dp),
    )
}

@Composable
fun OraclePillRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) { content() }
}

// OracleRead: shared by every poster instead of built again each time one is composed while scrolling
private val PosterVignetteDeep = Brush.verticalGradient(
    0.55f to Color.Transparent,
    1f to Color.Black.copy(alpha = 0.85f),
)
private val PosterVignetteLight = Brush.verticalGradient(
    0.55f to Color.Transparent,
    1f to Color.Black.copy(alpha = 0.35f),
)
