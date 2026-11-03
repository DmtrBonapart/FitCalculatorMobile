package com.example.ru.yourname.fitcalculatormobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ru.yourname.fitcalculatormobile.ui.DiaryViewModel
import com.example.ru.yourname.fitcalculatormobile.ui.ProfileViewModel
import com.example.ru.yourname.fitcalculatormobile.ui.StatisticsViewModel
import com.example.ru.yourname.fitcalculatormobile.ui.screens.CreateProductDialog
import com.example.ru.yourname.fitcalculatormobile.ui.screens.CatalogScreen
import com.example.ru.yourname.fitcalculatormobile.ui.screens.DiaryScreen
import com.example.ru.yourname.fitcalculatormobile.ui.screens.HomeScreen
import com.example.ru.yourname.fitcalculatormobile.ui.screens.LoginScreen
import com.example.ru.yourname.fitcalculatormobile.ui.screens.ProfileScreen
import com.example.ru.yourname.fitcalculatormobile.ui.screens.RegisterScreen
import com.example.ru.yourname.fitcalculatormobile.ui.screens.StatisticsScreen
import com.example.ru.yourname.fitcalculatormobile.ui.theme.FitCalculatorMobileTheme

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val diaryViewModel: DiaryViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val statisticsViewModel: StatisticsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            FitCalculatorMobileTheme {
                App(
                    authViewModel = authViewModel,
                    diaryViewModel = diaryViewModel,
                    profileViewModel = profileViewModel,
                    statisticsViewModel = statisticsViewModel
                )
            }
        }
    }
}

