package com.example.burgermenu

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.foundation.clickable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.burgermenu.ui.viewmodel.ProductViewModel
import com.example.burgermenu.ui.viewmodel.UserViewModel
import com.example.burgermenu.ui.viewmodel.OrderViewModel
import com.example.burgermenu.data.models.Product
import com.example.burgermenu.data.models.User
import com.example.burgermenu.data.models.Order
import com.example.burgermenu.services.NotificationService

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { 
            MaterialTheme {
                BurgerMenuApp() 
            }
        }
    }
}

@Composable
fun BurgerMenuApp() {
    val nav = rememberNavController()
    val tabs = listOf(Dest.Products, Dest.Users, Dest.Orders)
    val currentRoute = nav.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                    tabs.forEach { dest ->
                        NavigationBarItem(
                            selected = currentRoute == dest.route,
                            onClick = {
                                nav.navigate(dest.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            when (currentRoute) {
                Dest.Products.route -> {
                    FloatingActionButton(
                        onClick = { nav.navigate(Dest.CreateProduct.route) }
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Crear Producto")
                    }
                }
                Dest.Users.route -> {
                    FloatingActionButton(
                        onClick = { nav.navigate(Dest.CreateUser.route) }
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Crear Usuario")
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Dest.Products.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Dest.Products.route) { 
                val viewModel: ProductViewModel = viewModel()
                LaunchedEffect(Unit) { viewModel.refreshProducts() }
                ProductListScreen(viewModel = viewModel, navController = nav) 
            }
            composable(Dest.Users.route) { 
                val viewModel: UserViewModel = viewModel()
                com.example.burgermenu.ui.UserListScreen(viewModel = viewModel, navController = nav) 
            }
            composable(Dest.Orders.route) { 
                val viewModel: OrderViewModel = viewModel()
                OrderListScreen(viewModel = viewModel) 
            }
            composable(Dest.CreateProduct.route) {
                val viewModel: ProductViewModel = viewModel()
                CreateProductScreen(navController = nav, productViewModel = viewModel)
            }
            composable("${Dest.EditProduct.route}/{productId}") { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                val viewModel: ProductViewModel = viewModel()
                EditProductScreen(productId = productId, navController = nav, productViewModel = viewModel)
            }
            composable(Dest.CreateUser.route) {
                val viewModel: UserViewModel = viewModel()
                CreateUserScreen(navController = nav, userViewModel = viewModel)
            }
            composable("${Dest.EditUser.route}/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                val viewModel: UserViewModel = viewModel()
                EditUserScreen(userId = userId, navController = nav, userViewModel = viewModel)
            }
        }
    }
}

sealed class Dest(val route: String, val label: String, val icon: ImageVector) {
    data object Login : Dest("login", "Login", Icons.Filled.Person)
    data object Products : Dest("products", "Productos", Icons.AutoMirrored.Filled.List)
    data object Users    : Dest("users", "Usuarios", Icons.Filled.Person)
    data object Orders   : Dest("orders", "Pedidos", Icons.Filled.ShoppingCart)
    data object CreateProduct : Dest("create_product", "Crear Producto", Icons.Filled.Add)
    data object EditProduct : Dest("edit_product", "Editar Producto", Icons.Filled.Edit)
    data object CreateUser : Dest("create_user", "Crear Usuario", Icons.Filled.Add)
    data object EditUser : Dest("edit_user", "Editar Usuario", Icons.Filled.Edit)
}

@Composable
fun ProductListScreen(viewModel: ProductViewModel, navController: NavHostController) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Productos",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Error: ${uiState.error}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.retry() }) {
                        Text("Reintentar")
                    }
                }
            }
        } else if (uiState.products.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay productos disponibles")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.products) { product ->
                    ProductCard(
                        product = product, 
                        navController = navController,
                        onDelete = { productId -> viewModel.deleteProduct(productId) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, navController: NavHostController, onDelete: (String) -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Producto") },
            text = { Text("¿Estás seguro de que deseas eliminar ${product.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(product.id)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = product.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (product.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = product.description,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$${String.format("%.2f", product.price / 100.0)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Column {
                    IconButton(
                        onClick = { 
                            navController.navigate("${Dest.EditProduct.route}/${product.id}")
                        }
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar")
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true }
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                    }
                }
            }
        }
    }
}

