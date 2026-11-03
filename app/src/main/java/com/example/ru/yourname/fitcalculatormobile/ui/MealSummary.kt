package com.example.ru.yourname.fitcalculatormobile.ui

data class MealSummary(val name: String, val entries: List<DiaryEntry>) {
    val calories get() = entries.sumOf { it.calories }
    val protein get() = entries.sumOf { it.protein }
    val fat get() = entries.sumOf { it.fat }
    val carbs get() = entries.sumOf { it.carbs }
}

fun summarizeMeals(entries: List<DiaryEntry>): List<MealSummary> {
    val names = listOf("Завтрак", "Обед", "Ужин", "Перекус")
    val grouped = entries.groupBy { entry ->
        when (entry.meal_type.trim().lowercase()) {
            "breakfast", "завтрак" -> "Завтрак"
            "lunch", "обед" -> "Обед"
            "dinner", "ужин" -> "Ужин"
            "snack", "перекус", "перекусы" -> "Перекус"
            else -> entry.meal_type.ifBlank { "Другой приём пищи" }
        }
    }
    return (names + grouped.keys.filter { it !in names }).map { MealSummary(it, grouped[it].orEmpty()) }
}
