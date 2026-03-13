package com.akmeczo.votersystem.ui

import android.util.Patterns.EMAIL_ADDRESS
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.akmeczo.votersystem.server.Api
import com.akmeczo.votersystem.server.Server
import com.akmeczo.votersystem.server.requests.UserLoginRequest
import kotlinx.coroutines.launch

@PreviewScreenSizes
@Composable
fun LoginScreen(server: Server = Server("", "")) {
    var email by remember { mutableStateOf("example@gmail.com") }
    var password by remember { mutableStateOf("test_Str0ng_password") }
    val isValidEmail = EMAIL_ADDRESS.matcher(email).matches()
    val scope = rememberCoroutineScope()

    Column {
        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 100.dp)
        )

        Spacer(modifier = Modifier.height(100.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it.trim() },
            isError = email.isNotEmpty() && !isValidEmail,
            placeholder = { Text("Email") },
            supportingText = {
                if (email.isNotEmpty() && !isValidEmail) Text("Enter a valid email address")
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            val request = UserLoginRequest(email, password)

            scope.launch {
                val response = Api.Users.login(server, request)
                println("Got $response")
            }
        }) {
            Text("Login")
        }
    }
}