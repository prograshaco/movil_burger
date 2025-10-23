# 📁 Estructura del Proyecto BurgerMenu

## 🏗️ Arquitectura General

El proyecto sigue una **arquitectura MVVM (Model-View-ViewModel)** con **Clean Architecture** para Android, utilizando **Jetpack Compose** para la UI.

```
app/
├── src/main/
│   ├── java/com/example/burgermenu/
│   │   ├── data/                    # Capa de datos
│   │   │   ├── models/              # Modelos de datos
│   │   │   ├── network/             # Cliente de red (Turso)
│   │   │   └── repository/          # Repositorios (lógica de datos)
│   │   ├── ui/viewmodel/            # ViewModels (lógica de UI)
│   │   ├── services/                # Servicios (notificaciones)
│   │   └── MainActivity.kt          # Actividad principal y UI
│   ├── res/                         # Recursos de Android
│   └── AndroidManifest.xml          # Configuración de la app
├── build.gradle.kts                 # Configuración de dependencias
└── docs/                           # Documentación del proyecto
```

## 📦 Estructura Detallada

### **🎯 Capa de Presentación (UI)**
```
MainActivity.kt                      # Actividad principal con todas las pantallas
├── BurgerMenuApp()                 # Composable principal con navegación
├── ProductListScreen()             # Pantalla de lista de productos
├── CreateProductScreen()           # Formulario de creación de productos
├── EditProductScreen()             # Formulario de edición (con biometría)
├── UserListScreen()                # Pantalla de lista de usuarios
├── CreateUserScreen()              # Formulario de creación de usuarios
├── EditUserScreen()                # Formulario de edición de usuarios
├── OrderListScreen()               # Pantalla de gestión de pedidos
├── ProductCard()                   # Componente de tarjeta de producto
├── UserCard()                      # Componente de tarjeta de usuario
└── OrderCard()                     # Componente de tarjeta de pedido
```

### **🧠 Capa de Lógica (ViewModels)**
```
ui/viewmodel/
├── ProductViewModel.kt             # Lógica de productos
├── UserViewModel.kt                # Lógica de usuarios
└── OrderViewModel.kt               # Lógica de pedidos
```

### **💾 Capa de Datos**
```
data/
├── models/                         # Modelos de datos
│   ├── Product.kt                  # Modelo de producto
│   ├── User.kt                     # Modelo de usuario
│   └── Order.kt                    # Modelo de pedido
├── network/
│   └── TursoClient.kt              # Cliente para base de datos Turso
└── repository/                     # Repositorios (acceso a datos)
    ├── ProductRepository.kt        # Operaciones CRUD de productos
    ├── UserRepository.kt           # Operaciones CRUD de usuarios
    └── OrderRepository.kt          # Operaciones CRUD de pedidos
```

### **🔔 Servicios**
```
services/
└── NotificationService.kt          # Servicio de notificaciones push
```

## 🔧 Configuración del Proyecto

### **📱 Configuración de Android**
- **Target SDK**: 34
- **Min SDK**: 24
- **Compile SDK**: 34
- **Lenguaje**: Kotlin
- **UI Framework**: Jetpack Compose

### **📦 Dependencias Principales**

#### **Core Android**
```kotlin
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
implementation("androidx.activity:activity-compose:1.8.2")
```

#### **Jetpack Compose**
```kotlin
implementation(platform("androidx.compose:compose-bom:2023.10.01"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.ui:ui-graphics")
implementation("androidx.compose.ui:ui-tooling-preview")
implementation("androidx.compose.material3:material3")
```

#### **Navegación**
```kotlin
implementation("androidx.navigation:navigation-compose:2.7.5")
implementation("androidx.compose.runtime:runtime-saveable")
```

#### **Networking (Turso Database)**
```kotlin
implementation("io.ktor:ktor-client-core:2.3.7")
implementation("io.ktor:ktor-client-android:2.3.7")
implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
implementation("io.ktor:ktor-client-logging:2.3.7")
implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
```

