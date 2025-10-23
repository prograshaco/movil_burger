# 🔧 Documentación Técnica - BurgerMenu

## 📊 Base de Datos (Turso)

### **🔗 Configuración de Conexión**

#### **TursoClient.kt** - Cliente de Base de Datos
```kotlin
object TursoClient {
    private const val TURSO_DATABASE_URL = "https://restaurant-prograshaco.aws-us-west-2.turso.io"
    private const val TURSO_AUTH_TOKEN = "eyJhbGciOiJFZERTQSIsInR5cCI6IkpXVCJ9..."
    
    // Cliente HTTP configurado con Ktor
    val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                coerceInputValues = true
                encodeDefaults = true
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
    }
}
```

### **🗄️ Estructura de Tablas**

#### **Tabla: `products`**
```sql
CREATE TABLE products (
    id TEXT PRIMARY KEY,              -- ID único del producto
    name TEXT NOT NULL,               -- Nombre del producto
    description TEXT,                 -- Descripción del producto
    price REAL NOT NULL,              -- Precio en centavos (ej: 1599 = $15.99)
    category TEXT NOT NULL,           -- Categoría (Hamburguesas, Bebidas, etc.)
    image_url TEXT,                   -- URL de la imagen del producto
    available INTEGER DEFAULT 1,      -- Disponibilidad (1=disponible, 0=no disponible)
    created_at TEXT,                  -- Fecha de creación
    updated_at TEXT                   -- Fecha de última actualización
);
```

#### **Tabla: `users`**
```sql
CREATE TABLE users (
    id TEXT PRIMARY KEY,              -- ID único del usuario
    username TEXT UNIQUE NOT NULL,    -- Nombre de usuario único
    email TEXT UNIQUE NOT NULL,       -- Email único
    name TEXT NOT NULL,               -- Nombre completo
    phone TEXT,                       -- Teléfono (opcional)
    address TEXT,                     -- Dirección (opcional)
    is_active INTEGER DEFAULT 1,      -- Estado activo (1=activo, 0=inactivo)
    created_at TEXT,                  -- Fecha de creación
    updated_at TEXT                   -- Fecha de última actualización
);
```

#### **Tabla: `orders`**
```sql
CREATE TABLE orders (
    id TEXT PRIMARY KEY,              -- ID único del pedido
    user_id TEXT NOT NULL,            -- ID del usuario que hizo el pedido
    user_name TEXT NOT NULL,          -- Nombre del cliente
    user_email TEXT NOT NULL,         -- Email del cliente
    user_phone TEXT,                  -- Teléfono del cliente
    user_address TEXT,                -- Dirección de entrega
    items TEXT NOT NULL,              -- JSON con los items del pedido
    total REAL NOT NULL,              -- Total del pedido
    status TEXT DEFAULT 'pending',    -- Estado del pedido
    created_at TEXT,                  -- Fecha de creación
    updated_at TEXT                   -- Fecha de última actualización
);
```

### **📡 Operaciones de Base de Datos**

#### **Formato de Request a Turso v2**
```kotlin
@Serializable
data class TursoRequest(
    val requests: List<TursoExecuteRequest>
)

@Serializable
data class TursoExecuteRequest(
    val type: String = "execute",
    val stmt: TursoStatement
)

@Serializable
data class TursoStatement(
    val sql: String
)
```

#### **Ejemplo de Consulta**
```kotlin
suspend fun executeQuery(sql: String): Result<TursoResponse> {
    val requestBody = TursoRequest(
        requests = listOf(
            TursoExecuteRequest(
                type = "execute",
                stmt = TursoStatement(sql = sql)
            )
        )
    )
    
    val response = httpClient.post("$TURSO_DATABASE_URL/v2/pipeline") {
        header("Authorization", "Bearer $TURSO_AUTH_TOKEN")
        header("Content-Type", "application/json")
        setBody(requestBody)
    }
    
    return if (response.status.isSuccess()) {
        val tursoResponse = response.body<TursoResponse>()
        Result.success(tursoResponse)
    } else {
        Result.failure(Exception("HTTP ${response.status}"))
    }
}
```

## 🎨 Formularios y UI

### **📝 Formulario de Productos**

#### **CreateProductScreen** - Creación de Productos
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProductScreen(navController: NavHostController, productViewModel: ProductViewModel) {
    // Estados del formulario
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var isCreating by remember { mutableStateOf(false) }
    
    // Validación y envío
    Button(
        onClick = {
            if (name.isNotBlank() && price.isNotBlank() && category.isNotBlank()) {
                val priceValue = price.toDoubleOrNull()
                if (priceValue != null && priceValue > 0) {
                    // Crear producto
                    repository.createProduct(name, description, priceValue, category)
                }
            }
        }
    ) {
        Text("Crear Producto")
    }
}
```

#### **Validaciones Implementadas**
- **Nombre**: No puede estar vacío
- **Precio**: Debe ser un número válido mayor a 0
- **Categoría**: No puede estar vacía
- **Descripción**: Opcional

#### **Funcionalidad de Cámara**
```kotlin
// Configuración de cámara
val photoFile = remember {
    File(context.cacheDir, "product_${System.currentTimeMillis()}.jpg")
}

