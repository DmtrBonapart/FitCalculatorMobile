package com.example.ru.yourname.fitcalculatormobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ru.yourname.fitcalculatormobile.ui.DiaryUiState
import com.example.ru.yourname.fitcalculatormobile.ui.ProductDraft

@Composable
fun CreateProductDialog(state: DiaryUiState, onChange: (ProductDraft) -> Unit,
    onSave: () -> Unit, onDismiss: () -> Unit, onReloadCategories: () -> Unit) {
    var categoryMenu by remember { mutableStateOf(false) }
    val draft = state.productDraft
    val enabled = !state.isCreatingProduct
    AlertDialog(onDismissRequest = { if (enabled) onDismiss() },
        title = { Text("Создать продукт") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Укажите пищевую ценность на 100 г. Продукт сохранится в общем каталоге.")
                OutlinedTextField(draft.name, { onChange(draft.copy(name = it)) }, label = { Text("Название") },
                    singleLine = true, enabled = enabled, modifier = Modifier.fillMaxWidth())
                Box {
                    OutlinedButton(onClick = { categoryMenu = true },
                        enabled = enabled && state.categories.isNotEmpty(), modifier = Modifier.fillMaxWidth()) {
                        Text(state.categories.firstOrNull { it.id == draft.categoryId }?.name ?: "Выберите категорию")
                    }
                    DropdownMenu(categoryMenu, onDismissRequest = { categoryMenu = false }) {
                        state.categories.forEach { category ->
                            DropdownMenuItem(text = { Text(category.name) }, onClick = {
                                onChange(draft.copy(categoryId = category.id)); categoryMenu = false
                            })
                        }
                    }
                }
                if (state.isLoadingCategories) LinearProgressIndicator(Modifier.fillMaxWidth())
                if (state.categories.isEmpty() && !state.isLoadingCategories) {
                    TextButton(onClick = onReloadCategories, enabled = enabled) { Text("Загрузить категории повторно") }
                }
                ProductNumberField("Калории, ккал", draft.calories, enabled, KeyboardType.Number) { onChange(draft.copy(calories = it)) }
                ProductNumberField("Белки, г", draft.protein, enabled) { onChange(draft.copy(protein = it)) }
                ProductNumberField("Жиры, г", draft.fat, enabled) { onChange(draft.copy(fat = it)) }
                ProductNumberField("Углеводы, г", draft.carbs, enabled) { onChange(draft.copy(carbs = it)) }
                state.createProductError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { TextButton(onClick = onSave, enabled = enabled && state.categories.isNotEmpty()) {
            Text(if (state.isCreatingProduct) "Сохранение…" else "Сохранить")
        } },
        dismissButton = { TextButton(onClick = onDismiss, enabled = enabled) { Text("Отмена") } }
    )
}

@Composable
private fun ProductNumberField(label: String, value: String, enabled: Boolean,
    keyboardType: KeyboardType = KeyboardType.Decimal, onChange: (String) -> Unit) {
    OutlinedTextField(value, onChange, label = { Text(label) }, singleLine = true,
        enabled = enabled, keyboardOptions = KeyboardOptions(keyboardType = keyboardType), modifier = Modifier.fillMaxWidth())
}
