package com.example.ru.yourname.fitcalculatormobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ru.yourname.fitcalculatormobile.AuthUiState
import com.example.ru.yourname.fitcalculatormobile.R
import com.example.ru.yourname.fitcalculatormobile.ui.theme.AppWhite
import com.example.ru.yourname.fitcalculatormobile.ui.theme.Dimensions
import com.example.ru.yourname.fitcalculatormobile.ui.theme.ErrorRed
import com.example.ru.yourname.fitcalculatormobile.ui.theme.FitCalculatorMobileTheme
import com.example.ru.yourname.fitcalculatormobile.ui.theme.PrimaryGreen
import com.example.ru.yourname.fitcalculatormobile.ui.theme.TextDark
import com.example.ru.yourname.fitcalculatormobile.ui.theme.TextGray

@Composable
fun LoginScreen(
    uiState: AuthUiState,
    onLoginClick: (String, String) -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: (String) -> Unit
) {
    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppWhite)
            .padding(Dimensions.ScreenPadding)
    ) {
        Text(
            text = "‹",
            style = MaterialTheme.typography.titleLarge,
            color = TextDark,
            modifier = Modifier.padding(
                bottom = 18.dp
            )
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(
                    id = R.drawable.fitcalculator_round
                ),
                contentDescription = "FitCalculator",
                modifier = Modifier.size(Dimensions.LogoSize)
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            Text(
                text = "Вход в аккаунт",
                style = MaterialTheme.typography.titleLarge,
                color = TextDark
            )

            Spacer(
                modifier = Modifier.height(Dimensions.SpaceSmall)
            )

            Text(
                text = "Введите свои данные",
                style = MaterialTheme.typography.bodySmall,
                color = TextGray,
                textAlign = TextAlign.Center
            )
        }

        Spacer(
            modifier = Modifier.height(22.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Email")
            },
            singleLine = true,
            shape = RoundedCornerShape(Dimensions.CornerSmall),
            enabled = !uiState.isLoading
        )

        Spacer(
            modifier = Modifier.height(11.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Пароль")
            },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(Dimensions.CornerSmall),
            enabled = !uiState.isLoading
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = ErrorRed,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )
        }

        uiState.successMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = PrimaryGreen,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )
        }

        Button(
            onClick = {
                onLoginClick(email, password)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimensions.ButtonHeight),
            shape = RoundedCornerShape(Dimensions.CornerSmall),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryGreen
            ),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = AppWhite,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Войти",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        TextButton(
            onClick = {
                onForgotPasswordClick(email)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Text(
                text = "Забыли пароль?",
                style = MaterialTheme.typography.bodySmall,
                color = PrimaryGreen
            )
        }

        OutlinedButton(
            onClick = onRegisterClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimensions.ButtonHeight),
            shape = RoundedCornerShape(Dimensions.CornerSmall),
            enabled = !uiState.isLoading
        ) {
            Text(
                text = "Зарегистрироваться",
                style = MaterialTheme.typography.labelLarge,
                color = TextDark
            )
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
fun LoginScreenPreview() {
    FitCalculatorMobileTheme {
        LoginScreen(
            uiState = AuthUiState(),
            onLoginClick = { _, _ -> },
            onRegisterClick = {},
            onForgotPasswordClick = {}
        )
    }
}