@Composable
fun UserListScreen(viewModel: UserViewModel, navController: NavHostController) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Usuarios",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Error: ${uiState.error}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.retry() }) {
                        Text("Reintentar")
                    }
                }
            }
        } else if (uiState.users.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay usuarios registrados")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.users) { user ->
                    UserCard(
                        user = user, 
                        navController = navController,
                        onDelete = { userId -> viewModel.deleteUser(userId) }
                    )
                }
            }
        }
    }
}

@Composable
fun UserCard(user: User, navController: NavHostController, onDelete: (String) -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Usuario") },
            text = { Text("¿Estás seguro de que deseas eliminar a ${user.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(user.id)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "@${user.username}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (user.phone.isNotEmpty()) {
                        Text(
                            text = user.phone,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (user.address.isNotEmpty()) {
                        Text(
                            text = user.address,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }
                }
                
                Column {
                    AssistChip(
                        onClick = { },
                        label = { 
                            Text(if (user.isActive) "Activo" else "Inactivo") 
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (user.isActive) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.errorContainer
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { 
                                navController.navigate("${Dest.EditUser.route}/${user.id}")
                            }
                        ) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Editar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = { 
                                showDeleteDialog = true
                            }
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun OrderListScreen(viewModel: OrderViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val notificationService = remember { com.example.burgermenu.services.NotificationService(context) }

    LaunchedEffect(Unit) {
        viewModel.refreshOrders()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pedidos",
                style = MaterialTheme.typography.headlineMedium
            )
            
            // Botón para crear pedido de prueba
            OutlinedButton(
                onClick = {
                    viewModel.createOrder(
                        userId = "user_test",
                        userName = "Cliente Prueba",
                        userEmail = "test@example.com",
                        userPhone = "123-456-7890",
                        userAddress = "Dirección de prueba 123",
                        items = "[{\"name\":\"Hamburguesa Clásica\",\"quantity\":2,\"price\":15.99}]",
                        total = 31.98,
                        onSuccess = { orderId ->
                            // Mostrar notificación cuando se crea el pedido
                            notificationService.showNewOrderNotification(
                                orderId = orderId,
                                customerName = "Cliente Prueba",
                                total = 31.98
                            )
                        }
                    )
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Pedido Prueba")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Filtros por estado
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            items(uiState.availableStatuses) { status ->
                FilterChip(
                    onClick = { viewModel.filterByStatus(status) },
                    label = { 
                        Text(if (status == "Todos") status else viewModel.getStatusDisplayName(status)) 
                    },
                    selected = uiState.selectedStatus == status
                )
            }
        }
        
        // Contenido principal
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Error: ${uiState.error}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.retry() }) {
                        Text("Reintentar")
                    }
                }
            }
        } else if (uiState.orders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay pedidos",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Los pedidos aparecerán aquí cuando se creen",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.orders) { order ->
                    OrderCard(
                        order = order,
                        onStatusChange = { newStatus ->
                            viewModel.updateOrderStatus(order.id, newStatus)
                            // Mostrar notificación de cambio de estado
                            notificationService.showOrderStatusUpdateNotification(
                                orderId = order.id,
                                newStatus = newStatus,
                                customerName = order.userName
                            )
                        },
                        availableStatuses = viewModel.getAvailableStatusesForOrder(order.status),
                        getStatusDisplayName = { status -> viewModel.getStatusDisplayName(status) }
                    )
                }
            }
        }
    }
}

