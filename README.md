# 🍔 BurgerMenu - Sistema de Gestión de Restaurante

Una aplicación móvil completa para la gestión de restaurantes desarrollada en **Kotlin** con **Jetpack Compose**.Sistema 

## 📱 Características Principales

### ✅ **CRUD Completo**
- **Productos**: Crear, listar, editar y eliminar productos
- **Usuarios**: Gestión completa de usuarios del sistema
- **Pedidos**: Sistema completo de gestión de pedidos

### 🔐 **Seguridad**
- **Autenticación biométrica** para editar productos y usuarios
- **Validación de campos** en todos los formularios
- **Manejo seguro de errores**

### 🔔 **Notificaciones**
- **Notificaciones push** para nuevos pedidos
- **Sonido y vibración** estilo Uber
- **Notificaciones de cambio de estado** de pedidos

### 📷 **Funcionalidades Avanzadas**
- **Cámara integrada** para fotos de productos
- **Cambio de estado de pedidos** con flujo lógico
- **Filtros por estado** en pedidos
- **Interfaz moderna** con Material Design 3

## 🛠️ Tecnologías Utilizadas

- **Kotlin** - Lenguaje principal
- **Jetpack Compose** - UI moderna y declarativa
- **Turso Database** - Base de datos en la nube
- **Ktor Client** - Cliente HTTP para API
- **Biometric API** - Autenticación por huella dactilar
- **CameraX** - Captura de imágenes
- **Material Design 3** - Sistema de diseño

## 📊 Base de Datos

### **Tablas principales:**
- `products` - Productos del menú
- `users` - Usuarios del sistema
- `orders` - Pedidos de clientes

### **Conexión:**
- **Turso Database** en la nube
- **Sincronización en tiempo real**
- **Manejo de errores robusto**

## 🚀 Estados de Pedidos

El sistema maneja un flujo lógico de estados:

1. **🟡 Pendiente** → Pedido recién creado
2. **🔵 Confirmado** → Pedido aceptado por el restaurante
3. **🟠 Preparando** → Pedido en cocina
4. **🟢 Listo** → Pedido terminado, listo para entrega
5. **✅ Entregado** → Pedido completado
6. **❌ Cancelado** → Pedido cancelado

## 📱 Capturas de Pantalla

### Gestión de Productos
- Lista de productos con imágenes
- Formulario de creación con cámara
- Edición con autenticación biométrica

### Gestión de Pedidos
- Lista filtrable por estado
- Cambio de estado intuitivo
- Notificaciones automáticas

### Seguridad
- Autenticación por huella dactilar
- Fallback para dispositivos sin biometría

## 🔧 Instalación y Configuración

### **Prerrequisitos:**
- Android Studio Arctic Fox o superior
- SDK de Android 24+
- Dispositivo Android o emulador

### **Pasos:**
1. Clona el repositorio
```bash
git clone https://github.com/prograshaco/movil_burger.git
```

2. Abre el proyecto en Android Studio

3. Sincroniza las dependencias de Gradle

4. Ejecuta la aplicación

## 🌐 Configuración de Base de Datos

La aplicación está configurada para usar **Turso Database**. Las credenciales están incluidas en el código para fines de demostración.

### **Estructura de tablas:**

```sql
-- Productos
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

-- Usuarios
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

-- Pedidos
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
```

## 🎯 Funcionalidades Destacadas

### **Autenticación Biométrica**
- Requerida para editar productos y usuarios
- Fallback automático para dispositivos sin biometría
- Manejo de todos los casos de error

### **Sistema de Notificaciones**
- Notificación inmediata al crear pedidos
- Sonido personalizado y vibración
- Notificaciones de cambio de estado

### **Gestión de Imágenes**
- Captura con cámara integrada
- Permisos manejados automáticamente
- Almacenamiento temporal seguro

## 👥 Contribución

Este proyecto fue desarrollado como parte del sistema de gestión para restaurantes.

### **Desarrolladores:**
- Sistema de productos y usuarios
- Autenticación biométrica
- Sistema de notificaciones
- Interfaz de usuario

## 📄 Licencia

Este proyecto es de uso educativo y demostrativo.

## 📞 Contacto

Para más información sobre el proyecto, contacta al equipo de desarrollo.

---

**🍔 BurgerMenu** - Gestión moderna para restaurantes modernos