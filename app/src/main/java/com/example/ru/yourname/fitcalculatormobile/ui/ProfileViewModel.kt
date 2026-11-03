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
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.coroutines.CancellationException
import java.time.LocalDate
import java.time.Period

@Serializable
data class UserProfile(
    val id: String,
    val email: String,
    val name: String? = null,
    val gender: String? = null,
    val birth_date: String? = null,
    val height: Int? = null,
    val activity_level: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Serializable
private data class ProfileUpdate(
    val name: String,
    val gender: String?,
    val birth_date: String?,
    val height: Int?,
    val activity_level: String?
)

@Serializable
data class WeightRecord(
    val id: String,
    val user_id: String,
    val weight: Double,
    val record_date: String,
    val created_at: String? = null
)

@Serializable
private data class WeightRecordInsert(
    val user_id: String,
    val weight: Double,
    val record_date: String
)

@Serializable
private data class WeightRecordUpdate(
    val weight: Double
)

@Serializable
data class UserGoal(
    val id: String,
    val user_id: String,
    val goal_type: String? = null,
    val target_weight: Double? = null,
    val created_at: String? = null
)

@Serializable
private data class GoalInsert(
    val user_id: String,
    val goal_type: String,
    val target_weight: Double
)

@Serializable
private data class GoalUpdate(
    val target_weight: Double
)

data class ProfileUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val avatarId: String = ProfileAvatar.DEFAULT,
    val isSavingAvatar: Boolean = false,
    val avatarError: String? = null,
    val profile: UserProfile? = null,
    val currentWeight: Double? = null,
    val targetWeight: Double? = null,
    val nutritionNorms: NutritionNorms? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())

    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile() {

        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {

                val client = SupabaseClientProvider.client

                val user = client
                    .auth
                    .currentUserOrNull()

                if (user == null) {

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Пользователь не авторизован"
                    )

                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    avatarId = ProfileAvatar.normalize(user.userMetadata?.get("avatar_id")?.jsonPrimitive?.contentOrNull)
                )

                /*
                 * Профиль
                 */
                val profiles = client
                    .from("users")
                    .select {
                        filter {
                            eq("id", user.id)
                        }
                    }
                    .decodeList<UserProfile>()

                val profile = profiles.firstOrNull()

                if (profile == null) {

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Профиль пользователя не найден"
                    )

                    return@launch
                }

                /*
                 * Вес
                 */
                val weightRecords = client
                    .from("weight_records")
                    .select {
                        filter {
                            eq("user_id", user.id)
                        }
                    }
                    .decodeList<WeightRecord>()

                val currentWeight = weightRecords
                    .maxByOrNull {
                        it.record_date
                    }
                    ?.weight

                /*
                 * Цель
                 */
                val goals = client
                    .from("goals")
                    .select {
                        filter {
                            eq("user_id", user.id)
                        }
                    }
                    .decodeList<UserGoal>()

                val targetWeight = goals
                    .firstOrNull()
                    ?.target_weight

                /*
                 * Расчёт нормы КБЖУ
                 */
                val nutritionNorms = calculateNutritionNorms(
                    profile = profile,
                    currentWeight = currentWeight
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    profile = profile,
                    currentWeight = currentWeight,
                    targetWeight = targetWeight,
                    nutritionNorms = nutritionNorms,
                    errorMessage = null
                )

            } catch (e: Exception) {

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                        ?: "Не удалось загрузить данные профиля"
                )
            }
        }
    }

    fun selectAvatar(avatarId: String) {
        if (avatarId !in ProfileAvatar.ids || _uiState.value.isSavingAvatar) return
        val auth = SupabaseClientProvider.client.auth
        val user = auth.currentUserOrNull() ?: return
        _uiState.value = _uiState.value.copy(isSavingAvatar = true, avatarError = null)
        viewModelScope.launch {
            try {
                auth.updateUser {
                    data {
                        user.userMetadata?.forEach { (key, value) -> put(key, value) }
                        put("avatar_id", avatarId)
                    }
                }
                if (auth.currentUserOrNull()?.id == user.id) {
                    _uiState.value = _uiState.value.copy(avatarId = avatarId, isSavingAvatar = false)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (auth.currentUserOrNull()?.id == user.id) {
                    _uiState.value = _uiState.value.copy(isSavingAvatar = false,
                        avatarError = "Не удалось сохранить аватарку. Проверьте подключение и повторите.")
                }
            }
        }
    }

    private fun calculateNutritionNorms(
        profile: UserProfile,
        currentWeight: Double?
    ): NutritionNorms? {

        val gender = profile.gender ?: return null
        val birthDate = profile.birth_date ?: return null
        val height = profile.height ?: return null
        val weight = currentWeight ?: return null
        val activityLevel = profile.activity_level ?: return null

        val age = try {

            val dateOfBirth = LocalDate.parse(birthDate)

            Period.between(
                dateOfBirth,
                LocalDate.now()
            ).years

        } catch (e: Exception) {

            return null
        }

        if (age <= 0 || height <= 0 || weight <= 0) {
            return null
        }

        return NutritionCalculator.calculate(
            gender = gender,
            age = age,
            height = height,
            weight = weight,
            activityLevel = activityLevel
        )
    }

    fun saveProfile(
        name: String,
        gender: String,
        birthDate: String,
        height: String,
        activityLevel: String,
        weight: String,
        targetWeight: String
    ) {

        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(
                isSaving = true,
                errorMessage = null,
                successMessage = null
            )

            try {

                val client = SupabaseClientProvider.client

                val user = client
                    .auth
                    .currentUserOrNull()

                if (user == null) {

                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = "Пользователь не авторизован"
                    )

                    return@launch
                }

                /*
                 * Рост
                 */
                val heightValue = if (height.isBlank()) {
                    null
                } else {
                    height.toIntOrNull()
                }

                if (height.isNotBlank() && heightValue == null) {

                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = "Рост должен быть числом"
                    )

                    return@launch
                }

                /*
                 * Текущий вес
                 */
                val weightValue = if (weight.isBlank()) {
                    null
                } else {
                    weight
                        .replace(',', '.')
                        .toDoubleOrNull()
                }

                if (weight.isNotBlank() && weightValue == null) {

                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = "Текущий вес должен быть числом"
                    )

                    return@launch
                }

                /*
                 * Желаемый вес
                 */
                val targetWeightValue = if (targetWeight.isBlank()) {
                    null
                } else {
                    targetWeight
                        .replace(',', '.')
                        .toDoubleOrNull()
                }

                if (targetWeight.isNotBlank() && targetWeightValue == null) {

                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = "Желаемый вес должен быть числом"
                    )

                    return@launch
                }

                /*
                 * Обновляем users
                 */
                val profileUpdate = ProfileUpdate(
                    name = name,
                    gender = gender.ifBlank { null },
                    birth_date = birthDate.ifBlank { null },
                    height = heightValue,
                    activity_level = activityLevel.ifBlank { null }
                )

                client
                    .from("users")
                    .update(profileUpdate) {

                        filter {
                            eq("id", user.id)
                        }
                    }

                /*
                 * Сохраняем текущий вес
                 */
                val today = LocalDate.now().toString()

                if (weightValue != null) {

                    val todayRecords = client
                        .from("weight_records")
                        .select {

                            filter {
                                eq("user_id", user.id)
                                eq("record_date", today)
                            }
                        }
                        .decodeList<WeightRecord>()

                    val todayRecord = todayRecords.firstOrNull()

                    if (todayRecord != null) {

                        client
                            .from("weight_records")
                            .update(
                                WeightRecordUpdate(
                                    weight = weightValue
                                )
                            ) {

                                filter {
                                    eq("id", todayRecord.id)
                                }
                            }

                    } else {

                        client
                            .from("weight_records")
                            .insert(
                                WeightRecordInsert(
                                    user_id = user.id,
                                    weight = weightValue,
                                    record_date = today
                                )
                            )
                    }
                }

                /*
                 * Сохраняем цель
                 */
                if (targetWeightValue != null) {

                    val goals = client
                        .from("goals")
                        .select {

                            filter {
                                eq("user_id", user.id)
                            }
                        }
                        .decodeList<UserGoal>()

                    val existingGoal = goals.firstOrNull()

                    if (existingGoal != null) {

                        client
                            .from("goals")
                            .update(
                                GoalUpdate(
                                    target_weight = targetWeightValue
                                )
                            ) {

                                filter {
                                    eq("id", existingGoal.id)
                                }
                            }

                    } else {

                        client
                            .from("goals")
                            .insert(
                                GoalInsert(
                                    user_id = user.id,
                                    goal_type = "weight",
                                    target_weight = targetWeightValue
                                )
                            )
                    }
                }

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    currentWeight = weightValue
                        ?: _uiState.value.currentWeight,
                    targetWeight = targetWeightValue
                        ?: _uiState.value.targetWeight,
                    successMessage = "Данные сохранены"
                )

                loadProfile()

            } catch (e: Exception) {

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message
                        ?: "Не удалось сохранить данные"
                )
            }
        }
    }

    fun clearMessage() {

        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    fun clearUserData() {

        _uiState.value = ProfileUiState(
            isLoading = false
        )
    }
}