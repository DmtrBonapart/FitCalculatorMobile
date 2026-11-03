package com.example.ru.yourname.fitcalculatormobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ru.yourname.fitcalculatormobile.ui.DiaryEntry
import com.example.ru.yourname.fitcalculatormobile.ui.DiaryUiState
import com.example.ru.yourname.fitcalculatormobile.ui.NutritionNorms
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    name: String,
    uiState: DiaryUiState,
    nutritionNorms: NutritionNorms?,
    onDateSelected: (String) -> Unit,
    onRetry: () -> Unit,
    onDeleteEntry: (String) -> Unit,
    onCreateProduct: () -> Unit,
    onOpenCatalog: () -> Unit,
    avatarId: String,
    onProfileClick: () -> Unit
) {
    var showDatePicker by remember {
        mutableStateOf(false)
    }

    val selectedDate = LocalDate.parse(uiState.selectedDate)

    val formattedDate = selectedDate.format(
        DateTimeFormatter.ofPattern(
            "d MMMM yyyy",
            Locale("ru")
        )
    )

    var expandedMeals by rememberSaveable(uiState.selectedDate) { mutableStateOf(emptyList<String>()) }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 20.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { UserHeader(name, avatarId, onProfileClick) }
        item { Text("Дневник питания", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item {
            Button(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) { Text(formattedDate) }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onOpenCatalog, modifier = Modifier.weight(1f)) { Text("Добавить еду") }
                OutlinedButton(onClick = onCreateProduct, modifier = Modifier.weight(1f)) { Text("Создать продукт") }
            }
        }
        uiState.productMessage?.let { message -> item { Text(message, color = MaterialTheme.colorScheme.primary) } }
        item { SummaryCard(uiState, nutritionNorms) }
        uiState.errorMessage?.let { message ->
            item {
                Text(message, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onRetry) { Text("Повторить") }
            }
        }
        if (uiState.isLoading) {
            item { Text("Загрузка…") }
        } else {
            uiState.meals.forEach { meal ->
                val expanded = meal.name in expandedMeals
                item(key = "meal_${meal.name}") {
                    Card(onClick = {
                        expandedMeals = if (expanded) expandedMeals - meal.name else expandedMeals + meal.name
                    }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(meal.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("${meal.calories} ккал · ${meal.entries.size} записей", style = MaterialTheme.typography.bodyMedium)
                                Text("Б ${formatDouble(meal.protein)} · Ж ${formatDouble(meal.fat)} · У ${formatDouble(meal.carbs)} г",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            Text(if (expanded) "Скрыть ↑" else "Открыть ↓", style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                if (expanded) {
                    if (meal.entries.isEmpty()) {
                        item(key = "empty_${meal.name}") {
                            Text("Пока ничего не добавлено", modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    items(meal.entries, key = { "entry_${it.id}" }) { entry ->
                        DiaryEntryCard(entry,
                            uiState.products.firstOrNull { it.id == entry.product_id }?.name ?: "Продукт",
                            onDelete = { onDeleteEntry(entry.id) })
                    }
                }
            }
        }
    }

    if (showDatePicker) {

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {

                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->

                            val newDate = Instant
                                .ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()

                            onDateSelected(
                                newDate.toString()
                            )
                        }

                        showDatePicker = false
                    }
                ) {
                    Text("Выбрать")
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Отмена")
                }
            }
        ) {

            DatePicker(
                state = datePickerState
            )
        }
    }
}

@Composable
private fun SummaryCard(
    uiState: DiaryUiState,
    nutritionNorms: NutritionNorms?
) {
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
                text = "Итого за день",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            if (nutritionNorms != null) {

                Text(
                    text = "${uiState.totalCalories} / ${nutritionNorms.calories} ккал",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                NutritionRow(
                    title = "Белки",
                    current = uiState.totalProtein,
                    target = nutritionNorms.protein
                )

                NutritionRow(
                    title = "Жиры",
                    current = uiState.totalFat,
                    target = nutritionNorms.fat
                )

                NutritionRow(
                    title = "Углеводы",
                    current = uiState.totalCarbs,
                    target = nutritionNorms.carbs
                )

            } else {

                Text(
                    text = "${uiState.totalCalories} ккал",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "Заполните данные профиля для расчёта нормы",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NutritionRow(
    title: String,
    current: Double,
    target: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "${formatDouble(current)} / $target г",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DiaryEntryCard(
    entry: DiaryEntry,
    productName: String,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 18.dp,
                    top = 14.dp,
                    end = 8.dp,
                    bottom = 14.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = entry.meal_type,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = productName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "${entry.quantity} г",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = "${entry.calories} ккал",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Б ${formatDouble(entry.protein)}  " +
                            "Ж ${formatDouble(entry.fat)}  " +
                            "У ${formatDouble(entry.carbs)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(
                onClick = onDelete
            ) {
                Text(
                    text = "Удалить",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun formatDouble(value: Double): String {
    return String.format(
        Locale.US,
        "%.1f",
        value
    )
}