#### **ViewModels y Corrutinas**
```kotlin
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

#### **Cámara y Permisos**
```kotlin
implementation("androidx.camera:camera-camera2:1.3.1")
implementation("androidx.camera:camera-lifecycle:1.3.1")
implementation("androidx.camera:camera-view:1.3.1")
implementation("com.google.accompanist:accompanist-permissions:0.32.0")
```

#### **Biometría**
```kotlin
implementation("androidx.biometric:biometric:1.1.0")
```

#### **Notificaciones y Trabajo en Background**
```kotlin
implementation("androidx.work:work-runtime-ktx:2.9.0")
```

#### **Carga de Imágenes**
```kotlin
implementation("io.coil-kt:coil-compose:2.5.0")
```

## 🔐 Permisos Requeridos

### **AndroidManifest.xml**
```xml
<!-- Conectividad -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- Cámara -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

<!-- Biometría -->
<uses-permission android:name="android.permission.USE_FINGERPRINT" />
<uses-permission android:name="android.permission.USE_BIOMETRIC" />

<!-- Notificaciones -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />

<!-- Hardware de cámara (opcional) -->
<uses-feature
    android:name="android.hardware.camera"
    android:required="false" />
```

## 🗂️ Organización de Archivos

### **📁 Carpetas Principales**

#### **`data/models/`** - Modelos de Datos
- Contiene las clases de datos (data classes) que representan las entidades del sistema
- Cada modelo corresponde a una tabla de la base de datos

#### **`data/network/`** - Cliente de Red
- `TursoClient.kt`: Maneja todas las comunicaciones con la base de datos Turso
- Incluye serialización/deserialización JSON
- Manejo de errores de red

#### **`data/repository/`** - Repositorios
- Implementan el patrón Repository
- Abstraen el acceso a datos de los ViewModels
- Manejan la lógica de negocio relacionada con datos

#### **`ui/viewmodel/`** - ViewModels
- Contienen la lógica de presentación
- Manejan el estado de la UI
- Comunican con los repositorios

#### **`services/`** - Servicios
- Servicios de sistema como notificaciones
- Lógica que no pertenece directamente a la UI

## 🎨 Patrones de Diseño Utilizados

### **1. MVVM (Model-View-ViewModel)**
- **Model**: `data/models/` y `data/repository/`
- **View**: Composables en `MainActivity.kt`
- **ViewModel**: `ui/viewmodel/`

### **2. Repository Pattern**
- Abstrae el acceso a datos
- Permite cambiar la fuente de datos sin afectar la UI
- Centraliza la lógica de datos

### **3. Singleton Pattern**
- `TursoClient` es un object singleton
- `NotificationService` se instancia una vez por contexto

### **4. Observer Pattern**
- ViewModels exponen `StateFlow` para observar cambios
- UI se recompone automáticamente cuando cambia el estado

## 🔄 Flujo de Datos

```
UI (Composables) 
    ↕️
ViewModel (StateFlow)
    ↕️
Repository (suspend functions)
    ↕️
TursoClient (HTTP requests)
    ↕️
Turso Database (Cloud)
```

## 🚀 Puntos de Entrada

### **MainActivity.kt**
- Punto de entrada principal de la aplicación
- Contiene toda la UI usando Jetpack Compose
- Maneja la navegación entre pantallas

### **BurgerMenuApp()**
- Composable raíz que configura la navegación
- Define las rutas y el bottom navigation
- Maneja el FAB (Floating Action Button)

## 📱 Navegación

### **Rutas Definidas**
```kotlin
sealed class Dest(val route: String, val label: String, val icon: ImageVector) {
    object Products : Dest("products", "Productos", Icons.AutoMirrored.Filled.List)
    object Users : Dest("users", "Usuarios", Icons.Filled.Person)
    object Orders : Dest("orders", "Pedidos", Icons.Filled.ShoppingCart)
    object CreateProduct : Dest("create_product", "Crear Producto", Icons.Filled.Add)
    object EditProduct : Dest("edit_product", "Editar Producto", Icons.Filled.Edit)
    object CreateUser : Dest("create_user", "Crear Usuario", Icons.Filled.Add)
    object EditUser : Dest("edit_user", "Editar Usuario", Icons.Filled.Edit)
}
```

### **Navegación con Parámetros**
- Edición de productos: `edit_product/{productId}`
- Edición de usuarios: `edit_user/{userId}`

## 🔧 Configuración de Desarrollo

### **Build Configuration**
```kotlin
android {
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.example.burgermenu"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = "1.8"
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}
```

Esta estructura permite un desarrollo escalable, mantenible y siguiendo las mejores prácticas de Android moderno.