package mihon.feature.community

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.util.Screen
import kotlinx.coroutines.flow.update
import mihon.domain.community.repository.CommunityRepository
import tachiyomi.core.common.util.lang.launchIO
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.padding
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class CommunityAccountScreen : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { CommunityAccountScreenModel() }
        val state by screenModel.state.collectAsState()

        Scaffold(
            topBar = {
                AppBar(
                    title = "OracleRead Account",
                    navigateUp = navigator::pop,
                )
            },
        ) { padding ->
            Column(
                modifier = androidx.compose.ui.Modifier
                    .padding(padding)
                    .padding(MaterialTheme.padding.medium),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.medium),
            ) {
                if (state.signedIn) {
                    Text("You are logged in.", style = MaterialTheme.typography.titleMedium)
                    state.message?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Button(onClick = screenModel::logout) {
                        Text("Log out")
                    }
                } else {
                    AuthPanel(
                        signedIn = false,
                        working = state.working,
                        message = state.message,
                        onSignIn = screenModel::signIn,
                        onSignUp = screenModel::signUp,
                    )
                }
            }
        }
    }
}

class CommunityAccountScreenModel(
    private val repository: CommunityRepository = Injekt.get(),
) : StateScreenModel<CommunityAccountScreenModel.State>(
    State(signedIn = repository.isSignedIn),
) {

    fun signIn(email: String, password: String) {
        screenModelScope.launchIO {
            mutableState.update { it.copy(working = true, message = null) }
            repository.signIn(email, password)
                .onSuccess { mutableState.value = State(signedIn = true, message = "Logged in.") }
                .onFailure { error -> mutableState.update { it.copy(working = false, message = friendlyAuthError(error.message, "Unable to log in")) } }
        }
    }

    fun signUp(email: String, password: String, username: String) {
        screenModelScope.launchIO {
            mutableState.update { it.copy(working = true, message = null) }
            repository.signUp(email, password, username)
                .onSuccess {
                    mutableState.value = State(
                        signedIn = repository.isSignedIn,
                        message = if (repository.isSignedIn) "Account created." else "Account created. Log in to continue.",
                    )
                }
                .onFailure { error -> mutableState.update { it.copy(working = false, message = friendlyAuthError(error.message, "Unable to register")) } }
        }
    }

    fun logout() {
        screenModelScope.launchIO {
            repository.clearSession()
            mutableState.value = State(signedIn = false, message = "Logged out.")
        }
    }

    data class State(
        val signedIn: Boolean,
        val working: Boolean = false,
        val message: String? = null,
    )
}

private fun friendlyAuthError(message: String?, fallback: String): String {
    val text = message.orEmpty()
    return when {
        text.contains("already", ignoreCase = true) || text.contains("registered", ignoreCase = true) ->
            "That email is already registered. Log in instead."
        text.contains("invalid", ignoreCase = true) || text.contains("credentials", ignoreCase = true) ->
            "Email or password is incorrect."
        text.isNotBlank() -> text
        else -> fallback
    }
}
