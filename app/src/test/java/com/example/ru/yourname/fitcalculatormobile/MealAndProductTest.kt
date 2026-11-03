package com.example.ru.yourname.fitcalculatormobile

import com.example.ru.yourname.fitcalculatormobile.ui.*
import org.junit.Assert.*
import org.junit.Test

class MealAndProductTest {
    private fun entry(id: String, meal: String, calories: Int, protein: Double = 0.0) =
        DiaryEntry(id, "user", "product", meal, 100, calories, protein, 0.0, 0.0, "2026-11-03")

    @Test fun emptyDayStillShowsFourMeals() {
        val meals = summarizeMeals(emptyList())
        assertEquals(listOf("Завтрак", "Обед", "Ужин", "Перекус"), meals.map { it.name })
        assertTrue(meals.all { it.calories == 0 && it.entries.isEmpty() })
    }

    @Test fun mealsSumEveryEntryExactlyOnceIncludingLegacyNames() {
        val entries = listOf(entry("1", "Завтрак", 100, 2.5), entry("2", "breakfast", 200, 3.2),
            entry("3", "Перекусы", 50), entry("4", "Поздний ужин", 70))
        val meals = summarizeMeals(entries)
        assertEquals(300, meals.first().calories)
        assertEquals(5.7, meals.first().protein, 0.0001)
        assertEquals(420, meals.sumOf { it.calories })
        assertEquals(entries.map { it.id }.sorted(), meals.flatMap { it.entries }.map { it.id }.sorted())
    }

    private fun validDraft() = ProductDraft(" Йогурт ", "category", "61", "3,47", "3.25", "4.66")

    @Test fun acceptsCommaAndTrimsName() {
        val draft = validDraft()
        assertNull(draft.validationError())
        val product = draft.toProduct("new-id")
        assertEquals("Йогурт", product.name)
        assertEquals(3.47, product.protein, 0.0001)
        assertEquals("category", product.category_id)
    }

    @Test fun rejectsInvalidNutritionWithoutThrowing() {
        listOf("NaN", "Infinity", "-1", "101", "", "abc").forEach {
            assertNotNull(validDraft().copy(protein = it).validationError())
        }
        assertNotNull(validDraft().copy(calories = "999999999999").validationError())
        assertNotNull(validDraft().copy(protein = "60", fat = "60").validationError())
    }

    @Test fun requiresNameAndCategoryButAllowsZeroCalories() {
        assertNotNull(validDraft().copy(name = "  ").validationError())
        assertNotNull(validDraft().copy(categoryId = "").validationError())
        assertNull(ProductDraft("Вода", "category", "0", "0", "0", "0").validationError())
    }

    @Test fun avatarIdsRemainStableAndUnknownValuesUseSilhouette() {
        assertEquals(5, ProfileAvatar.ids.distinct().size)
        ProfileAvatar.ids.forEach { assertEquals(it, ProfileAvatar.normalize(it)) }
        assertEquals("default", ProfileAvatar.normalize("missing"))
        assertEquals("default", ProfileAvatar.normalize(null))
    }
}
