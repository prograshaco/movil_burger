# 🗄️ Documentación de Base de Datos y API - BurgerMenu

## 🌐 Turso Database API

### **📡 Configuración de Conexión**

#### **Endpoint Base**
```
URL: https://restaurant-prograshaco.aws-us-west-2.turso.io
Versión API: v2/pipeline
Método: POST
Content-Type: application/json
Authorization: Bearer {AUTH_TOKEN}
```

#### **Formato de Request**
```json
{
  "requests": [
    {
      "type": "execute",
      "stmt": {
        "sql": "SELECT * FROM products"
      }
    }
  ]
}
```

#### **Formato de Response**
```json
{
  "results": [
    {
      "response": {
        "type": "ok",
        "result": {
          "cols": [
            {"name": "id", "decltype": "TEXT"},
            {"name": "name", "decltype": "TEXT"},
            {"name": "price", "decltype": "REAL"}
          ],
          "rows": [
            [
              {"type": "text", "value": "prod_1"},
              {"type": "text", "value": "Hamburguesa"},
              {"type": "float", "value": 15.99}
            ]
          ],
          "affected_row_count": 0,
          "last_insert_rowid": null
        }
      }
    }
  ]
}
```

## 📊 Esquema de Base de Datos

### **🍔 Tabla: `products`**

#### **Estructura**
```sql
CREATE TABLE products (
    id TEXT PRIMARY KEY,              -- Identificador único del producto
    name TEXT NOT NULL,               -- Nombre del producto
    description TEXT,                 -- Descripción detallada
    price REAL NOT NULL,              -- Precio en formato decimal (15.99)
    category TEXT NOT NULL,           -- Categoría del producto
    image_url TEXT,                   -- URL de la imagen (opcional)
    available INTEGER DEFAULT 1,      -- Disponibilidad (1=disponible, 0=no disponible)
    created_at TEXT,                  -- Fecha de creación (ISO 8601)
    updated_at TEXT                   -- Fecha de última actualización (ISO 8601)
);
```

#### **Índices**
```sql
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_available ON products(available);
CREATE INDEX idx_products_created_at ON products(created_at);
```

#### **Operaciones CRUD**

##### **Crear Producto**
```sql
INSERT INTO products (
    id, name, description, price, category, 
    image_url, available, created_at, updated_at
) VALUES (
    'prod_uuid', 'Hamburguesa Clásica', 'Carne, lechuga, tomate', 
    15.99, 'Hamburguesas', '', 1, 
    '2024-01-01 12:00:00', '2024-01-01 12:00:00'
);
```

##### **Leer Productos**
```sql
-- Todos los productos
SELECT * FROM products ORDER BY created_at DESC;

-- Por categoría
SELECT * FROM products WHERE category = 'Hamburguesas' AND available = 1;

-- Por ID
SELECT * FROM products WHERE id = 'prod_uuid';

-- Categorías disponibles
SELECT DISTINCT category FROM products WHERE available = 1 ORDER BY category;
```

##### **Actualizar Producto**
```sql
UPDATE products 
SET name = 'Nuevo Nombre', 
    description = 'Nueva Descripción', 
    price = 18.99, 
    category = 'Nueva Categoría',
    updated_at = '2024-01-01 13:00:00'
WHERE id = 'prod_uuid';
```

##### **Eliminar Producto**
```sql
-- Eliminación lógica (recomendado)
UPDATE products SET available = 0, updated_at = '2024-01-01 13:00:00' WHERE id = 'prod_uuid';

-- Eliminación física
DELETE FROM products WHERE id = 'prod_uuid';
```

### **👥 Tabla: `users`**

#### **Estructura**
```sql
CREATE TABLE users (
    id TEXT PRIMARY KEY,              -- Identificador único del usuario
    username TEXT UNIQUE NOT NULL,    -- Nombre de usuario único
    email TEXT UNIQUE NOT NULL,       -- Email único
    name TEXT NOT NULL,               -- Nombre completo
    phone TEXT,                       -- Número de teléfono (opcional)
    address TEXT,                     -- Dirección (opcional)
    is_active INTEGER DEFAULT 1,      -- Estado activo (1=activo, 0=inactivo)
    created_at TEXT,                  -- Fecha de creación
    updated_at TEXT                   -- Fecha de última actualización
);
```

#### **Índices**
```sql
CREATE UNIQUE INDEX idx_users_username ON users(username);
CREATE UNIQUE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_is_active ON users(is_active);
```

#### **Operaciones CRUD**

##### **Crear Usuario**
```sql
INSERT INTO users (
    id, username, email, name, phone, address, 
    is_active, created_at, updated_at
) VALUES (
    'user_uuid', 'johndoe', 'john@example.com', 'John Doe', 
    '123-456-7890', '123 Main St', 1, 
    '2024-01-01 12:00:00', '2024-01-01 12:00:00'
);
```

##### **Leer Usuarios**
```sql
-- Todos los usuarios activos
SELECT * FROM users WHERE is_active = 1 ORDER BY name;

-- Por ID
SELECT * FROM users WHERE id = 'user_uuid';

-- Por username
SELECT * FROM users WHERE username = 'johndoe';

-- Por email
SELECT * FROM users WHERE email = 'john@example.com';
```