val photoUri = remember {
    FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        photoFile
    )
}

// Launcher para captura
val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture()
) { success ->
    if (success) {
        imageUri = photoUri.toString()
    }
}

// Launcher para permisos
val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
) { isGranted ->
    if (isGranted) {
        cameraLauncher.launch(photoUri)
    }
}
```

### **👥 Formulario de Usuarios**

#### **CreateUserScreen** - Creación de Usuarios
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUserScreen(navController: NavHostController, userViewModel: UserViewModel) {
    // Estados del formulario
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    
    // Validación y envío
    Button(
        onClick = {
            if (name.isNotBlank() && email.isNotBlank() && username.isNotBlank()) {
                repository.createUser(username, email, name, phone, address)
            }
        }
    ) {
        Text("Crear Usuario")
    }
}
```

#### **Validaciones de Usuario**
- **Nombre**: Obligatorio
- **Email**: Obligatorio, debe ser único
- **Username**: Obligatorio, debe ser único
- **Teléfono**: Opcional
- **Dirección**: Opcional

### **📋 Gestión de Pedidos**

#### **OrderListScreen** - Lista de Pedidos
```kotlin
@Composable
fun OrderListScreen(viewModel: OrderViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Filtros por estado
    LazyRow {
        items(uiState.availableStatuses) { status ->
            FilterChip(
                onClick = { viewModel.filterByStatus(status) },
                label = { Text(viewModel.getStatusDisplayName(status)) },
                selected = uiState.selectedStatus == status
            )
        }
    }
    
    // Lista de pedidos
    LazyColumn {
        items(uiState.orders) { order ->
            OrderCard(
                order = order,
                onStatusChange = { newStatus ->
                    viewModel.updateOrderStatus(order.id, newStatus)
                }
            )
        }
    }
}
```

#### **Estados de Pedidos**
```kotlin
// Estados disponibles
val availableStatuses = listOf(
    "Todos", "pending", "confirmed", 
    "preparing", "ready", "delivered", "cancelled"
)

// Flujo lógico de estados
fun getNextStatus(currentStatus: String): String? {
    return when (currentStatus) {
        "pending" -> "confirmed"
        "confirmed" -> "preparing"
        "preparing" -> "ready"
        "ready" -> "delivered"
        else -> null
    }
}

// Estados disponibles para cambio
fun getAvailableStatusesForOrder(currentStatus: String): List<String> {
    return when (currentStatus) {
        "pending" -> listOf("confirmed", "cancelled")
        "confirmed" -> listOf("preparing", "cancelled")
        "preparing" -> listOf("ready", "cancelled")
        "ready" -> listOf("delivered")
        else -> emptyList()
    }
}
```

## 🔐 Autenticación Biométrica

### **Configuración de Biometría**
```kotlin
// Verificar disponibilidad
val biometricManager = BiometricManager.from(context)
when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
    BiometricManager.BIOMETRIC_SUCCESS -> {
        // Mostrar prompt biométrico
    }
    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
        // No hay sensor, continuar sin biometría
    }
    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
        // No hay huellas registradas, continuar sin biometría
    }
}
```

### **Implementación del Prompt**
```kotlin
val biometricPrompt = BiometricPrompt(activity, 
    ContextCompat.getMainExecutor(context),
    object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            // Proceder con la operación
            performUpdate()
        }
        
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            // Manejar error
            errorMessage = "Error de autenticación: $errString"
        }
        
        override fun onAuthenticationFailed() {
            // Autenticación fallida
            errorMessage = "Autenticación fallida"
        }
    }
)

val promptInfo = BiometricPrompt.PromptInfo.Builder()
    .setTitle("Confirmar actualización")
    .setSubtitle("Usa tu huella dactilar para confirmar")
    .setNegativeButtonText("Cancelar")
    .build()

biometricPrompt.authenticate(promptInfo)
```

## 🔔 Sistema de Notificaciones

### **NotificationService.kt** - Servicio de Notificaciones

#### **Configuración del Canal**
```kotlin
private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
            description = CHANNEL_DESCRIPTION
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 250, 500)
            
            // Sonido personalizado
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            setSound(soundUri, AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build())
        }
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
```

#### **Notificación de Nuevo Pedido**
```kotlin
fun showNewOrderNotification(orderId: String, customerName: String, total: Double) {
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("🍔 Nuevo Pedido!")
        .setContentText("$customerName - $${String.format("%.2f", total)}")
        .setStyle(NotificationCompat.BigTextStyle()
            .bigText("Nuevo pedido de $customerName\nTotal: $${String.format("%.2f", total)}\nID: $orderId"))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .setVibrate(longArrayOf(0, 500, 250, 500))
    
    with(NotificationManagerCompat.from(context)) {
        notify(NOTIFICATION_ID, builder.build())
    }
    
    vibratePhone() // Vibración adicional
}
```

