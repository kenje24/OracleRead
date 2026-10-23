package eu.kanade.presentation.more.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.more.OracleLogoGlow
import eu.kanade.presentation.oracle.OracleGlass
import soup.compose.material.motion.animation.materialSharedAxisX
import soup.compose.material.motion.animation.rememberSlideDistance
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.theme.OracleSpacing

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onRestoreBackup: () -> Unit,
) {
    val slideDistance = rememberSlideDistance()

    var currentStep by rememberSaveable { mutableIntStateOf(0) }
    val steps = remember {
        listOf(
            ThemeStep(),
            StorageStep(),
            PermissionStep(),
            GuidesStep(onRestoreBackup = onRestoreBackup),
        )
    }
    val isLastStep = currentStep == steps.lastIndex

    BackHandler(enabled = currentStep != 0) {
        currentStep--
    }

    val background = MaterialTheme.colorScheme.background
    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            0f to background.copy(alpha = 0f),
                            0.35f to background.copy(alpha = 0.92f),
                            1f to background,
                        ),
                    )
                    .windowInsetsPadding(NavigationBarDefaults.windowInsets)
                    .padding(horizontal = OracleSpacing.gutter)
                    .padding(top = 20.dp, bottom = 12.dp),
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = steps[currentStep].isComplete,
                    onClick = {
                        if (isLastStep) {
                            onComplete()
                        } else {
                            currentStep++
                        }
                    },
                ) {
                    Text(
                        text = stringResource(
                            if (isLastStep) {
                                MR.strings.onboarding_action_finish
                            } else {
                                MR.strings.onboarding_action_next
                            },
                        ),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        },
    ) { paddingValues ->
        Box {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(horizontal = OracleSpacing.gutter),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                OracleLogoGlow(
                    modifier = Modifier.padding(top = 12.dp),
                    glowSize = 184.dp,
                    logoSize = 72.dp,
                )
                Text(
                    text = stringResource(MR.strings.oracle_onboarding_heading, stringResource(MR.strings.app_name)),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .offset(y = (-16).dp)
                        .semantics { heading() },
                )
                Text(
                    text = stringResource(MR.strings.onboarding_description),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )

                StepIndicator(
                    count = steps.size,
                    current = currentStep,
                    modifier = Modifier.padding(top = 20.dp, bottom = 16.dp),
                )

                OracleGlass(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                ) {
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            materialSharedAxisX(
                                forward = targetState > initialState,
                                slideDistance = slideDistance,
                            )
                        },
                        label = "stepContent",
                    ) {
                        Box(modifier = Modifier.padding(vertical = 8.dp)) {
                            steps[it].Content()
                        }
                    }
                }
            }

            // Status bar scrim, so scrolled content doesn't run under the system icons
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(paddingValues.calculateTopPadding())
                    .background(background.copy(alpha = 0.9f)),
            )
        }
    }
}

/** Row of small pills; the current step's pill stretches and fills with the lantern primary. */
@Composable
private fun StepIndicator(
    count: Int,
    current: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.semantics { contentDescription = "${current + 1} / $count" },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { index ->
            val selected = index == current
            val width by animateDpAsState(if (selected) 24.dp else 8.dp, label = "stepPill")
            Box(
                modifier = Modifier
                    .width(width)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (index <= current) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                    ),
            )
        }
    }
}