##### **Actualizar Usuario**
```sql
UPDATE users 
SET name = 'John Smith', 
    email = 'johnsmith@example.com', 
    phone = '098-765-4321',
    address = '456 Oak Ave',
    updated_at = '2024-01-01 13:00:00'
WHERE id = 'user_uuid';
```

##### **Desactivar Usuario**
```sql
UPDATE users 
SET is_active = 0, updated_at = '2024-01-01 13:00:00' 
WHERE id = 'user_uuid';
```

### **📋 Tabla: `orders`**

#### **Estructura**
```sql
CREATE TABLE orders (
    id TEXT PRIMARY KEY,              -- Identificador único del pedido
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

#### **Estados de Pedido**
```sql
-- Estados válidos
CHECK (status IN ('pending', 'confirmed', 'preparing', 'ready', 'delivered', 'cancelled'))
```

#### **Índices**
```sql
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
```

#### **Formato de Items (JSON)**
```json
[
  {
    "id": "prod_1",
    "name": "Hamburguesa Clásica",
    "price": 15.99,
    "quantity": 2,
    "subtotal": 31.98
  },
  {
    "id": "prod_2",
    "name": "Coca Cola",
    "price": 3.50,
    "quantity": 1,
    "subtotal": 3.50
  }
]
```

#### **Operaciones CRUD**

##### **Crear Pedido**
```sql
INSERT INTO orders (
    id, user_id, user_name, user_email, user_phone, 
    user_address, items, total, status, created_at, updated_at
) VALUES (
    'order_uuid', 'user_1', 'John Doe', 'john@example.com', 
    '123-456-7890', '123 Main St', 
    '[{"id":"prod_1","name":"Hamburguesa","price":15.99,"quantity":2,"subtotal":31.98}]',
    31.98, 'pending', 
    '2024-01-01 12:00:00', '2024-01-01 12:00:00'
);
```

##### **Leer Pedidos**
```sql
-- Todos los pedidos
SELECT * FROM orders ORDER BY created_at DESC;

-- Por estado
SELECT * FROM orders WHERE status = 'pending' ORDER BY created_at ASC;

-- Por usuario
SELECT * FROM orders WHERE user_id = 'user_1' ORDER BY created_at DESC;

-- Por ID
SELECT * FROM orders WHERE id = 'order_uuid';

-- Estadísticas por estado
SELECT status, COUNT(*) as count FROM orders GROUP BY status;
```

##### **Actualizar Estado de Pedido**
```sql
UPDATE orders 
SET status = 'confirmed', updated_at = '2024-01-01 13:00:00' 
WHERE id = 'order_uuid';
```

##### **Consultas Avanzadas**
```sql
-- Pedidos del día
SELECT * FROM orders 
WHERE DATE(created_at) = DATE('now') 
ORDER BY created_at DESC;

-- Total de ventas por día
SELECT DATE(created_at) as date, SUM(total) as daily_total 
FROM orders 
WHERE status = 'delivered' 
GROUP BY DATE(created_at) 
ORDER BY date DESC;

-- Productos más vendidos
SELECT 
    JSON_EXTRACT(items, '$[*].name') as product_name,
    SUM(JSON_EXTRACT(items, '$[*].quantity')) as total_quantity
FROM orders 
WHERE status = 'delivered'
GROUP BY product_name 
ORDER BY total_quantity DESC;
```

## 🔧 Implementación en Kotlin

### **TursoClient - Cliente de Base de Datos**

#### **Configuración**
```kotlin
object TursoClient {
    private const val TURSO_DATABASE_URL = "https://restaurant-prograshaco.aws-us-west-2.turso.io"
    private const val TURSO_AUTH_TOKEN = "eyJhbGciOiJFZERTQSIsInR5cCI6IkpXVCJ9..."
    
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

#### **Ejecutar Consulta**
```kotlin
suspend fun executeQuery(sql: String): Result<TursoResponse> {
    return try {
        val requestBody = TursoRequest(
            requests = listOf(
                TursoExecuteRequest(
                    type = "execute",
                    stmt = TursoStatement(sql = sql)
                )
            )
        )
        
        val response: HttpResponse = httpClient.post("$TURSO_DATABASE_URL/v2/pipeline") {
            header("Authorization", "Bearer $TURSO_AUTH_TOKEN")
            header("Content-Type", "application/json")
            setBody(requestBody)
        }
        
        if (response.status.isSuccess()) {
            val tursoResponse = response.body<TursoResponse>()
            Result.success(tursoResponse)
        } else {
            val responseText = response.bodyAsText()
            Result.failure(Exception("HTTP ${response.status}: $responseText"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

#### **Procesar Respuesta**
```kotlin
fun rowsToMaps(columns: List<String>, rows: List<JsonArray>): List<Map<String, String>> {
    return rows.map { row ->
        columns.mapIndexed { index, column ->
            val value = if (index < row.size) {
                val element = row[index]
                when (element) {
                    is JsonPrimitive -> element.content
                    is JsonObject -> {
                        // Formato Turso v2: {"type":"text","value":"valor_real"}
                        val typeValue = element["type"]?.let { 
                            when (it) {
                                is JsonPrimitive -> it.content
                                else -> it.toString()
                            }
                        }
                        
                        when (typeValue) {
                            "null" -> ""
                            else -> {
                                element["value"]?.let { valueElement ->
                                    when (valueElement) {
                                        is JsonPrimitive -> valueElement.content
                                        else -> valueElement.toString()
                                    }
                                } ?: ""
                            }
                        }
                    }
                    else -> element.toString()
                }
            } else {
                ""
            }
            column to value
        }.toMap()
    }
}
```

### **Repositorios - Capa de Acceso a Datos**

#### **ProductRepository**
```kotlin
class ProductRepository {
    suspend fun getAllProducts(): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val response = TursoClient.executeQuery(
                "SELECT * FROM products WHERE available = 1 ORDER BY created_at DESC"
            )
            
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
    