#### **Patrón de Vibración (Estilo Uber)**
```kotlin
private fun vibratePhone() {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val vibrationEffect = VibrationEffect.createWaveform(
            longArrayOf(0, 500, 250, 500), // Patrón: esperar, vibrar, pausa, vibrar
            -1 // No repetir
        )
        vibrator.vibrate(vibrationEffect)
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(longArrayOf(0, 500, 250, 500), -1)
    }
}
```

## 🎯 ViewModels y Estado

### **ProductViewModel** - Gestión de Productos
```kotlin
data class ProductUiState(
    val products: List<Product> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "Todos",
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProductViewModel(
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()
    
    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            repository.getAllProducts()
                .onSuccess { products ->
                    _uiState.value = _uiState.value.copy(
                        products = products,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Error desconocido",
                        isLoading = false
                    )
                }
        }
    }
}
```

### **Manejo de Estados de UI**
```kotlin
// Estados de carga
if (uiState.isLoading) {
    CircularProgressIndicator()
}

// Estados de error
if (uiState.error != null) {
    Column {
        Text("Error: ${uiState.error}")
        Button(onClick = { viewModel.retry() }) {
            Text("Reintentar")
        }
    }
}

// Estados vacíos
if (uiState.products.isEmpty()) {
    Column {
        Icon(Icons.Filled.ShoppingCart, modifier = Modifier.size(64.dp))
        Text("No hay productos disponibles")
    }
}

// Contenido normal
LazyColumn {
    items(uiState.products) { product ->
        ProductCard(product = product)
    }
}
```

## 🔄 Repositorios y Operaciones CRUD

### **ProductRepository** - Operaciones de Productos

#### **Crear Producto**
```kotlin
suspend fun createProduct(name: String, description: String, price: Double, category: String): Result<Boolean> {
    return withContext(Dispatchers.IO) {
        try {
            val productId = "prod_${java.util.UUID.randomUUID()}"
            val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            
            // Escapar caracteres especiales
            val safeName = name.replace("'", "''").replace("\n", " ").replace("\r", " ")
            val safeDescription = description.replace("'", "''").replace("\n", " ").replace("\r", " ")
            val safeCategory = category.replace("'", "''").replace("\n", " ").replace("\r", " ")
            
            val sql = """
                INSERT INTO products (id, name, description, price, category, image_url, available, created_at, updated_at) 
                VALUES ('$productId', '$safeName', '$safeDescription', $price, '$safeCategory', '', 1, '$currentTime', '$currentTime')
            """.trimIndent()
            
            val response = TursoClient.executeQuery(sql)
            
            if (response.isSuccess) {
                Result.success(true)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Error al crear producto"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

#### **Obtener Productos**
```kotlin
suspend fun getAllProducts(): Result<List<Product>> {
    return withContext(Dispatchers.IO) {
        try {
            val response = TursoClient.executeQuery("SELECT * FROM products ORDER BY created_at DESC")
            
            if (response.isSuccess) {
                val tursoResponse = response.getOrThrow()
                val (columns, rows) = TursoClient.extractDataFromV2Response(tursoResponse)
                
                val products = TursoClient.rowsToMaps(columns, rows).map { row ->
                    Product(
                        id = row["id"] ?: "",
                        name = row["name"] ?: "",
                        description = row["description"] ?: "",
                        price = (row["price"]?.toDoubleOrNull()?.times(100))?.toInt() ?: 0,
                        category = row["category"] ?: "",
                        image_url = row["image_url"] ?: "",
                        available = row["available"]?.toIntOrNull() ?: 1,
                        created_at = row["created_at"] ?: ""
                    )
                }
                Result.success(products)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Error al obtener productos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## 🎨 Componentes de UI Reutilizables

### **ProductCard** - Tarjeta de Producto
```kotlin
@Composable
fun ProductCard(product: Product, navController: NavHostController) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp)) {
            // Imagen placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Imagen del producto")
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información del producto
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, style = MaterialTheme.typography.titleMedium)
                Text(text = product.category, style = MaterialTheme.typography.bodySmall)
                Text(text = "$${String.format("%.2f", product.price / 100.0)}")
            }
            
            // Botones de acción
            Column {
                IconButton(onClick = { navController.navigate("edit_product/${product.id}") }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = { /* Eliminar */ }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}
```

### **Manejo de Errores Globales**
```kotlin
// En ViewModels
.onFailure { exception ->
    _uiState.value = _uiState.value.copy(
        error = when (exception) {
            is java.net.UnknownHostException -> "Sin conexión a internet"
            is java.net.SocketTimeoutException -> "Tiempo de espera agotado"
            else -> exception.message ?: "Error desconocido"
        },
        isLoading = false
    )
}

// En UI
if (uiState.error != null) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Text(
            text = "Error: ${uiState.error}",
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}
```

Esta documentación técnica cubre todos los aspectos importantes del código, desde la configuración de base de datos hasta la implementación de componentes de UI, proporcionando una guía completa para entender y mantener el proyecto.