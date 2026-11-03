package com.example.ru.yourname.fitcalculatormobile.ui

data class NutritionNorms(
    val calories: Int,
    val protein: Int,
    val fat: Int,
    val carbs: Int
)

object NutritionCalculator {

    fun calculate(
        gender: String,
        age: Int,
        height: Int,
        weight: Double,
        activityLevel: String
    ): NutritionNorms {

        // Базовый обмен веществ по формуле Mifflin-St Jeor
        val bmr = if (gender == "Мужской") {
            10 * weight + 6.25 * height - 5 * age + 5
        } else {
            10 * weight + 6.25 * height - 5 * age - 161
        }

        // Коэффициент физической активности
        val activityCoefficient = when (activityLevel) {
            "Минимальная" -> 1.2
            "Низкая" -> 1.375
            "Средняя" -> 1.55
            "Высокая" -> 1.725
            "Очень высокая" -> 1.9
            else -> 1.2
        }

        val calories = bmr * activityCoefficient

        // Белок: 1.6 г на кг веса
        val protein = weight * 1.6

        // Жиры: 25% от суточной калорийности
        val fat = (calories * 0.25) / 9.0

        // Оставшиеся калории идут на углеводы
        val carbs = (calories - protein * 4 - fat * 9) / 4.0

        return NutritionNorms(
            calories = calories.toInt(),
            protein = protein.toInt(),
            fat = fat.toInt(),
            carbs = carbs.toInt()
        )
    }
}