package com.example.burgermenu.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.burgermenu.Dest
import com.example.burgermenu.data.network.BurgerApiClient
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavHostController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Iniciar Sesión",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Usuario") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    errorMessage = null
                    val result = BurgerApiClient.login(username, password)
                    isLoading = false
                    
                    if (result.isSuccess) {
                        val token = result.getOrNull()
                        if (token?.startsWith("fake_token") == true) {
                            errorMessage = "⚠️ ADVERTENCIA: Login sin autenticación real.\nNo podrás eliminar ni editar productos.\n\nVerifica que el usuario 'admin' con contraseña 'admin123' exista en la base de datos."
                            // Esperar 4 segundos para que el usuario lea el mensaje
                            kotlinx.coroutines.delay(4000)
                        }
                        // Navegar a la pantalla principal y limpiar el stack
                        navController.navigate(Dest.Products.route) {
                            popUpTo(Dest.Login.route) { inclusive = true }
                        }
                    } else {
                        val error = result.exceptionOrNull()?.message ?: "Error desconocido"
                        errorMessage = when {
                            error.contains("401") || error.contains("403") -> 
                                "Usuario o contraseña incorrectos"
                            error.contains("Connection") || error.contains("timeout") -> 
                                "Error de conexión. Verifica tu internet"
                            else -> "Error: $error"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && username.isNotBlank() && password.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Ingresar")
            }
        }
    }
}
