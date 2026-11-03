package com.example.ru.yourname.fitcalculatormobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ru.yourname.fitcalculatormobile.ui.DiaryUiState
import com.example.ru.yourname.fitcalculatormobile.ui.NutritionNorms
import com.example.ru.yourname.fitcalculatormobile.ui.theme.FitCalculatorMobileTheme
import java.util.Locale

@Composable
fun HomeScreen(
    name: String,
    diaryUiState: DiaryUiState,
    nutritionNorms: NutritionNorms?,
    avatarId: String = "default",
    onProfileClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Пользователь
        UserHeader(
            name = name, avatarId = avatarId, onProfileClick = onProfileClick
        )

        Text(
            text = "Сегодня",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        /*
         * Калории
         */
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
                    text = "Калории",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                if (nutritionNorms != null) {

                    Text(
                        text = "${diaryUiState.totalCalories} / ${nutritionNorms.calories} ккал",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    val remainingCalories =
                        (nutritionNorms.calories - diaryUiState.totalCalories)
                            .coerceAtLeast(0)

                    Text(
                        text = "Осталось: $remainingCalories ккал",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                } else {

                    Text(
                        text = "${diaryUiState.totalCalories} ккал",
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

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        /*
         * КБЖУ за сегодня
         */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NutritionCard(
                title = "Белки",
                value = if (nutritionNorms != null) {
                    "${formatDouble(diaryUiState.totalProtein)} / ${nutritionNorms.protein} г"
                } else {
                    "${formatDouble(diaryUiState.totalProtein)} г"
                },
                modifier = Modifier.weight(1f)
            )

            NutritionCard(
                title = "Жиры",
                value = if (nutritionNorms != null) {
                    "${formatDouble(diaryUiState.totalFat)} / ${nutritionNorms.fat} г"
                } else {
                    "${formatDouble(diaryUiState.totalFat)} г"
                },
                modifier = Modifier.weight(1f)
            )

            NutritionCard(
                title = "Углеводы",
                value = if (nutritionNorms != null) {
                    "${formatDouble(diaryUiState.totalCarbs)} / ${nutritionNorms.carbs} г"
                } else {
                    "${formatDouble(diaryUiState.totalCarbs)} г"
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        /*
         * Суточная норма
         */
        if (nutritionNorms != null) {

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
                        text = "Ваша суточная норма",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    Text(
                        text = "${nutritionNorms.calories} ккал",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "Белки: ${nutritionNorms.protein} г",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Жиры: ${nutritionNorms.fat} г",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Углеводы: ${nutritionNorms.carbs} г",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )
        }
    }
}

@Composable
private fun NutritionCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(13.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
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

@Preview(
    showBackground = true,
    showSystemUi = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
fun HomeScreenPreview() {

    FitCalculatorMobileTheme {
        HomeScreen(
            name = "Димас",
            diaryUiState = DiaryUiState(
                isLoading = false
            ),
            nutritionNorms = NutritionNorms(
                calories = 2200,
                protein = 120,
                fat = 61,
                carbs = 275
            )
        )
    }
}