@Composable
fun App(
    authViewModel: AuthViewModel,
    diaryViewModel: DiaryViewModel,
    profileViewModel: ProfileViewModel,
    statisticsViewModel: StatisticsViewModel
) {
    var currentScreen by remember {
        mutableStateOf("login")
    }

    var selectedTab by remember {
        mutableStateOf(0)
    }

    var currentEmail by remember {
        mutableStateOf("")
    }

    val authUiState by authViewModel.uiState.collectAsState()
    val diaryUiState by diaryViewModel.uiState.collectAsState()
    val profileUiState by profileViewModel.uiState.collectAsState()

    LaunchedEffect(authUiState.isLoggedIn) {
        if (authUiState.isLoggedIn) {

            currentScreen = "main"
            selectedTab = 0

            diaryViewModel.loadDiary()
            diaryViewModel.loadProducts()
            profileViewModel.loadProfile()

        } else if (currentScreen == "main") {

            currentScreen = "login"
            currentEmail = ""

            diaryViewModel.clearUserData()
            profileViewModel.clearUserData()
            statisticsViewModel.clearUserData()
        }
    }

    LaunchedEffect(selectedTab, authUiState.isLoggedIn) {
        if (authUiState.isLoggedIn && selectedTab == 3) {
            statisticsViewModel.loadStatistics()
        }
    }

    LaunchedEffect(authUiState.successMessage) {
        if (
            authUiState.successMessage == "Регистрация выполнена" &&
            currentScreen == "register"
        ) {
            currentScreen = "login"
            authViewModel.clearMessage()
        }
    }

    if (authUiState.isLoggedIn && diaryUiState.showCreateProduct) {
        CreateProductDialog(diaryUiState, diaryViewModel::updateProductDraft,
            diaryViewModel::createProduct, diaryViewModel::closeCreateProduct, diaryViewModel::loadCategories)
    }

    when (currentScreen) {

        "login" -> {

            LoginScreen(
                uiState = authUiState,

                onLoginClick = { email, password ->
                    currentEmail = email

                    authViewModel.login(
                        email = email,
                        password = password
                    )
                },

                onRegisterClick = {
                    authViewModel.clearMessage()
                    currentScreen = "register"
                },

                onForgotPasswordClick = { email ->
                    authViewModel.resetPassword(email)
                }
            )
        }

        "register" -> {

            RegisterScreen(
                uiState = authUiState,

                onBackClick = {
                    authViewModel.clearMessage()
                    currentScreen = "login"
                },

                onRegisterClick = {
                        name,
                        email,
                        password,
                        confirmPassword ->

                    authViewModel.register(
                        name = name,
                        email = email,
                        password = password,
                        confirmPassword = confirmPassword
                    )
                }
            )
        }

        "main" -> {

            when (selectedTab) {

                /*
                 * Главная
                 */
                0 -> {

                    MainTabScreen(
                        selectedTab = selectedTab,
                        onTabSelected = {
                            selectedTab = it
                        }
                    ) {

                        HomeScreen(
                            name = profileUiState.profile?.name
                                ?: currentEmail.substringBefore("@"),

                            diaryUiState = diaryUiState,

                            nutritionNorms = profileUiState.nutritionNorms,
                            avatarId = profileUiState.avatarId,
                            onProfileClick = { selectedTab = 4 }
                        )
                    }
                }

                /*
                 * Дневник
                 */
                1 -> {

                    MainTabScreen(
                        selectedTab = selectedTab,
                        onTabSelected = {
                            selectedTab = it
                        }
                    ) {

                        DiaryScreen(
                            name = profileUiState.profile?.name
                                ?: currentEmail.substringBefore("@"),

                            uiState = diaryUiState,

                            nutritionNorms = profileUiState.nutritionNorms,

                            onDateSelected = { date ->
                                diaryViewModel.selectDate(date)
                            },

                            onRetry = {
                                diaryViewModel.loadDiary()
                            },

                            avatarId = profileUiState.avatarId,
                            onProfileClick = { selectedTab = 4 },
                            onOpenCatalog = { selectedTab = 2 },
                            onCreateProduct = diaryViewModel::openCreateProduct,
                            onDeleteEntry = { entryId ->
                                diaryViewModel.deleteEntry(entryId)
                            }
                        )
                    }
                }

                /*
                 * Каталог
                 */
                2 -> {

                    MainTabScreen(
                        selectedTab = selectedTab,
                        onTabSelected = {
                            selectedTab = it
                        }
                    ) {

                        CatalogScreen(
                            products = diaryUiState.products,
                            isAdding = diaryUiState.isAdding,
                            errorMessage = diaryUiState.errorMessage,
                            onCreateProduct = diaryViewModel::openCreateProduct,
                            productMessage = diaryUiState.productMessage,

                            onAddProduct = {
                                    product,
                                    mealType,
                                    quantity ->

                                diaryViewModel.addEntry(
                                    product = product,
                                    mealType = mealType,
                                    quantity = quantity
                                )
                            },

                            onClearError = {
                                diaryViewModel.clearError()
                            }
                        )
                    }
                }

                /*
                 * Статистика
                 */
                3 -> {

                    MainTabScreen(
                        selectedTab = selectedTab,
                        onTabSelected = {
                            selectedTab = it
                        }
                    ) {

                        StatisticsScreen(
                            viewModel = statisticsViewModel,
                            name = profileUiState.profile?.name ?: currentEmail.substringBefore("@"),
                            avatarId = profileUiState.avatarId,
                            onProfileClick = { selectedTab = 4 }
                        )
                    }
                }

                /*
                 * Профиль
                 */
                4 -> {

                    MainTabScreen(
                        selectedTab = selectedTab,
                        onTabSelected = {
                            selectedTab = it
                        }
                    ) {

                        ProfileScreen(
                            uiState = profileUiState,
                            onAvatarSelected = profileViewModel::selectAvatar,

                            onSaveClick = {
                                    name,
                                    gender,
                                    birthDate,
                                    height,
                                    activityLevel,
                                    weight,
                                    targetWeight ->

                                profileViewModel.saveProfile(
                                    name = name,
                                    gender = gender,
                                    birthDate = birthDate,
                                    height = height,
                                    activityLevel = activityLevel,
                                    weight = weight,
                                    targetWeight = targetWeight
                                )
                            },

                            onClearMessage = {
                                profileViewModel.clearMessage()
                            },

                            onLogoutClick = {
                                authViewModel.logout()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MainTabScreen(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            content()
        }
    }
}

@Composable
private fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {

        /*
         * Главная
         */
        NavigationBarItem(
            selected = selectedTab == 0,

            onClick = {
                onTabSelected(0)
            },

            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Главная"
                )
            },

            label = {
                Text(
                    text = "Главная",
                    style = MaterialTheme.typography.labelSmall
                )
            },

            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        /*
         * Дневник
         */
        NavigationBarItem(
            selected = selectedTab == 1,

            onClick = {
                onTabSelected(1)
            },

            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = "Дневник"
                )
            },

            label = {
                Text(
                    text = "Дневник",
                    style = MaterialTheme.typography.labelSmall
                )
            },

            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        /*
         * Каталог
         */
        NavigationBarItem(
            selected = selectedTab == 2,

            onClick = {
                onTabSelected(2)
            },

            icon = {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = "Каталог"
                )
            },

            label = {
                Text(
                    text = "Каталог",
                    style = MaterialTheme.typography.labelSmall
                )
            },

            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        /*
         * Статистика
         */
        NavigationBarItem(
            selected = selectedTab == 3,

            onClick = {
                onTabSelected(3)
            },

            icon = {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = "Статистика"
                )
            },

            label = {
                Text(
                    text = "Статистика",
                    style = MaterialTheme.typography.labelSmall
                )
            },

            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        /*
         * Профиль
         */
        NavigationBarItem(
            selected = selectedTab == 4,

            onClick = {
                onTabSelected(4)
            },

            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Профиль"
                )
            },

            label = {
                Text(
                    text = "Профиль",
                    style = MaterialTheme.typography.labelSmall
                )
            },

            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}