// Pantallas de CRUD básicas (sin funcionalidades avanzadas por ahora)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProductScreen(navController: NavHostController, productViewModel: ProductViewModel) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var isCreating by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var showErrorMessage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val repository = remember { com.example.burgermenu.data.repository.ProductRepository() }
    val context = LocalContext.current
    
    // Crear archivo temporal para la foto
    val photoFile = remember {
        File(
            context.cacheDir,
            "product_${System.currentTimeMillis()}.jpg"
        )
    }
    
    val photoUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
    }
    
    // Launcher para la cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = photoUri.toString()
        }
    }
    
    // Launcher para permisos de cámara
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(photoUri)
        } else {
            errorMessage = "Permiso de cámara requerido"
            showErrorMessage = true
        }
    }
    
    // Mostrar mensajes
    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            kotlinx.coroutines.delay(2000)
            navController.popBackStack()
        }
    }
    
    LaunchedEffect(showErrorMessage) {
        if (showErrorMessage) {
            kotlinx.coroutines.delay(3000)
            showErrorMessage = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Producto") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del producto") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Precio") },
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("$") }
            )
            
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Categoría") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Botón para tomar foto
            OutlinedButton(
                onClick = {
                    permissionLauncher.launch(android.Manifest.permission.CAMERA)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (imageUri != null) "Cambiar foto" else "Tomar foto")
            }
            
            if (imageUri != null) {
                Text(
                    text = "✓ Foto capturada",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Mensajes de estado
            if (showSuccessMessage) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "✓ Producto creado exitosamente",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            if (showErrorMessage) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "Error: $errorMessage",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            Button(
                onClick = {
                    if (name.isNotBlank() && price.isNotBlank() && category.isNotBlank()) {
                        val priceValue = price.toDoubleOrNull()
                        if (priceValue != null && priceValue > 0) {
                            isCreating = true
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                repository.createProduct(name, description, priceValue, category)
                                    .onSuccess {
                                        isCreating = false
                                        showSuccessMessage = true
                                        productViewModel.refreshProducts()
                                    }
                                    .onFailure { exception ->
                                        isCreating = false
                                        errorMessage = exception.message ?: "Error desconocido"
                                        showErrorMessage = true
                                    }
                            }
                        } else {
                            errorMessage = "Precio debe ser un número válido mayor a 0"
                            showErrorMessage = true
                        }
                    } else {
                        errorMessage = "Todos los campos son obligatorios"
                        showErrorMessage = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCreating
            ) {
                if (isCreating) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Crear Producto")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(productId: String, navController: NavHostController, productViewModel: ProductViewModel) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isUpdating by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var showErrorMessage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var pendingUpdate by remember { mutableStateOf(false) }
    
    val repository = remember { com.example.burgermenu.data.repository.ProductRepository() }
    val context = LocalContext.current
    
    // Cargar datos del producto
    LaunchedEffect(productId) {
        repository.getProductById(productId)
            .onSuccess { product ->
                product?.let {
                    name = it.name
                    description = it.description
                    price = String.format("%.2f", it.price / 100.0)
                    category = it.category
                }
                isLoading = false
            }
            .onFailure {
                errorMessage = "Error al cargar producto"
                showErrorMessage = true
                isLoading = false
            }
    }
    
    // Función para actualizar producto
    val performUpdate = {
        val priceValue = price.toDoubleOrNull()
        if (priceValue != null && priceValue > 0) {
            isUpdating = true
            CoroutineScope(Dispatchers.Main).launch {
                repository.updateProduct(productId, name, description, priceValue, category)
                    .onSuccess {
                        isUpdating = false
                        showSuccessMessage = true
                        productViewModel.refreshProducts()
                        pendingUpdate = false
                    }
                    .onFailure { exception ->
                        isUpdating = false
                        errorMessage = exception.message ?: "Error desconocido"
                        showErrorMessage = true
                        pendingUpdate = false
                    }
            }
        } else {
            errorMessage = "Precio debe ser un número válido mayor a 0"
            showErrorMessage = true
            pendingUpdate = false
        }
    }
    
    // Función para mostrar prompt biométrico
    val showBiometricAuthentication = {
        try {
            val activity = context as FragmentActivity
            val biometricManager = BiometricManager.from(context)
            
            when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    val biometricPrompt = BiometricPrompt(activity, 
                        ContextCompat.getMainExecutor(context),
                        object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                super.onAuthenticationSucceeded(result)
                                // Proceder con la actualización
                                performUpdate()
                            }
                            
                            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                super.onAuthenticationError(errorCode, errString)
                                errorMessage = "Error de autenticación: $errString"
                                showErrorMessage = true
                                pendingUpdate = false
                            }
                            
                            override fun onAuthenticationFailed() {
                                super.onAuthenticationFailed()
                                errorMessage = "Autenticación fallida"
                                showErrorMessage = true
                                pendingUpdate = false
                            }
                        }
                    )
                    
                    val promptInfo = BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Confirmar actualización")
                        .setSubtitle("Usa tu huella dactilar para confirmar la actualización del producto")
                        .setNegativeButtonText("Cancelar")
                        .build()
                    
                    biometricPrompt.authenticate(promptInfo)
                }
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                    errorMessage = "No hay sensor biométrico disponible. Actualizando sin autenticación."
                    showErrorMessage = true
                    // Actualizar sin biometría
                    performUpdate()
                }
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                    errorMessage = "Sensor biométrico no disponible. Actualizando sin autenticación."
                    showErrorMessage = true
                    // Actualizar sin biometría
                    performUpdate()
                }
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    errorMessage = "No hay huellas registradas. Actualizando sin autenticación."
                    showErrorMessage = true
                    // Actualizar sin biometría
                    performUpdate()
                }
                else -> {
                    errorMessage = "Autenticación biométrica no disponible. Actualizando sin autenticación."
                    showErrorMessage = true
                    // Actualizar sin biometría
                    performUpdate()
                }
            }
        } catch (e: Exception) {
            errorMessage = "Error con autenticación biométrica: ${e.message}. Actualizando sin autenticación."
            showErrorMessage = true
            // Actualizar sin biometría como fallback
            performUpdate()
        }
    }
    
    // Mostrar mensajes
    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            kotlinx.coroutines.delay(2000)
            navController.popBackStack()
        }
    }
    
    LaunchedEffect(showErrorMessage) {
        if (showErrorMessage) {
            kotlinx.coroutines.delay(3000)
            showErrorMessage = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Producto") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del producto") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Precio") },
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("$") }
                )
                
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Mensajes de estado
                if (showSuccessMessage) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = "✓ Producto actualizado exitosamente",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                if (showErrorMessage) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "Error: $errorMessage",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                
                Button(
                    onClick = {
                        if (name.isNotBlank() && price.isNotBlank() && category.isNotBlank()) {
                            pendingUpdate = true
                            showBiometricAuthentication()
                        } else {
                            errorMessage = "Todos los campos son obligatorios"
                            showErrorMessage = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUpdating && !pendingUpdate
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Lock, contentDescription = null)
                            Text("Actualizar con Huella")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUserScreen(navController: NavHostController, userViewModel: UserViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var showErrorMessage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val repository = remember { com.example.burgermenu.data.repository.UserRepository() }
    
    // Mostrar mensajes
    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            kotlinx.coroutines.delay(2000)
            navController.popBackStack()
        }
    }
    
    LaunchedEffect(showErrorMessage) {
        if (showErrorMessage) {
            kotlinx.coroutines.delay(3000)
            showErrorMessage = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Usuario") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre completo") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nombre de usuario") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Mensajes de estado
            if (showSuccessMessage) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "✓ Usuario creado exitosamente",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            if (showErrorMessage) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "Error: $errorMessage",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank() && username.isNotBlank()) {
                        isCreating = true
                        CoroutineScope(Dispatchers.Main).launch {
                            repository.createUser(username, email, name, phone, address)
                                .onSuccess {
                                    isCreating = false
                                    showSuccessMessage = true
                                    userViewModel.loadUsers()
                                }
                                .onFailure { exception ->
                                    isCreating = false
                                    errorMessage = exception.message ?: "Error desconocido"
                                    showErrorMessage = true
                                }
                        }
                    } else {
                        errorMessage = "Nombre, email y username son obligatorios"
                        showErrorMessage = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCreating
            ) {
                if (isCreating) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Crear Usuario")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserScreen(userId: String, navController: NavHostController, userViewModel: UserViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isUpdating by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var showErrorMessage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var pendingUpdate by remember { mutableStateOf(false) }
    
    val repository = remember { com.example.burgermenu.data.repository.UserRepository() }
    val context = LocalContext.current
    
    // Cargar datos del usuario
    LaunchedEffect(userId) {
        repository.getUserById(userId)
            .onSuccess { user ->
                user?.let {
                    name = it.name
                    email = it.email
                    username = it.username
                    phone = it.phone
                    address = it.address
                }
                isLoading = false
            }
            .onFailure {
                errorMessage = "Error al cargar usuario"
                showErrorMessage = true
                isLoading = false
            }
    }
    
    // Función para actualizar usuario
    val performUpdate = {
        if (name.isNotBlank() && email.isNotBlank() && username.isNotBlank()) {
            isUpdating = true
            CoroutineScope(Dispatchers.Main).launch {
                repository.updateUser(userId, username, email, name, phone, address)
                    .onSuccess {
                        isUpdating = false
                        showSuccessMessage = true
                        userViewModel.loadUsers()
                        pendingUpdate = false
                    }
                    .onFailure { exception ->
                        isUpdating = false
                        errorMessage = exception.message ?: "Error desconocido"
                        showErrorMessage = true
                        pendingUpdate = false
                    }
            }
        } else {
            errorMessage = "Nombre, email y username son obligatorios"
            showErrorMessage = true
            pendingUpdate = false
        }
    }
    
    // Función para mostrar prompt biométrico
    val showBiometricAuthentication = {
        try {
            val activity = context as FragmentActivity
            val biometricManager = BiometricManager.from(context)
            
            when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    val biometricPrompt = BiometricPrompt(activity, 
                        ContextCompat.getMainExecutor(context),
                        object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                super.onAuthenticationSucceeded(result)
                                // Proceder con la actualización
                                performUpdate()
                            }
                            
                            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                super.onAuthenticationError(errorCode, errString)
                                errorMessage = "Error de autenticación: $errString"
                                showErrorMessage = true
                                pendingUpdate = false
                            }
                            
                            override fun onAuthenticationFailed() {
                                super.onAuthenticationFailed()
                                errorMessage = "Autenticación fallida"
                                showErrorMessage = true
                                pendingUpdate = false
                            }
                        }
                    )
                    
                    val promptInfo = BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Confirmar actualización")
                        .setSubtitle("Usa tu huella dactilar para confirmar la actualización del usuario")
                        .setNegativeButtonText("Cancelar")
                        .build()
                    
                    biometricPrompt.authenticate(promptInfo)
                }
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                    errorMessage = "No hay sensor biométrico disponible. Actualizando sin autenticación."
                    showErrorMessage = true
                    // Actualizar sin biometría
                    performUpdate()
                }
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                    errorMessage = "Sensor biométrico no disponible. Actualizando sin autenticación."
                    showErrorMessage = true
                    // Actualizar sin biometría
                    performUpdate()
                }
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    errorMessage = "No hay huellas registradas. Actualizando sin autenticación."
                    showErrorMessage = true
                    // Actualizar sin biometría
                    performUpdate()
                }
                else -> {
                    errorMessage = "Autenticación biométrica no disponible. Actualizando sin autenticación."
                    showErrorMessage = true
                    // Actualizar sin biometría
                    performUpdate()
                }
            }
        } catch (e: Exception) {
            errorMessage = "Error con autenticación biométrica: ${e.message}. Actualizando sin autenticación."
            showErrorMessage = true
            // Actualizar sin biometría como fallback
            performUpdate()
        }
    }
    
    // Mostrar mensajes
    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            kotlinx.coroutines.delay(2000)
            navController.popBackStack()
        }
    }
    
    LaunchedEffect(showErrorMessage) {
        if (showErrorMessage) {
            kotlinx.coroutines.delay(3000)
            showErrorMessage = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Usuario") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre completo") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Nombre de usuario") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Dirección") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Mensajes de estado
                if (showSuccessMessage) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = "✓ Usuario actualizado exitosamente",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                if (showErrorMessage) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "Error: $errorMessage",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                
                Button(
                    onClick = {
                        if (name.isNotBlank() && email.isNotBlank() && username.isNotBlank()) {
                            pendingUpdate = true
                            showBiometricAuthentication()
                        } else {
                            errorMessage = "Nombre, email y username son obligatorios"
                            showErrorMessage = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUpdating && !pendingUpdate
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Lock, contentDescription = null)
                            Text("Actualizar con Huella")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderCard(
    order: Order,
    onStatusChange: (String) -> Unit,
    availableStatuses: List<String>,
    getStatusDisplayName: (String) -> String
) {
    var showStatusDialog by remember { mutableStateOf(false) }
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pedido #${order.id.takeLast(8)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = order.userName,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (order.userPhone.isNotEmpty()) {
                        Text(
                            text = "📞 ${order.userPhone}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (order.userAddress.isNotEmpty()) {
                        Text(
                            text = "📍 ${order.userAddress}",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format("%.2f", order.total)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Chip de estado clickeable
                    AssistChip(
                        onClick = { 
                            if (availableStatuses.isNotEmpty()) {
                                showStatusDialog = true
                            }
                        },
                        label = { Text(getStatusDisplayName(order.status)) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = when (order.status) {
                                "delivered" -> MaterialTheme.colorScheme.primaryContainer
                                "ready" -> MaterialTheme.colorScheme.secondaryContainer
                                "preparing" -> MaterialTheme.colorScheme.tertiaryContainer
                                "confirmed" -> MaterialTheme.colorScheme.surfaceVariant
                                "pending" -> MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                                "cancelled" -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        enabled = availableStatuses.isNotEmpty()
                    )
                }
            }
            
            // Mostrar items si están disponibles
            if (order.items.isNotEmpty() && order.items != "[]") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Items: ${parseOrderItems(order.items)}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
            }
        }
    }
    
    // Diálogo para cambiar estado
    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Cambiar Estado del Pedido") },
            text = {
                Column {
                    Text("Selecciona el nuevo estado:")
                    Spacer(modifier = Modifier.height(8.dp))
                    availableStatuses.forEach { status ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onStatusChange(status)
                                    showStatusDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                when (status) {
                                    "confirmed" -> Icons.Filled.Check
                                    "preparing" -> Icons.Filled.Build
                                    "ready" -> Icons.Filled.Notifications
                                    "delivered" -> Icons.Filled.Done
                                    "cancelled" -> Icons.Filled.Clear
                                    else -> Icons.Filled.Add
                                },
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(getStatusDisplayName(status))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStatusDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// Función helper para parsear items de pedidos
fun parseOrderItems(itemsJson: String): String {
    return try {
        // Simplificado: extraer nombres de productos del JSON
        if (itemsJson.contains("name")) {
            // Buscar patrones como "name":"Producto"
            val regex = """"name":"([^"]+)"""".toRegex()
            val matches = regex.findAll(itemsJson)
            val items = matches.map { it.groupValues[1] }.toList()
            if (items.isNotEmpty()) {
                items.joinToString(", ")
            } else {
                "Productos varios"
            }
        } else {
            "Productos varios"
        }
    } catch (e: Exception) {
        "Items no disponibles"
    }
}