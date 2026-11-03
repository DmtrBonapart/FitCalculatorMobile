package com.example.ru.yourname.fitcalculatormobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ru.yourname.fitcalculatormobile.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import java.util.UUID
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class DiaryProduct(
    val id: String,
    val name: String,
    val calories: Int,
    val protein: Double,
    val fat: Double,
    val carbs: Double
)

@Serializable
data class DiaryEntry(
    val id: String,
    val user_id: String,
    val product_id: String,
    val meal_type: String,
    val quantity: Int,
    val calories: Int,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val diary_date: String,
    val created_at: String? = null
)

@Serializable
private data class NewDiaryEntry(
    val user_id: String,
    val product_id: String,
    val meal_type: String,
    val quantity: Int,
    val calories: Int,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val diary_date: String
)

data class DiaryUiState(
    val isLoading: Boolean = true,
    val isAdding: Boolean = false,
    val isCreatingProduct: Boolean = false,
    val showCreateProduct: Boolean = false,
    val productDraft: ProductDraft = ProductDraft(),
    val createProductError: String? = null,
    val productMessage: String? = null,
    val categories: List<ProductCategory> = emptyList(),
    val isLoadingCategories: Boolean = false,
    val entries: List<DiaryEntry> = emptyList(),
    val products: List<DiaryProduct> = emptyList(),
    val errorMessage: String? = null,
    val selectedDate: String = LocalDate.now().toString()
) {
    val meals: List<MealSummary> get() = summarizeMeals(entries)

    val totalCalories: Int
        get() = entries.sumOf { it.calories }

    val totalProtein: Double
        get() = entries.sumOf { it.protein }

    val totalFat: Double
        get() = entries.sumOf { it.fat }

    val totalCarbs: Double
        get() = entries.sumOf { it.carbs }
}

class DiaryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    private var draftId = UUID.randomUUID().toString()

    fun openCreateProduct() {
        _uiState.value = _uiState.value.copy(showCreateProduct = true, createProductError = null, productMessage = null)
        loadCategories()
    }

    fun closeCreateProduct() {
        if (!_uiState.value.isCreatingProduct) {
            _uiState.value = _uiState.value.copy(showCreateProduct = false)
        }
    }

    fun updateProductDraft(draft: ProductDraft) {
        if (!_uiState.value.isCreatingProduct) {
            _uiState.value = _uiState.value.copy(productDraft = draft, createProductError = null)
        }
    }

    fun loadCategories() {
        if (_uiState.value.isLoadingCategories) return
        _uiState.value = _uiState.value.copy(isLoadingCategories = true, createProductError = null)
        val userId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id
        viewModelScope.launch {
            try {
                val categories = SupabaseClientProvider.client.from("categories")
                    .select { order("name", Order.ASCENDING) }.decodeList<ProductCategory>()
                if (SupabaseClientProvider.client.auth.currentUserOrNull()?.id == userId) {
                    _uiState.value = _uiState.value.copy(categories = categories, isLoadingCategories = false,
                        createProductError = if (categories.isEmpty()) "В базе пока нет категорий продуктов" else null)
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                if (SupabaseClientProvider.client.auth.currentUserOrNull()?.id == userId) {
                    _uiState.value = _uiState.value.copy(isLoadingCategories = false,
                        createProductError = "Не удалось загрузить категории. Проверьте подключение и повторите.")
                }
            }
        }
    }

    fun createProduct() {
        if (_uiState.value.isCreatingProduct) return
        val draft = _uiState.value.productDraft
        val error = draft.validationError()
        if (error != null) {
            _uiState.value = _uiState.value.copy(createProductError = error)
            return
        }
        if (_uiState.value.categories.none { it.id == draft.categoryId }) {
            _uiState.value = _uiState.value.copy(createProductError = "Выберите доступную категорию")
            return
        }
        val client = SupabaseClientProvider.client
        val userId = client.auth.currentUserOrNull()?.id
        if (userId == null) {
            _uiState.value = _uiState.value.copy(createProductError = "Войдите в аккаунт для создания продукта")
            return
        }
        val product = draft.toProduct(draftId)
        _uiState.value = _uiState.value.copy(isCreatingProduct = true, createProductError = null)
        viewModelScope.launch {
            try {
                // Reuse the draft UUID on retry so an interrupted response cannot create duplicates.
                client.from("products").upsert(product) { onConflict = "id" }
                val saved = DiaryProduct(product.id, product.name, product.calories, product.protein, product.fat, product.carbs)
                if (client.auth.currentUserOrNull()?.id == userId) {
                    _uiState.value = _uiState.value.copy(isCreatingProduct = false, showCreateProduct = false,
                        productDraft = ProductDraft(), createProductError = null,
                        products = (_uiState.value.products.filterNot { it.id == saved.id } + saved).sortedBy { it.name },
                        productMessage = "Продукт «${saved.name}» сохранён в каталоге")
                    draftId = UUID.randomUUID().toString()
                    loadProducts()
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                if (client.auth.currentUserOrNull()?.id == userId) {
                    _uiState.value = _uiState.value.copy(isCreatingProduct = false,
                        createProductError = "Не удалось сохранить продукт. Проверьте подключение и повторите.")
                }
            }
        }
    }

    fun loadDiary() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val user = SupabaseClientProvider.client
                    .auth
                    .currentUserOrNull()

                if (user == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Пользователь не авторизован"
                    )
                    return@launch
                }

                val selectedDate = _uiState.value.selectedDate

                val entries = SupabaseClientProvider.client
                    .from("food_diary")
                    .select {
                        filter {
                            eq("user_id", user.id)
                            eq("diary_date", selectedDate)
                        }

                        order(
                            column = "created_at",
                            order = Order.ASCENDING
                        )
                    }
                    .decodeList<DiaryEntry>()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    entries = entries,
                    errorMessage = null
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                        ?: "Не удалось загрузить дневник"
                )
            }
        }
    }

    fun selectDate(date: String) {
        if (date == _uiState.value.selectedDate) {
            return
        }

        _uiState.value = _uiState.value.copy(
            selectedDate = date
        )

        loadDiary()
    }

    fun loadProducts() {
        viewModelScope.launch {
            try {
                val products = SupabaseClientProvider.client
                    .from("products")
                    .select {
                        order(
                            column = "name",
                            order = Order.ASCENDING
                        )
                    }
                    .decodeList<DiaryProduct>()

                _uiState.value = _uiState.value.copy(
                    products = products
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message
                        ?: "Не удалось загрузить продукты"
                )
            }
        }
    }

    fun addEntry(
        product: DiaryProduct,
        mealType: String,
        quantity: Int
    ) {
        if (quantity <= 0) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Количество должно быть больше 0"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAdding = true,
                errorMessage = null
            )

            try {
                val user = SupabaseClientProvider.client
                    .auth
                    .currentUserOrNull()

                if (user == null) {
                    _uiState.value = _uiState.value.copy(
                        isAdding = false,
                        errorMessage = "Пользователь не авторизован"
                    )
                    return@launch
                }

                val multiplier = quantity / 100.0
                val selectedDate = _uiState.value.selectedDate

                val newEntry = NewDiaryEntry(
                    user_id = user.id,
                    product_id = product.id,
                    meal_type = mealType,
                    quantity = quantity,
                    calories = (product.calories * multiplier).toInt(),
                    protein = product.protein * multiplier,
                    fat = product.fat * multiplier,
                    carbs = product.carbs * multiplier,
                    diary_date = selectedDate
                )

                SupabaseClientProvider.client
                    .from("food_diary")
                    .insert(newEntry)

                _uiState.value = _uiState.value.copy(
                    isAdding = false
                )

                loadDiary()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAdding = false,
                    errorMessage = e.message
                        ?: "Не удалось добавить продукт"
                )
            }
        }
    }

    fun deleteEntry(entryId: String) {
        viewModelScope.launch {
            try {
                val user = SupabaseClientProvider.client
                    .auth
                    .currentUserOrNull()

                if (user == null) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Пользователь не авторизован"
                    )
                    return@launch
                }

                SupabaseClientProvider.client
                    .from("food_diary")
                    .delete {
                        filter {
                            eq("id", entryId)
                            eq("user_id", user.id)
                        }
                    }

                loadDiary()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message
                        ?: "Не удалось удалить запись"
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
        draftId = UUID.randomUUID().toString()
        _uiState.value = DiaryUiState(
            isLoading = false
        )
    }
}
