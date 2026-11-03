package com.example.ru.yourname.fitcalculatormobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ru.yourname.fitcalculatormobile.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class StatisticsEntry(
    val id: String,
    val user_id: String,
    val calories: Int,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val diary_date: String
)

data class DayStatistics(
    val date: String,
    val calories: Int
)

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val todayCalories: Int = 0,
    val todayProtein: Double = 0.0,
    val todayFat: Double = 0.0,
    val todayCarbs: Double = 0.0,
    val weekData: List<DayStatistics> = emptyList(),
    val errorMessage: String? = null
)

class StatisticsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    fun loadStatistics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val user = SupabaseClientProvider.client.auth.currentUserOrNull()

                if (user == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Пользователь не авторизован"
                    )
                    return@launch
                }

                val today = LocalDate.now()
                val weekStart = today.minusDays(6)

                val entries = SupabaseClientProvider.client
                    .from("food_diary")
                    .select {
                        filter {
                            eq("user_id", user.id)
                            gte("diary_date", weekStart.toString())
                            lte("diary_date", today.toString())
                        }
                    }
                    .decodeList<StatisticsEntry>()

                val todayEntries = entries.filter {
                    it.diary_date == today.toString()
                }

                val todayCalories = todayEntries.sumOf { it.calories }

                val todayProtein = todayEntries.sumOf {
                    it.protein
                }

                val todayFat = todayEntries.sumOf {
                    it.fat
                }

                val todayCarbs = todayEntries.sumOf {
                    it.carbs
                }

                val weekData = (0..6).map { offset ->
                    val date = weekStart.plusDays(offset.toLong())
                    val dateString = date.toString()

                    DayStatistics(
                        date = dateString,
                        calories = entries
                            .filter { it.diary_date == dateString }
                            .sumOf { it.calories }
                    )
                }

                _uiState.value = StatisticsUiState(
                    isLoading = false,
                    todayCalories = todayCalories,
                    todayProtein = todayProtein,
                    todayFat = todayFat,
                    todayCarbs = todayCarbs,
                    weekData = weekData,
                    errorMessage = null
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                        ?: "Не удалось загрузить статистику"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null
        )
    }

    fun clearUserData() {
        _uiState.value = StatisticsUiState(isLoading = false)
    }
}
