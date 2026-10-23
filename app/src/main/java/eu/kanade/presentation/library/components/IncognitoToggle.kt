package eu.kanade.presentation.library.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.oracle.oracleGlassColor
import eu.kanade.presentation.oracle.oracleHairline
import mihon.app.di.appGraph
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Eyeglasses2
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState

/** One-tap incognito from the library: a glass capsule that lights up in lantern gold while history pauses. */
@Composable
fun IncognitoToggle(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val incognitoPref = remember { context.appGraph.basePreferences.incognitoMode }
    val incognito by incognitoPref.collectAsState()
    val container by animateColorAsState(
        if (incognito) MaterialTheme.colorScheme.primary else oracleGlassColor(0.7f),
        label = "incognitoContainer",
    )
    val tint by animateColorAsState(
        if (incognito) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "incognitoTint",
    )
    val description = stringResource(MR.strings.pref_incognito_mode)
    Surface(
        shape = CircleShape,
        color = container,
        border = if (incognito) null else oracleHairline(),
        modifier = modifier
            .clip(CircleShape)
            .toggleable(value = incognito, role = Role.Switch, onValueChange = incognitoPref::set)
            .semantics { contentDescription = description },
    ) {
        Icon(
            imageVector = MaterialSymbols.Rounded.Eyeglasses2,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 7.dp)
                .size(20.dp),
        )
    }
}
