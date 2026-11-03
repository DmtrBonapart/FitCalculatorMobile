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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ru.yourname.fitcalculatormobile.ui.DiaryProduct

@Composable
fun CatalogScreen(
    products: List<DiaryProduct>,
    isAdding: Boolean,
    errorMessage: String?,
    onAddProduct: (DiaryProduct, String, Int) -> Unit,
    onClearError: () -> Unit,
    onCreateProduct: () -> Unit,
    productMessage: String? = null
) {
    var searchText by remember {
        mutableStateOf("")
    }

    var selectedProduct by remember {
        mutableStateOf<DiaryProduct?>(null)
    }

    val filteredProducts = products.filter {
        it.name.contains(
            searchText,
            ignoreCase = true
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(
                start = 20.dp,
                top = 20.dp,
                end = 20.dp
            )
    ) {

        Text(
            text = "Каталог продуктов",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = "Выберите продукт для добавления в дневник",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        OutlinedButton(onClick = onCreateProduct, modifier = Modifier.fillMaxWidth()) { Text("Создать продукт") }
        productMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            label = {
                Text("Поиск продукта")
            }
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        if (filteredProducts.isEmpty()) {

            Text(
                text = if (products.isEmpty()) {
                    "Продукты пока не загружены"
                } else {
                    "Ничего не найдено"
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

        } else {

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                items(filteredProducts) { product ->

                    ProductCard(
                        product = product,
                        onClick = {
                            selectedProduct = product
                        }
                    )
                }
            }
        }
    }

    selectedProduct?.let { product ->

        AddProductDialog(
            product = product,
            isAdding = isAdding,

            onDismiss = {
                if (!isAdding) {
                    selectedProduct = null
                }
            },

            onAdd = { mealType, quantity ->
                onAddProduct(
                    product,
                    mealType,
                    quantity
                )

                selectedProduct = null
            }
        )
    }

    if (errorMessage != null) {

        AlertDialog(
            onDismissRequest = onClearError,

            title = {
                Text("Ошибка")
            },

            text = {
                Text(errorMessage)
            },

            confirmButton = {
                TextButton(
                    onClick = onClearError
                ) {
                    Text("ОК")
                }
            }
        )
    }
}

@Composable
private fun ProductCard(
    product: DiaryProduct,
    onClick: () -> Unit
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
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "${product.calories} ккал / 100 г",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            Text(
                text = "Б ${product.protein} г   Ж ${product.fat} г   У ${product.carbs} г",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Добавить")
            }
        }
    }
}

@Composable
private fun AddProductDialog(
    product: DiaryProduct,
    isAdding: Boolean,
    onDismiss: () -> Unit,
    onAdd: (String, Int) -> Unit
) {
    var quantityText by remember {
        mutableStateOf("100")
    }

    var selectedMeal by remember {
        mutableStateOf("Завтрак")
    }

    var mealMenuExpanded by remember {
        mutableStateOf(false)
    }

    val quantity = quantityText.toIntOrNull() ?: 0
    val multiplier = quantity / 100.0

    val calculatedCalories =
        (product.calories * multiplier).toInt()

    val calculatedProtein =
        product.protein * multiplier

    val calculatedFat =
        product.fat * multiplier

    val calculatedCarbs =
        product.carbs * multiplier

    AlertDialog(
        onDismissRequest = {
            if (!isAdding) {
                onDismiss()
            }
        },

        title = {
            Text(
                text = product.name,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        },

        text = {

            Column {

                Text(
                    text = "${product.calories} ккал / 100 г",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = {
                        quantityText = it.filter { char ->
                            char.isDigit()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = {
                        Text("Количество, г")
                    }
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Text(
                    text = "Приём пищи",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.height(5.dp)
                )

                BoxWithDropdown(
                    selectedMeal = selectedMeal,
                    expanded = mealMenuExpanded,
                    onExpandedChange = {
                        mealMenuExpanded = it
                    },
                    onMealSelected = {
                        selectedMeal = it
                        mealMenuExpanded = false
                    }
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                Text(
                    text = "Итого:",
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(
                    modifier = Modifier.height(5.dp)
                )

                Text(
                    text = "$calculatedCalories ккал"
                )

                Text(
                    text = "Белки: ${"%.1f".format(calculatedProtein)} г"
                )

                Text(
                    text = "Жиры: ${"%.1f".format(calculatedFat)} г"
                )

                Text(
                    text = "Углеводы: ${"%.1f".format(calculatedCarbs)} г"
                )
            }
        },

        confirmButton = {

            Button(
                enabled = quantity > 0 && !isAdding,
                onClick = {
                    onAdd(
                        selectedMeal,
                        quantity
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    if (isAdding) {
                        "Добавление..."
                    } else {
                        "Добавить"
                    }
                )
            }
        },

        dismissButton = {

            TextButton(
                enabled = !isAdding,
                onClick = onDismiss
            ) {
                Text("Отмена")
            }
        }
    )
}

@Composable
private fun BoxWithDropdown(
    selectedMeal: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onMealSelected: (String) -> Unit
) {
    Column {

        OutlinedButton(
            onClick = {
                onExpandedChange(true)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedMeal,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "▼",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                onExpandedChange(false)
            }
        ) {

            listOf(
                "Завтрак",
                "Обед",
                "Ужин",
                "Перекус"
            ).forEach { meal ->

                DropdownMenuItem(
                    text = {
                        Text(meal)
                    },
                    onClick = {
                        onMealSelected(meal)
                    }
                )
            }
        }
    }
}