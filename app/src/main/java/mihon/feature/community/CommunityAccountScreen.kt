package mihon.feature.community

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import dev.zacsweers.metrox.viewmodel.metroViewModel
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.util.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mihon.domain.community.repository.CommunityRepository
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.padding

class CommunityAccountScreen : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = metroViewModel<CommunityAccountViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()

        Scaffold(
            topBar = {
                AppBar(
                    title = "OracleRead Account",
                    navigateUp = navigator::pop,
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(MaterialTheme.padding.medium),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.medium),
            ) {
                if (state.signedIn) {
                    Text("You are logged in.", style = MaterialTheme.typography.titleMedium)
                    state.message?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Button(onClick = viewModel::logout) {
                        Text("Log out")
                    }
                } else {
                    AuthPanel(
                        signedIn = false,
                        working = state.working,
                        message = state.message,
                        onSignIn = viewModel::signIn,
                        onSignUp = viewModel::signUp,
                    )
                }
            }
        }
    }
}

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding = binding<ViewModel>())
class CommunityAccountViewModel(
    private val repository: CommunityRepository,
) : ViewModel() {

    val state: StateFlow<State>
        field = MutableStateFlow(State(signedIn = repository.isSignedIn))

    fun signIn(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            state.update { it.copy(working = true, message = null) }
            repository.signIn(email, password)
                .onSuccess { state.value = State(signedIn = true, message = "Logged in.") }
                .onFailure { error -> state.update { it.copy(working = false, message = friendlyAuthError(error.message, "Unable to log in")) } }
        }
    }

    fun signUp(email: String, password: String, username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            state.update { it.copy(working = true, message = null) }
            repository.signUp(email, password, username)
                .onSuccess {
                    state.value = State(
                        signedIn = repository.isSignedIn,
                        message = if (repository.isSignedIn) "Account created." else "Account created. Log in to continue.",
                    )
                }
                .onFailure { error -> state.update { it.copy(working = false, message = friendlyAuthError(error.message, "Unable to register")) } }
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearSession()
            state.value = State(signedIn = false, message = "Logged out.")
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
