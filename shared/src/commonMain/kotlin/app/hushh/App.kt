package app.hushh

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import hushh.shared.generated.resources.Res
import hushh.shared.generated.resources.app_logo_letter
import hushh.shared.generated.resources.nearby_searching_body
import hushh.shared.generated.resources.nearby_searching_title
import hushh.shared.generated.resources.people_empty_body
import hushh.shared.generated.resources.people_empty_title
import hushh.shared.generated.resources.tab_nearby
import hushh.shared.generated.resources.tab_people
import hushh.shared.generated.resources.tab_settings
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val Accent = Color(0xFF3ED6C0)

private val DarkColors = darkColorScheme(
    primary = Accent,
    onPrimary = Color(0xFF0E1116),
    secondaryContainer = Color(0xFF12332E),
    onSecondaryContainer = Accent,
    background = Color(0xFF0E1116),
    surface = Color(0xFF0E1116),
    surfaceContainer = Color(0xFF171B21),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF0E8F7E),
    secondaryContainer = Color(0xFFCFF3EC),
    onSecondaryContainer = Color(0xFF0E8F7E),
)

@Composable
fun App() {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors) {
        var tab by rememberSaveable { mutableStateOf(Tab.Nearby) }
        Scaffold(
            topBar = { TopBar(tab) },
            bottomBar = { BottomBar(tab) { tab = it } },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (tab) {
                    Tab.Nearby -> EmptyState(Res.string.nearby_searching_title, Res.string.nearby_searching_body)
                    Tab.People -> EmptyState(Res.string.people_empty_title, Res.string.people_empty_body)
                    Tab.Settings -> Unit
                }
            }
        }
    }
}

private val Tab.label: StringResource
    get() = when (this) {
        Tab.Nearby -> Res.string.tab_nearby
        Tab.People -> Res.string.tab_people
        Tab.Settings -> Res.string.tab_settings
    }

@Composable
private fun TopBar(tab: Tab) {
    Row(
        Modifier.statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(Modifier.size(32.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
            Box(contentAlignment = Alignment.Center) {
                Text(stringResource(Res.string.app_logo_letter), fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.width(14.dp))
        Text(stringResource(tab.label), style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun BottomBar(selected: Tab, onSelect: (Tab) -> Unit) {
    NavigationBar {
        Tab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == selected,
                onClick = { onSelect(tab) },
                icon = {
                    Icon(
                        when (tab) {
                            Tab.Nearby -> Icons.Outlined.Home
                            Tab.People -> Icons.Outlined.Person
                            Tab.Settings -> Icons.Outlined.Settings
                        },
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(tab.label)) },
            )
        }
    }
}

@Composable
private fun EmptyState(title: StringResource, body: StringResource) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(title), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.size(8.dp))
        Text(
            stringResource(body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