    suspend fun createProduct(name: String, description: String, price: Double, category: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val productId = "prod_${UUID.randomUUID()}"
                val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                
                val safeName = name.replace("'", "''")
                val safeDescription = description.replace("'", "''")
                val safeCategory = category.replace("'", "''")
                
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
}
```

## 🔍 Consultas de Ejemplo

### **Reportes y Analytics**

#### **Productos más vendidos**
```sql
SELECT 
    p.name,
    p.category,
    COUNT(o.id) as order_count,
    SUM(JSON_EXTRACT(o.items, '$[*].quantity')) as total_sold
FROM products p
JOIN orders o ON JSON_EXTRACT(o.items, '$[*].id') LIKE '%' || p.id || '%'
WHERE o.status = 'delivered'
GROUP BY p.id, p.name, p.category
ORDER BY total_sold DESC
LIMIT 10;
```

#### **Ventas por categoría**
```sql
SELECT 
    p.category,
    COUNT(DISTINCT o.id) as orders,
    SUM(o.total) as revenue
FROM orders o
JOIN products p ON JSON_EXTRACT(o.items, '$[*].id') LIKE '%' || p.id || '%'
WHERE o.status = 'delivered'
  AND DATE(o.created_at) >= DATE('now', '-30 days')
GROUP BY p.category
ORDER BY revenue DESC;
```

#### **Estadísticas de pedidos por día**
```sql
SELECT 
    DATE(created_at) as date,
    COUNT(*) as total_orders,
    COUNT(CASE WHEN status = 'delivered' THEN 1 END) as completed_orders,
    COUNT(CASE WHEN status = 'cancelled' THEN 1 END) as cancelled_orders,
    SUM(CASE WHEN status = 'delivered' THEN total ELSE 0 END) as daily_revenue
FROM orders
WHERE DATE(created_at) >= DATE('now', '-7 days')
GROUP BY DATE(created_at)
ORDER BY date DESC;
```

#### **Usuarios más activos**
```sql
SELECT 
    user_name,
    user_email,
    COUNT(*) as total_orders,
    SUM(total) as total_spent,
    AVG(total) as avg_order_value,
    MAX(created_at) as last_order
FROM orders
WHERE status = 'delivered'
GROUP BY user_id, user_name, user_email
HAVING COUNT(*) >= 2
ORDER BY total_spent DESC
LIMIT 20;
```

## 🚨 Manejo de Errores

### **Códigos de Error Comunes**

#### **HTTP Status Codes**
- **200**: Éxito
- **400**: Bad Request (SQL inválido)
- **401**: Unauthorized (token inválido)
- **403**: Forbidden (permisos insuficientes)
- **404**: Not Found (base de datos no encontrada)
- **500**: Internal Server Error

#### **Errores de SQL**
```sql
-- Error de sintaxis
SQLSTATE[42000]: Syntax error or access violation

-- Violación de constraint único
SQLSTATE[23000]: Integrity constraint violation

-- Tabla no existe
SQLSTATE[42S02]: Base table or view not found
```

#### **Manejo en Kotlin**
```kotlin
suspend fun executeQueryWithErrorHandling(sql: String): Result<TursoResponse> {
    return try {
        val response = TursoClient.executeQuery(sql)
        
        if (response.isSuccess) {
            response
        } else {
            val exception = response.exceptionOrNull()
            when {
                exception?.message?.contains("syntax error", ignoreCase = true) == true -> {
                    Result.failure(Exception("Error de sintaxis en la consulta SQL"))
                }
                exception?.message?.contains("constraint", ignoreCase = true) == true -> {
                    Result.failure(Exception("Violación de restricción: datos duplicados"))
                }
                exception?.message?.contains("not found", ignoreCase = true) == true -> {
                    Result.failure(Exception("Tabla o registro no encontrado"))
                }
                else -> {
                    Result.failure(Exception("Error de base de datos: ${exception?.message}"))
                }
            }
        }
    } catch (e: Exception) {
        Result.failure(Exception("Error de conexión: ${e.message}"))
    }
}
```

Esta documentación proporciona una referencia completa para trabajar con la base de datos Turso y entender la estructura de datos de la aplicación BurgerMenu.