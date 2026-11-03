package com.example.ru.yourname.fitcalculatormobile.ui

import kotlinx.serialization.Serializable

@Serializable
data class ProductCategory(val id: String, val name: String)

@Serializable
data class NewProduct(
    val id: String,
    val category_id: String,
    val name: String,
    val calories: Int,
    val protein: Double,
    val fat: Double,
    val carbs: Double
)

data class ProductDraft(
    val name: String = "",
    val categoryId: String = "",
    val calories: String = "",
    val protein: String = "",
    val fat: String = "",
    val carbs: String = ""
) {
    private fun number(value: String) = value.trim().replace(',', '.').toDoubleOrNull()
    fun validationError(): String? {
        if (name.trim().isEmpty()) return "Введите название продукта"
        if (name.trim().length > 100) return "Название должно быть не длиннее 100 символов"
        if (categoryId.isBlank()) return "Выберите категорию"
        val kcal = calories.trim().toIntOrNull()
        if (kcal == null || kcal !in 0..1000) return "Калории: целое число от 0 до 1000 на 100 г"
        val macros = listOf(number(protein), number(fat), number(carbs))
        if (macros.any { it == null || !it.isFinite() || it !in 0.0..100.0 })
            return "Белки, жиры и углеводы: числа от 0 до 100 г"
        if (macros.filterNotNull().sum() > 100.5) return "Сумма БЖУ не может превышать 100 г"
        return null
    }
    fun toProduct(id: String) = NewProduct(id, categoryId, name.trim(), calories.trim().toInt(),
        number(protein)!!, number(fat)!!, number(carbs)!!)
}
