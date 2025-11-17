# Integración con API de Productos

## Descripción

Este proyecto ahora está conectado a la API REST de productos alojada en Render:
- **URL Base**: `https://api-burger.onrender.com/api`
- **Endpoint de Productos**: `/products`

## Arquitectura

### 1. Cliente API (`BurgerApiClient.kt`)

Cliente HTTP usando Ktor que maneja todas las peticiones a la API:

```kotlin
// Obtener todos los productos
BurgerApiClient.getAllProducts()

// Obtener producto por ID
BurgerApiClient.getProductById(id)

// Crear producto
BurgerApiClient.createProduct(apiProduct)

// Actualizar producto
BurgerApiClient.updateProduct(id, apiProduct)

// Eliminar producto
BurgerApiClient.deleteProduct(id)
```

### 2. Repositorio (`BurgerApiProductRepository.kt`)

Capa de abstracción que convierte entre los modelos de la API y los modelos locales:

- **ApiProduct**: Modelo de la API (usa `_id`, `price` como Double, `imageUrl`)
- **Product**: Modelo local (usa `id`, `price` en centavos, `image_url`)

### 3. ViewModel (`ProductViewModel.kt`)

Actualizado para usar `BurgerApiProductRepository` en lugar de `ProductRepository` (Turso).

## Endpoints Disponibles

### GET /api/products
Obtiene todos los productos

**Respuesta**:
```json
[
  {
    "_id": "507f1f77bcf86cd799439011",
    "name": "Hamburguesa Clásica",
    "description": "Hamburguesa con queso y vegetales",
    "price": 8.99,
    "category": "Hamburguesas",
    "imageUrl": "https://example.com/image.jpg",
    "available": true
  }
]
```

### GET /api/products/:id
Obtiene un producto específico

### POST /api/products
Crea un nuevo producto

**Body**:
```json
{
  "name": "Producto Nuevo",
  "description": "Descripción del producto",
  "price": 9.99,
  "category": "Hamburguesas",
  "imageUrl": "https://example.com/image.jpg",
  "available": true
}
```

### PUT /api/products/:id
Actualiza un producto existente

### DELETE /api/products/:id
Elimina un producto

## Uso en la Aplicación

### Cargar Productos

```kotlin
val viewModel = ProductViewModel()

// Observar el estado
viewModel.uiState.collect { state ->
    when {
        state.isLoading -> // Mostrar loading
        state.error != null -> // Mostrar error
        else -> // Mostrar productos: state.products
    }
}
```

### Filtrar por Categoría

```kotlin
viewModel.selectCategory("Hamburguesas")
```

### Refrescar Productos

```kotlin
viewModel.refreshProducts()
```

## Conversión de Modelos

### API → Local
```kotlin
ApiProduct(
    _id = "123",
    price = 8.99,
    imageUrl = "url"
) → Product(
    id = "123",
    price = 899, // centavos
    image_url = "url"
)
```

### Local → API
```kotlin
Product(
    id = "123",
    price = 899, // centavos
    image_url = "url"
) → ApiProduct(
    _id = "123",
    price = 8.99,
    imageUrl = "url"
)
```

## Manejo de Errores

Todos los métodos del repositorio retornan `Result<T>`:

```kotlin
repository.getAllProducts()
    .onSuccess { products ->
        // Manejar éxito
    }
    .onFailure { exception ->
        // Manejar error
        Log.e("TAG", "Error: ${exception.message}")
    }
```

## Logs

Todos los componentes incluyen logs detallados:
- `BurgerApiClient`: Logs de peticiones HTTP
- `BurgerApiProductRepository`: Logs de operaciones del repositorio
- `ProductViewModel`: Logs de cambios de estado

Filtrar en Logcat por:
- `BurgerApiClient`
- `BurgerApiProductRepository`
- `ProductViewModel`

## Migración desde Turso

Si necesitas volver a usar Turso, simplemente cambia en `ProductViewModel.kt`:

```kotlin
// API de Render
private val repository: BurgerApiProductRepository = BurgerApiProductRepository()

// Turso (anterior)
private val repository: ProductRepository = ProductRepository()
```

## Notas Importantes

1. **Permisos**: Asegúrate de tener el permiso de Internet en `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

2. **Timeout**: La API de Render puede tardar en responder si está en modo "sleep". La primera petición puede tomar 30-60 segundos.

3. **Precios**: Los precios se manejan en centavos internamente para evitar problemas de punto flotante, pero se convierten a decimales para la API.

4. **Imágenes**: El campo `imageUrl` es opcional. Si no se proporciona, se usa una cadena vacía.
