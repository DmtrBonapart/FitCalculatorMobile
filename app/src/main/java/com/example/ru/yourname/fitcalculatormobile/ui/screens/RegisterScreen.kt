package com.example.ru.yourname.fitcalculatormobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ru.yourname.fitcalculatormobile.AuthUiState
import com.example.ru.yourname.fitcalculatormobile.R
import com.example.ru.yourname.fitcalculatormobile.ui.theme.FitCalculatorMobileTheme

private val PrimaryGreen = Color(0xFF4CAF50)
private val TextDark = Color(0xFF333333)
private val TextGray = Color(0xFF666666)
private val ErrorRed = Color(0xFFD32F2F)

@Composable
fun RegisterScreen(
    uiState: AuthUiState,
    onBackClick: () -> Unit,
    onRegisterClick: (String, String, String, String) -> Unit
) {
    var name by remember {
        mutableStateOf("")
    }

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var confirmPassword by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp)
    ) {
        Text(
            text = "‹",
            fontSize = 32.sp,
            color = TextDark,
            modifier = Modifier
                .padding(bottom = 18.dp)
                .clickable(
                    enabled = !uiState.isLoading
                ) {
                    onBackClick()
                }
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.fitcalculator_round),
                contentDescription = "FitCalculator",
                modifier = Modifier.size(90.dp)
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            Text(
                text = "Создать аккаунт",
                color = TextDark,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = "Заполните форму для регистрации",
                color = TextGray,
                fontSize = 13.sp
            )
        }

        Spacer(
            modifier = Modifier.height(22.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Ваше имя")
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            enabled = !uiState.isLoading
        )

        Spacer(
            modifier = Modifier.height(11.dp)
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
            shape = RoundedCornerShape(10.dp),
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
            shape = RoundedCornerShape(10.dp),
            enabled = !uiState.isLoading
        )

        Spacer(
            modifier = Modifier.height(11.dp)
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Подтвердите пароль")
            },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            enabled = !uiState.isLoading
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                color = ErrorRed,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )
        }

        uiState.successMessage?.let { message ->
            Text(
                text = message,
                color = PrimaryGreen,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )
        }

        Button(
            onClick = {
                onRegisterClick(
                    name,
                    email,
                    password,
                    confirmPassword
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryGreen
            ),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Зарегистрироваться",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
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
fun RegisterScreenPreview() {
    FitCalculatorMobileTheme {
        RegisterScreen(
            uiState = AuthUiState(),
            onBackClick = {},
            onRegisterClick = { _, _, _, _ -> }
        )
    }
}