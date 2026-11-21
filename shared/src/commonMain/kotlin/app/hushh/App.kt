package app.hushh

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.hushh.mesh.Identity
import app.hushh.mesh.MeshCore
import app.hushh.mesh.randomNickname
import com.ionspin.kotlin.crypto.LibsodiumInitializer
import hushh.shared.generated.resources.Res
import hushh.shared.generated.resources.app_logo_letter
import hushh.shared.generated.resources.nearby_count
import hushh.shared.generated.resources.tab_nearby
import hushh.shared.generated.resources.tab_people
import hushh.shared.generated.resources.tab_settings
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val Accent = Color(0xFF3ED6C0)

private val DarkColors = darkColorScheme(
    primary = Accent,
    onPrimary = Color(0xFF0E1116),
    primaryContainer = Color(0xFF1F4D44),
    onPrimaryContainer = Color(0xFFE6F7F3),
    secondaryContainer = Color(0xFF12332E),
    onSecondaryContainer = Accent,
    background = Color(0xFF0E1116),
    surface = Color(0xFF0E1116),
    surfaceContainer = Color(0xFF171B21),
    surfaceContainerHigh = Color(0xFF1E232B),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF0E8F7E),
    primaryContainer = Color(0xFFCFF3EC),
    secondaryContainer = Color(0xFFCFF3EC),
    onSecondaryContainer = Color(0xFF0E8F7E),
)

private var meshCore: MeshCore? = null

/**
 * The one Mesh core for this app process, with a fresh Identity and Nickname.
 * Nothing is stored yet (that comes with encrypted storage), and the Bluetooth
 * adapters that move its bytes come in later tickets.
 */
private suspend fun meshCore(): MeshCore = meshCore ?: run {
    LibsodiumInitializer.initialize()
    MeshCore(Identity.generate(), randomNickname(), send = { _, _ -> }).also { meshCore = it }
}

@Composable
fun App() {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors) {
        val core = produceState<MeshCore?>(null) { value = meshCore() }.value
        val state = core?.state?.collectAsState()?.value
        var tab by rememberSaveable { mutableStateOf(Tab.Nearby) }
        Scaffold(
            topBar = { TopBar(tab, state?.peers?.size ?: 0) },
            bottomBar = { BottomBar(tab) { tab = it } },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).imePadding()) {
                if (core == null || state == null) return@Box
                when (tab) {
                    Tab.Nearby -> NearbyScreen(state.nearby, onSend = core::postToNearby)
                    Tab.People -> PeopleScreen(state.peers)
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
private fun TopBar(tab: Tab, peerCount: Int) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(Modifier.size(32.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
            Box(contentAlignment = Alignment.Center) {
                Text(stringResource(Res.string.app_logo_letter), fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.width(14.dp))
        Text(stringResource(tab.label), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        if (peerCount > 0) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(Res.string.nearby_count, peerCount),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
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
internal fun EmptyState(title: StringResource, body: StringResource, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(32.dp),
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
