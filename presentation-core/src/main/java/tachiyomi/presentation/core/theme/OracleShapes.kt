package tachiyomi.presentation.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** OracleRead cinematic shape scale: soft, generous corners that read as cards floating over artwork. */
val OracleShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp),
)

/** Corner radius for manga covers everywhere in the app. */
val OracleCoverShape = RoundedCornerShape(14.dp)

/** Spacing tokens on a 4dp grid. */
object OracleSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val gutter = 20.dp
    val section = 28.dp
}
