package com.example.aiddproject.core.locale

import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Wraps content in overrides for both [LocalConfiguration] AND [LocalContext] so
 * `stringResource()` calls within [content] resolve against the user's selected
 * [Language] without an Activity recreation.
 *
 * Both overrides are required: `stringResource()` reads from
 * `LocalContext.current.resources`, so providing only a configuration override would
 * silently fail. With the dual override, switching language re-renders all localizable
 * text within a single recomposition and preserves Compose state — satisfying SC-004.
 *
 * The provided context MUST stay a [ContextWrapper] over the hosting Activity —
 * `hiltViewModel()` walks `ContextWrapper.baseContext` looking for the Activity to find
 * its `ViewModelStoreOwner`. A bare `createConfigurationContext()` result is a
 * `ContextImpl` and breaks that lookup with `IllegalStateException: Expected an
 * activity context for creating a HiltViewModelFactory`. We therefore wrap the
 * Activity context and only swap `getResources()`.
 */
@Composable
fun LanguageProvider(
    viewModel: LocaleViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val language by viewModel.language.collectAsState()
    LanguageProvider(language = language, content = content)
}

/**
 * Stateless variant. Tests inject a fixed [Language]; production callers use the
 * [HiltViewModel]-backed overload above.
 */
@Composable
fun LanguageProvider(
    language: Language,
    content: @Composable () -> Unit,
) {
    val baseConfig = LocalConfiguration.current
    val baseContext = LocalContext.current

    val overlay =
        remember(baseConfig, language) {
            Configuration(baseConfig).apply { setLocale(language.toLocale()) }
        }
    val localizedContext =
        remember(baseContext, overlay) {
            val localizedResources =
                baseContext.createConfigurationContext(overlay).resources
            object : ContextWrapper(baseContext) {
                override fun getResources(): Resources = localizedResources
            }
        }

    CompositionLocalProvider(
        LocalConfiguration provides overlay,
        LocalContext provides localizedContext,
    ) {
        content()
    }
}

@HiltViewModel
class LocaleViewModel
    @Inject
    constructor(
        private val repository: LanguagePreferenceRepository,
    ) : ViewModel() {
        val language: StateFlow<Language> =
            repository.language
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.Eagerly,
                    initialValue = Language.Default,
                )

        /**
         * Persists the user's choice. The [language] flow re-emits the new value once
         * DataStore commits, which causes [LanguageProvider] to rebuild the locale
         * overlay and `stringResource()` calls to read the matching translation in the
         * same recomposition (SC-004).
         */
        fun setLanguage(language: Language) {
            viewModelScope.launch {
                repository.set(language)
            }
        }
    }
