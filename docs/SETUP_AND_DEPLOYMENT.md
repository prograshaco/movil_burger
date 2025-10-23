# 🚀 Configuración y Deployment - BurgerMenu

## 📋 Requisitos del Sistema

### **💻 Entorno de Desarrollo**
- **Android Studio**: Hedgehog (2023.1.1) o superior
- **JDK**: 17 o superior
- **Gradle**: 8.0 o superior
- **Kotlin**: 1.9.10 o superior
- **Android SDK**: API 34 (Android 14)
- **Min SDK**: API 24 (Android 7.0)

### **📱 Dispositivos de Prueba**
- **Emulador**: Android 7.0+ (API 24+)
- **Dispositivo físico**: Android 7.0+ con USB debugging habilitado
- **Biometría**: Dispositivo con sensor de huella dactilar (opcional)
- **Cámara**: Dispositivo con cámara trasera (opcional)

## 🔧 Configuración Inicial

### **1. Clonar el Repositorio**
```bash
git clone https://github.com/prograshaco/movil_burger.git
cd movil_burger
```

### **2. Abrir en Android Studio**
1. Abrir Android Studio
2. Seleccionar "Open an Existing Project"
3. Navegar a la carpeta del proyecto
4. Esperar a que Gradle sincronice las dependencias

### **3. Configurar SDK y Herramientas**
```bash
# Verificar instalación de SDK
android list targets

# Instalar herramientas necesarias (si no están instaladas)
sdkmanager "platforms;android-34"
sdkmanager "build-tools;34.0.0"
sdkmanager "platform-tools"
```

### **4. Configurar Emulador (Opcional)**
```bash
# Crear AVD con biometría
avdmanager create avd -n BurgerMenu_Test -k "system-images;android-34;google_apis;x86_64"

# Configurar biometría en el emulador
# Settings > Security > Fingerprint > Add fingerprint
```

## 🗄️ Configuración de Base de Datos

### **Turso Database Setup**

#### **Credenciales Actuales**
```kotlin
// En TursoClient.kt
private const val TURSO_DATABASE_URL = "https://restaurant-prograshaco.aws-us-west-2.turso.io"
private const val TURSO_AUTH_TOKEN = "eyJhbGciOiJFZERTQSIsInR5cCI6IkpXVCJ9..."
```

#### **Crear Nueva Base de Datos (Opcional)**
```bash
# Instalar Turso CLI
curl -sSfL https://get.tur.so/install.sh | bash

# Crear nueva base de datos
turso db create restaurant-app

# Obtener URL de conexión
turso db show restaurant-app

# Crear token de autenticación
turso db tokens create restaurant-app
```

#### **Estructura de Tablas**
```sql
-- Ejecutar en Turso CLI o Dashboard
turso db shell restaurant-app

-- Crear tabla de productos
CREATE TABLE products (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    price REAL NOT NULL,
    category TEXT NOT NULL,
    image_url TEXT,
    available INTEGER DEFAULT 1,
    created_at TEXT,
    updated_at TEXT
);

-- Crear tabla de usuarios
CREATE TABLE users (
    id TEXT PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    email TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    phone TEXT,
    address TEXT,
    is_active INTEGER DEFAULT 1,
    created_at TEXT,
    updated_at TEXT
);

-- Crear tabla de pedidos
CREATE TABLE orders (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    user_name TEXT NOT NULL,
    user_email TEXT NOT NULL,
    user_phone TEXT,
    user_address TEXT,
    items TEXT NOT NULL,
    total REAL NOT NULL,
    status TEXT DEFAULT 'pending',
    created_at TEXT,
    updated_at TEXT
);

-- Insertar datos de prueba
INSERT INTO products (id, name, description, price, category, image_url, available, created_at, updated_at) VALUES
('prod_1', 'Hamburguesa Clásica', 'Carne, lechuga, tomate, cebolla', 15.99, 'Hamburguesas', '', 1, '2024-01-01 12:00:00', '2024-01-01 12:00:00'),
('prod_2', 'Coca Cola', 'Bebida gaseosa 500ml', 3.50, 'Bebidas', '', 1, '2024-01-01 12:00:00', '2024-01-01 12:00:00'),
('prod_3', 'Papas Fritas', 'Papas fritas crujientes', 5.99, 'Acompañamientos', '', 1, '2024-01-01 12:00:00', '2024-01-01 12:00:00');

INSERT INTO users (id, username, email, name, phone, address, is_active, created_at, updated_at) VALUES
('user_1', 'admin', 'admin@restaurant.com', 'Administrador', '123-456-7890', 'Oficina Principal', 1, '2024-01-01 12:00:00', '2024-01-01 12:00:00'),
('user_2', 'chef', 'chef@restaurant.com', 'Chef Principal', '123-456-7891', 'Cocina', 1, '2024-01-01 12:00:00', '2024-01-01 12:00:00');
```

## 🔐 Configuración de Seguridad

### **Permisos de Android**

#### **AndroidManifest.xml**
```xml
<!-- Permisos críticos -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.USE_BIOMETRIC" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.VIBRATE" />

<!-- Características opcionales -->
<uses-feature
    android:name="android.hardware.camera"
    android:required="false" />
<uses-feature
    android:name="android.hardware.fingerprint"
    android:required="false" />
```

### **FileProvider para Cámara**

#### **res/xml/file_paths.xml**
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <cache-path name="camera_images" path="." />
</paths>
```

#### **Configuración en AndroidManifest.xml**
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

## 🏗️ Build y Compilación

### **Configuración de Build**

#### **build.gradle.kts (Module: app)**
```kotlin
android {
    namespace = "com.example.burgermenu"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.burgermenu"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
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
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}
```

### **Comandos de Build**
```bash
# Limpiar proyecto
./gradlew clean

