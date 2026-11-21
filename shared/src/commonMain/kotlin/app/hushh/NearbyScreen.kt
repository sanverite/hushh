package app.hushh

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.hushh.mesh.MeshCore
import app.hushh.mesh.NearbyMessage
import hushh.shared.generated.resources.Res
import hushh.shared.generated.resources.nearby_composer_hint
import hushh.shared.generated.resources.nearby_searching_body
import hushh.shared.generated.resources.nearby_searching_title
import hushh.shared.generated.resources.nearby_send
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

@Composable
fun NearbyScreen(messages: List<NearbyMessage>, onSend: (String) -> Unit) {
    Column {
        if (messages.isEmpty()) {
            EmptyState(Res.string.nearby_searching_title, Res.string.nearby_searching_body, Modifier.weight(1f))
        } else {
            val listState = rememberLazyListState()
            LaunchedEffect(messages.size) { listState.animateScrollToItem(messages.lastIndex) }
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(messages) { MessageBubble(it) }
            }
        }
        Composer(onSend)
    }
}

@Composable
private fun MessageBubble(message: NearbyMessage) {
    Box(Modifier.fillMaxWidth(), contentAlignment = if (message.isMine) Alignment.CenterEnd else Alignment.CenterStart) {
        Surface(
            Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(16.dp),
            color = if (message.isMine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (!message.isMine) {
                    Text(
                        message.nickname,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(message.text, style = MaterialTheme.typography.bodyLarge)
                Text(
                    timeOf(message.timestamp),
                    Modifier.align(Alignment.End),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun Composer(onSend: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    val send = {
        onSend(text.trim())
        text = ""
    }
    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        TextField(
            value = text,
            onValueChange = { if (it.length <= MeshCore.MAX_MESSAGE_LENGTH) text = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(Res.string.nearby_composer_hint)) },
            shape = RoundedCornerShape(24.dp),
            maxLines = 4,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        )
        Spacer(Modifier.size(8.dp))
        FilledIconButton(onClick = send, enabled = text.isNotBlank(), modifier = Modifier.size(52.dp), shape = CircleShape) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(Res.string.nearby_send))
        }
    }
}

private fun timeOf(epochMillis: Long): String {
    val time = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())
    return "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
}
