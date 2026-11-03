package com.example.ru.yourname.fitcalculatormobile.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ru.yourname.fitcalculatormobile.R
import com.example.ru.yourname.fitcalculatormobile.ui.ProfileUiState
import com.example.ru.yourname.fitcalculatormobile.ui.theme.FitCalculatorMobileTheme
import java.util.Calendar
import java.util.Locale

private data class GenderOption(
    val value: String,
    val label: String
)

private data class ActivityOption(
    val value: String,
    val label: String
)

private val genderOptions = listOf(
    GenderOption(
        value = "male",
        label = "Мужской"
    ),
    GenderOption(
        value = "female",
        label = "Женский"
    )
)

private val activityOptions = listOf(
    ActivityOption(
        value = "minimal",
        label = "Минимальная"
    ),
    ActivityOption(
        value = "low",
        label = "Низкая"
    ),
    ActivityOption(
        value = "medium",
        label = "Средняя"
    ),
    ActivityOption(
        value = "high",
        label = "Высокая"
    ),
    ActivityOption(
        value = "very_high",
        label = "Очень высокая"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onSaveClick: (
        name: String,
        gender: String,
        birthDate: String,
        height: String,
        activityLevel: String,
        weight: String,
        targetWeight: String
    ) -> Unit,
    onLogoutClick: () -> Unit,
    onClearMessage: () -> Unit,
    onAvatarSelected: (String) -> Unit = {}
) {
    var name by remember {
        mutableStateOf("")
    }

    var gender by remember {
        mutableStateOf("")
    }

    var birthDate by remember {
        mutableStateOf("")
    }

    var height by remember {
        mutableStateOf("")
    }

    var activityLevel by remember {
        mutableStateOf("")
    }

    var weight by remember {
        mutableStateOf("")
    }

    var targetWeight by remember {
        mutableStateOf("")
    }

    var genderExpanded by remember {
        mutableStateOf(false)
    }

    var activityExpanded by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    LaunchedEffect(
        uiState.profile,
        uiState.currentWeight,
        uiState.targetWeight
    ) {
        uiState.profile?.let { profile ->

            name = profile.name ?: ""

            gender = when (profile.gender) {
                "male", "Мужской" -> "male"
                "female", "Женский" -> "female"
                else -> profile.gender ?: ""
            }

            birthDate = profile.birth_date ?: ""

            height = profile.height?.toString() ?: ""

            activityLevel = when (profile.activity_level) {
                "minimal", "Минимальная" -> "minimal"
                "low", "Низкая" -> "low"
                "medium", "Средняя" -> "medium"
                "high", "Высокая" -> "high"
                "very_high", "Очень высокая" -> "very_high"
                else -> profile.activity_level ?: ""
            }
        }

        weight = uiState.currentWeight?.toString() ?: ""
        targetWeight = uiState.targetWeight?.toString() ?: ""
    }

    val selectedGenderLabel = genderOptions
        .firstOrNull { it.value == gender }
        ?.label
        ?: ""

    val selectedActivityLabel = activityOptions
        .firstOrNull { it.value == activityLevel }
        ?.label
        ?: ""

    fun showBirthDatePicker() {

        val calendar = Calendar.getInstance()

        if (
            birthDate.matches(
                Regex("\\d{4}-\\d{2}-\\d{2}")
            )
        ) {
            val parts = birthDate.split("-")

            calendar.set(
                parts[0].toInt(),
                parts[1].toInt() - 1,
                parts[2].toInt()
            )
        }

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->

                birthDate = String.format(
                    Locale.US,
                    "%04d-%02d-%02d",
                    year,
                    month + 1,
                    dayOfMonth
                )
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        Text(
            text = "Профиль",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        if (uiState.isLoading) {

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }

        } else {

            // Аватар аккаунта
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                ProfileAvatarPicker(uiState, onAvatarSelected)
            }

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            // Аккаунт
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {

                Column(
                    modifier = Modifier.padding(18.dp)
                ) {

                    Text(
                        text = "Аккаунт",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = uiState.profile?.email ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            // Личные данные
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {

                Column(
                    modifier = Modifier.padding(18.dp)
                ) {

                    Text(
                        text = "Личные данные",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    // Имя
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Имя")
                        },
                        singleLine = true
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    // Пол
                    ExposedDropdownMenuBox(
                        expanded = genderExpanded,
                        onExpandedChange = {
                            genderExpanded = !genderExpanded
                        }
                    ) {

                        OutlinedTextField(
                            value = selectedGenderLabel,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            label = {
                                Text("Пол")
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = genderExpanded
                                )
                            },
                            singleLine = true
                        )

                        ExposedDropdownMenu(
                            expanded = genderExpanded,
                            onDismissRequest = {
                                genderExpanded = false
                            }
                        ) {

                            genderOptions.forEach { option ->

                                DropdownMenuItem(
                                    text = {
                                        Text(option.label)
                                    },
                                    onClick = {
                                        gender = option.value
                                        genderExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    // Дата рождения
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        OutlinedTextField(
                            value = formatBirthDateForDisplay(birthDate),
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text("Дата рождения")
                            },
                            placeholder = {
                                Text("ДД.ММ.ГГГГ")
                            },
                            singleLine = true
                        )

                        // Прозрачная область поверх поля.
                        // Она принимает нажатие и открывает календарь.
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clickable {
                                    showBirthDatePicker()
                                }
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    // Рост
                    OutlinedTextField(
                        value = height,
                        onValueChange = {
                            height = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Рост, см")
                        },
                        singleLine = true
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    // Текущий вес
                    OutlinedTextField(
                        value = weight,
                        onValueChange = {
                            weight = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Текущий вес, кг")
                        },
                        singleLine = true
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    // Желаемый вес
                    OutlinedTextField(
                        value = targetWeight,
                        onValueChange = {
                            targetWeight = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Желаемый вес, кг")
                        },
                        singleLine = true
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    // Уровень активности
                    ExposedDropdownMenuBox(
                        expanded = activityExpanded,
                        onExpandedChange = {
                            activityExpanded = !activityExpanded
                        }
                    ) {

                        OutlinedTextField(
                            value = selectedActivityLabel,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            label = {
                                Text("Уровень активности")
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = activityExpanded
                                )
                            },
                            singleLine = true
                        )

                        ExposedDropdownMenu(
                            expanded = activityExpanded,
                            onDismissRequest = {
                                activityExpanded = false
                            }
                        ) {

                            activityOptions.forEach { option ->

                                DropdownMenuItem(
                                    text = {
                                        Text(option.label)
                                    },
                                    onClick = {
                                        activityLevel = option.value
                                        activityExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    // Сохранить
                    Button(
                        onClick = {

                            onSaveClick(
                                name,
                                gender,
                                birthDate,
                                height,
                                activityLevel,
                                weight,
                                targetWeight
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !uiState.isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {

                        Text(
                            text = if (uiState.isSaving) {
                                "Сохранение..."
                            } else {
                                "Сохранить изменения"
                            }
                        )
                    }

                    // Ошибка
                    if (uiState.errorMessage != null) {

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = uiState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Успешное сохранение
                    if (uiState.successMessage != null) {

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = uiState.successMessage,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            // Выход
            TextButton(
                onClick = {
                    onLogoutClick()
                },
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = "Выйти из аккаунта",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun formatBirthDateForDisplay(
    birthDate: String
): String {

    if (birthDate.isBlank()) {
        return ""
    }

    val parts = birthDate.split("-")

    if (parts.size != 3) {
        return birthDate
    }

    return "${parts[2]}.${parts[1]}.${parts[0]}"
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
fun ProfileScreenPreview() {

    FitCalculatorMobileTheme {

        ProfileScreen(
            uiState = ProfileUiState(
                isLoading = false,
                currentWeight = 75.5,
                targetWeight = 70.0
            ),
            onSaveClick = { _, _, _, _, _, _, _ -> },
            onLogoutClick = {},
            onClearMessage = {}
        )
    }
}