package eu.kanade.presentation.more.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.domain.ui.model.NavigationBarMode
import eu.kanade.domain.ui.model.setAppCompatDelegateThemeMode
import eu.kanade.presentation.more.settings.widget.AppThemeModePreferenceWidget
import eu.kanade.presentation.more.settings.widget.AppThemePreferenceWidget
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.util.collectAsState
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

internal class ThemeStep : OnboardingStep {

    override val isComplete: Boolean = true

    private val uiPreferences: UiPreferences = Injekt.get()

    @Composable
    override fun Content() {
        val themeModePref = uiPreferences.themeMode
        val themeMode by themeModePref.collectAsState()

        val appThemePref = uiPreferences.appTheme
        val appTheme by appThemePref.collectAsState()

        val amoledPref = uiPreferences.themeDarkAmoled
        val amoled by amoledPref.collectAsState()

        val navigationBarModePref = uiPreferences.navigationBarMode
        val navigationBarMode by navigationBarModePref.collectAsState()

        Column {
            AppThemeModePreferenceWidget(
                value = themeMode,
                onItemClick = {
                    themeModePref.set(it)
                    setAppCompatDelegateThemeMode(it)
                },
            )

            AppThemePreferenceWidget(
                value = appTheme,
                amoled = amoled,
                onItemClick = { appThemePref.set(it) },
            )

            Text(
                text = "Navigation layout",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(
                    start = MaterialTheme.padding.medium,
                    top = MaterialTheme.padding.medium,
                    end = MaterialTheme.padding.medium,
                ),
            )
            AssistChip(
                onClick = { navigationBarModePref.set(NavigationBarMode.COMPACT) },
                label = { Text("Compact: 3 tabs, Browse expands") },
                modifier = Modifier.padding(horizontal = MaterialTheme.padding.medium),
                enabled = navigationBarMode != NavigationBarMode.COMPACT,
            )
            AssistChip(
                onClick = { navigationBarModePref.set(NavigationBarMode.FULL) },
                label = { Text("Always show 5 tabs") },
                modifier = Modifier.padding(horizontal = MaterialTheme.padding.medium),
                enabled = navigationBarMode != NavigationBarMode.FULL,
            )
        }
    }
}