# Compilar debug
./gradlew assembleDebug

# Compilar release
./gradlew assembleRelease

# Instalar en dispositivo conectado
./gradlew installDebug

# Ejecutar tests
./gradlew test

# Generar APK firmado
./gradlew bundleRelease
```

## 📱 Testing y Debugging

### **Configuración de Logging**

#### **Niveles de Log**
```kotlin
// En desarrollo
Log.d("TAG", "Debug message")
Log.i("TAG", "Info message")
Log.w("TAG", "Warning message")
Log.e("TAG", "Error message")

// En TursoClient
android.util.Log.d("TursoClient", "Ejecutando SQL: $sql")
android.util.Log.d("TursoClient", "Response body: $responseText")
```

#### **Filtros de Logcat**
```bash
# Filtrar por tag
adb logcat -s "TursoClient"

# Filtrar por nivel
adb logcat "*:E"

# Filtrar por aplicación
adb logcat | grep "com.example.burgermenu"
```

### **Testing en Dispositivos**

#### **Emulador con Biometría**
1. Abrir emulador
2. Settings > Security & location > Fingerprint
3. Agregar huella dactilar
4. Usar "Touch the sensor" en Extended Controls

#### **Dispositivo Físico**
```bash
# Habilitar USB Debugging
# Settings > Developer options > USB debugging

# Verificar conexión
adb devices

# Instalar APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Ver logs en tiempo real
adb logcat
```

### **Testing de Funcionalidades**

#### **Checklist de Testing**
- [ ] **Productos**: Crear, listar, editar, eliminar
- [ ] **Usuarios**: Crear, listar, editar, eliminar
- [ ] **Pedidos**: Crear, cambiar estado, filtrar
- [ ] **Cámara**: Capturar foto, permisos
- [ ] **Biometría**: Autenticación, fallback
- [ ] **Notificaciones**: Sonido, vibración, permisos
- [ ] **Base de datos**: Conexión, CRUD operations
- [ ] **UI**: Navegación, estados de carga, errores

## 🚀 Deployment

### **Preparación para Release**

#### **1. Configurar Signing**
```kotlin
// En build.gradle.kts
android {
    signingConfigs {
        create("release") {
            keyAlias = "your_key_alias"
            keyPassword = "your_key_password"
            storeFile = file("path/to/keystore.jks")
            storePassword = "your_store_password"
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

#### **2. Generar Keystore**
```bash
keytool -genkey -v -keystore burgermenu-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias burgermenu
```

#### **3. Build Release**
```bash
./gradlew bundleRelease
```

### **Google Play Store**

#### **Preparación del AAB**
```bash
# Generar Android App Bundle
./gradlew bundleRelease

# Ubicación del archivo
# app/build/outputs/bundle/release/app-release.aab
```

#### **Metadatos Requeridos**
- **Título**: BurgerMenu - Gestión de Restaurante
- **Descripción corta**: Sistema completo de gestión para restaurantes
- **Descripción completa**: Ver archivo `STORE_LISTING.md`
- **Categoría**: Business / Productivity
- **Clasificación de contenido**: Everyone
- **Capturas de pantalla**: 1080x1920 (mínimo 2, máximo 8)

### **Distribución Interna**

#### **Firebase App Distribution**
```bash
# Instalar Firebase CLI
npm install -g firebase-tools

# Login
firebase login

# Configurar proyecto
firebase init

# Subir APK
firebase appdistribution:distribute app/build/outputs/apk/debug/app-debug.apk \
    --app YOUR_APP_ID \
    --groups "testers" \
    --release-notes "Nueva versión con funcionalidades completas"
```

## 🔧 Troubleshooting

### **Problemas Comunes**

#### **Error de Compilación**
```bash
# Limpiar y reconstruir
./gradlew clean
./gradlew build

# Invalidar caché de Android Studio
# File > Invalidate Caches and Restart
```

#### **Error de Dependencias**
```bash
# Actualizar Gradle Wrapper
./gradlew wrapper --gradle-version 8.0

# Sincronizar proyecto
# Android Studio: File > Sync Project with Gradle Files
```

#### **Error de Base de Datos**
```kotlin
// Verificar conectividad
suspend fun testConnection(): Result<String> {
    return try {
        val response = TursoClient.executeQuery("SELECT 1 as test")
        if (response.isSuccess) {
            Result.success("Conexión exitosa")
        } else {
            Result.failure(Exception("Error de conexión"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

#### **Error de Permisos**
```kotlin
// Verificar permisos en runtime
if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) 
    != PackageManager.PERMISSION_GRANTED) {
    // Solicitar permiso
    ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), REQUEST_CODE)
}
```

### **Logs de Debugging**
```bash
# Ver logs específicos
adb logcat | grep -E "(TursoClient|ProductRepository|NotificationService)"

# Guardar logs en archivo
adb logcat > debug_logs.txt

# Limpiar logs
adb logcat -c
```

## 📊 Monitoreo y Analytics

### **Crashlytics (Opcional)**
```kotlin
// En build.gradle.kts
implementation("com.google.firebase:firebase-crashlytics-ktx")

// En código
FirebaseCrashlytics.getInstance().recordException(exception)
```

### **Performance Monitoring**
```kotlin
// Medir tiempo de operaciones
val trace = FirebasePerformance.getInstance().newTrace("database_operation")
trace.start()
// ... operación
trace.stop()
```

Esta documentación proporciona una guía completa para configurar, desarrollar y desplegar la aplicación BurgerMenu en diferentes entornos.