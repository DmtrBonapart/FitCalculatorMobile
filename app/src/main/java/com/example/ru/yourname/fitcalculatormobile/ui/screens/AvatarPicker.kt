package com.example.ru.yourname.fitcalculatormobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.ru.yourname.fitcalculatormobile.R
import com.example.ru.yourname.fitcalculatormobile.ui.ProfileAvatar
import com.example.ru.yourname.fitcalculatormobile.ui.ProfileUiState

private data class AvatarOption(val id: String, val label: String, val image: Int)
private val avatarOptions = listOf(
    AvatarOption(ProfileAvatar.DEFAULT, "Силуэт", R.drawable.avatar_default),
    AvatarOption("boy_brunette", "Парень, брюнет", R.drawable.avatar_boy_brunette),
    AvatarOption("boy_blond", "Парень, блондин", R.drawable.avatar_boy_blond),
    AvatarOption("girl_brunette", "Девушка, брюнетка", R.drawable.avatar_girl_brunette),
    AvatarOption("girl_blond", "Девушка, блондинка", R.drawable.avatar_girl_blond)
)

@Composable
fun ProfileAvatarImage(avatarId: String, modifier: Modifier = Modifier, description: String? = null) {
    val option = avatarOptions.firstOrNull { it.id == avatarId } ?: avatarOptions.first()
    Image(painterResource(option.image), description,
        modifier = modifier.clip(CircleShape), contentScale = ContentScale.Fit)
}

@Composable
fun ProfileAvatarPicker(uiState: ProfileUiState, onSelect: (String) -> Unit) {
    var open by rememberSaveable { mutableStateOf(false) }
    var pending by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(uiState.avatarId, uiState.isSavingAvatar) {
        if (pending == uiState.avatarId && !uiState.isSavingAvatar) {
            open = false
            pending = null
        }
    }
    ProfileAvatarImage(uiState.avatarId, Modifier.size(92.dp), "Аватар профиля")
    TextButton(onClick = { open = true }, enabled = !uiState.isSavingAvatar) {
        Text(if (uiState.isSavingAvatar) "Сохранение…" else "Выбрать аватарку")
    }
    if (open) {
        AlertDialog(
            onDismissRequest = { if (!uiState.isSavingAvatar) open = false },
            title = { Text("Выберите аватарку") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()).selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    avatarOptions.forEach { option ->
                        val selected = uiState.avatarId == option.id
                        Row(Modifier.fillMaxWidth().selectable(
                            selected = selected,
                            enabled = !uiState.isSavingAvatar,
                            role = Role.RadioButton,
                            onClick = {
                                if (selected) open = false
                                else { pending = option.id; onSelect(option.id) }
                            }).padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            ProfileAvatarImage(option.id, Modifier.size(56.dp).then(
                                if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                else Modifier))
                            Spacer(Modifier.width(36.dp))
                            RadioButton(selected = selected, onClick = null)
                        }
                    }
                    if (uiState.isSavingAvatar) LinearProgressIndicator(Modifier.fillMaxWidth())
                    uiState.avatarError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(onClick = { open = false }, enabled = !uiState.isSavingAvatar) { Text("Закрыть") }
            }
        )
    }
}
