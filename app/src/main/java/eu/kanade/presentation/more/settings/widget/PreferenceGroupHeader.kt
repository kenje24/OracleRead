package eu.kanade.presentation.more.settings.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Group title in the headline serif and lantern primary, sitting above the group's glass card. */
@Composable
fun PreferenceGroupHeader(title: String, highlighted: Boolean = false) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .highlightBackground(highlighted)
            .padding(top = 22.dp, bottom = 10.dp),
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(horizontal = OracleCardMargin + 8.dp)
                .semantics { heading() },
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp, lineHeight = 26.sp),
        )
    }
}
