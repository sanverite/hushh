package app.hushh

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.hushh.mesh.Peer
import hushh.shared.generated.resources.Res
import hushh.shared.generated.resources.people_empty_body
import hushh.shared.generated.resources.people_empty_title
import hushh.shared.generated.resources.people_section_nearby
import hushh.shared.generated.resources.people_status_nearby
import org.jetbrains.compose.resources.stringResource

@Composable
fun PeopleScreen(peers: List<Peer>) {
    if (peers.isEmpty()) {
        EmptyState(Res.string.people_empty_title, Res.string.people_empty_body)
        return
    }
    LazyColumn {
        item {
            Text(
                stringResource(Res.string.people_section_nearby, peers.size).uppercase(),
                Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(peers, key = { it.id.hex }) { PeerRow(it) }
    }
}

@Composable
private fun PeerRow(peer: Peer) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(Modifier.size(44.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Box(contentAlignment = Alignment.Center) {
                Text(peer.nickname.take(1).uppercase(), style = MaterialTheme.typography.titleMedium)
            }
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(peer.nickname, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(Res.string.people_status_nearby),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
