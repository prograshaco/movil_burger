# 📱 Guía de Usuario - BurgerMenu

## 🍔 Bienvenido a BurgerMenu

BurgerMenu es una aplicación completa para la gestión de restaurantes que te permite administrar productos, usuarios y pedidos de manera eficiente y segura.

## 🚀 Primeros Pasos

### **📱 Instalación**
1. Descarga la aplicación desde el enlace proporcionado
2. Instala el APK en tu dispositivo Android
3. Acepta los permisos necesarios cuando se soliciten
4. ¡Listo para usar!

### **🔐 Permisos Requeridos**
- **Cámara**: Para tomar fotos de productos
- **Biometría**: Para autenticación segura al editar
- **Notificaciones**: Para recibir alertas de nuevos pedidos
- **Vibración**: Para notificaciones tipo Uber
- **Internet**: Para sincronizar con la base de datos

## 🏠 Navegación Principal

### **📍 Barra de Navegación Inferior**
La aplicación tiene tres secciones principales:

1. **🍔 Productos** - Gestión del menú del restaurante
2. **👥 Usuarios** - Administración de usuarios del sistema
3. **📋 Pedidos** - Control de pedidos y estados

### **➕ Botón Flotante**
- Aparece en las secciones de Productos y Usuarios
- Permite crear nuevos elementos rápidamente
- Cambia según la sección activa

## 🍔 Gestión de Productos

### **📋 Lista de Productos**

#### **Ver Productos**
- La pantalla principal muestra todos los productos disponibles
- Cada producto muestra:
  - 📷 Imagen (si está disponible)
  - 🏷️ Nombre del producto
  - 📂 Categoría
  - 💰 Precio
  - 📝 Descripción (si está disponible)

#### **Filtrar por Categoría**
- Usa los chips en la parte superior para filtrar
- Categorías disponibles: Hamburguesas, Bebidas, Acompañamientos, etc.
- Toca "Todos" para ver todos los productos

### **➕ Crear Producto**

#### **Pasos para Crear**
1. Toca el botón **➕** en la pantalla de productos
2. Completa el formulario:
   - **Nombre**: Nombre del producto (obligatorio)
   - **Descripción**: Descripción detallada (opcional)
   - **Precio**: Precio en formato decimal (ej: 15.99)
   - **Categoría**: Categoría del producto (obligatorio)
3. **📷 Tomar Foto** (opcional):
   - Toca "Tomar foto"
   - Acepta permisos de cámara si se solicitan
   - Toma la foto del producto
   - Verás "✓ Foto capturada" cuando esté lista
4. Toca **"Crear Producto"**
5. ¡Producto creado exitosamente!

#### **Validaciones**
- ✅ Nombre no puede estar vacío
- ✅ Precio debe ser un número válido mayor a 0
- ✅ Categoría no puede estar vacía
- ✅ Descripción es opcional

### **✏️ Editar Producto**

#### **Proceso de Edición**
1. En la lista de productos, toca el ícono **✏️** del producto
2. La pantalla carga automáticamente los datos actuales
3. Modifica los campos que desees cambiar
4. Toca **"Actualizar con Huella"**
5. **🔐 Autenticación Biométrica**:
   - Si tienes sensor de huella: coloca tu dedo
   - Si no tienes sensor: se actualiza automáticamente
   - Si falla la autenticación: puedes reintentar
6. ¡Producto actualizado!

#### **Casos de Autenticación**
- **✅ Con sensor y huellas registradas**: Pide huella dactilar
- **⚠️ Sin sensor biométrico**: Actualiza automáticamente + mensaje informativo
- **⚠️ Sin huellas registradas**: Actualiza automáticamente + mensaje informativo
- **❌ Error de autenticación**: Permite reintentar o cancelar

### **🗑️ Eliminar Producto**
1. Toca el ícono **🗑️** del producto
2. Confirma la eliminación (próximamente)
3. El producto se marca como no disponible

## 👥 Gestión de Usuarios

### **📋 Lista de Usuarios**

#### **Ver Usuarios**
- Muestra todos los usuarios del sistema
- Información visible:
  - 👤 Nombre completo
  - 🏷️ @username
  - 📧 Email
  - 📞 Teléfono (si está disponible)
  - 📍 Dirección (si está disponible)
  - 🟢 Estado (Activo/Inactivo)

### **➕ Crear Usuario**

#### **Formulario de Usuario**
1. Toca el botón **➕** en la pantalla de usuarios
2. Completa los campos:
   - **Nombre completo**: Nombre real del usuario (obligatorio)
   - **Nombre de usuario**: Username único (obligatorio)
   - **Email**: Dirección de correo única (obligatorio)
   - **Teléfono**: Número de contacto (opcional)
   - **Dirección**: Dirección física (opcional)
3. Toca **"Crear Usuario"**
4. ¡Usuario creado exitosamente!

#### **Validaciones de Usuario**
- ✅ Nombre completo es obligatorio
- ✅ Username debe ser único en el sistema
- ✅ Email debe ser único y válido
- ✅ Teléfono y dirección son opcionales

### **✏️ Editar Usuario**

#### **Proceso con Biometría**
1. Toca el ícono **✏️** del usuario
2. Los datos se cargan automáticamente
3. Modifica los campos necesarios
4. Toca **"Actualizar con Huella"**
5. **🔐 Autenticación biométrica** (igual que productos)
6. ¡Usuario actualizado!

### **🔄 Estados de Usuario**
- **🟢 Activo**: Usuario puede usar el sistema
- **🔴 Inactivo**: Usuario deshabilitado temporalmente

## 📋 Gestión de Pedidos

### **📊 Panel de Pedidos**

#### **Vista General**
- Lista todos los pedidos del sistema
- Información por pedido:
  - 🆔 ID del pedido (últimos 8 caracteres)
  - 👤 Nombre del cliente
  - 📞 Teléfono del cliente
  - 📍 Dirección de entrega
  - 💰 Total del pedido
  - 🏷️ Estado actual
  - 📦 Items del pedido

#### **Filtros por Estado**
Usa los chips superiores para filtrar pedidos:
- **Todos**: Muestra todos los pedidos
- **🟡 Pendiente**: Pedidos recién creados
- **🔵 Confirmado**: Pedidos aceptados
- **🟠 Preparando**: Pedidos en cocina
- **🟢 Listo**: Pedidos terminados
- **✅ Entregado**: Pedidos completados
- **❌ Cancelado**: Pedidos cancelados

### **📝 Crear Pedido de Prueba**

#### **Función de Testing**
1. Toca **"Pedido Prueba"** en la pantalla de pedidos
2. Se crea automáticamente un pedido de ejemplo
3. **🔔 Recibirás una notificación** con:
   - Sonido de alerta
   - Vibración estilo Uber
   - Notificación push en la pantalla
4. El pedido aparece en la lista con estado "Pendiente"

### **🔄 Cambiar Estado de Pedido**

#### **Proceso de Cambio**
1. **Toca el chip de estado** del pedido que quieres cambiar
2. Se abre un diálogo con **estados disponibles**:
   - Desde **Pendiente**: → Confirmado, Cancelado
   - Desde **Confirmado**: → Preparando, Cancelado
   - Desde **Preparando**: → Listo, Cancelado
   - Desde **Listo**: → Entregado
3. **Selecciona el nuevo estado**
4. El estado se actualiza automáticamente
5. **🔔 Se envía notificación** de cambio de estado

#### **Flujo Lógico de Estados**
```
🟡 Pendiente → 🔵 Confirmado → 🟠 Preparando → 🟢 Listo → ✅ Entregado
     ↓              ↓              ↓
   ❌ Cancelado   ❌ Cancelado   ❌ Cancelado
```

### **🔔 Sistema de Notificaciones**

#### **Tipos de Notificaciones**

##### **Nuevo Pedido**
- **🔊 Sonido**: Tono de notificación del sistema
- **📳 Vibración**: Patrón estilo Uber (vibrar-pausa-vibrar)
- **📱 Notificación**: "🍔 Nuevo Pedido! - [Cliente] - $[Total]"
- **📋 Detalles**: Nombre del cliente, total, ID del pedido

##### **Cambio de Estado**
- **📱 Notificación**: "📋 Estado del Pedido Actualizado"
- **📋 Detalles**: ID del pedido, nuevo estado, cliente
- **🔊 Sin sonido**: Solo notificación visual

#### **Configuración de Notificaciones**
- Las notificaciones se configuran automáticamente
- Si no tienes permisos, solo habrá vibración
- Puedes desactivarlas desde Configuración de Android

## 🔐 Seguridad y Autenticación

### **🔒 Autenticación Biométrica**

#### **¿Cuándo se Requiere?**
- **✏️ Editar productos**: Siempre requiere huella
- **✏️ Editar usuarios**: Siempre requiere huella
- **➕ Crear elementos**: No requiere autenticación
- **👀 Ver información**: No requiere autenticación

#### **Configurar Huella Dactilar**
1. Ve a **Configuración** de Android
2. **Seguridad** > **Huella dactilar**
3. **Agregar huella dactilar**
4. Sigue las instrucciones en pantalla
5. ¡Listo para usar en BurgerMenu!

#### **Casos Sin Biometría**
Si tu dispositivo no tiene sensor o no tienes huellas registradas:
- La app funciona normalmente
- Se muestra un mensaje informativo
- Las operaciones se completan sin autenticación
- No hay limitaciones de funcionalidad

### **🛡️ Validaciones de Seguridad**
- **Campos obligatorios**: No se pueden dejar vacíos
- **Datos únicos**: Username y email deben ser únicos
- **Formato de precios**: Solo números válidos
- **Caracteres especiales**: Se escapan automáticamente para seguridad

## 📱 Consejos de Uso

### **⚡ Mejores Prácticas**

#### **Para Productos**
- 📷 **Toma fotos claras**: Mejora la presentación del menú
- 🏷️ **Usa categorías consistentes**: Facilita la organización
- 💰 **Precios precisos**: Evita errores en pedidos
- 📝 **Descripciones útiles**: Ayuda a los clientes a decidir

#### **Para Usuarios**
- 📧 **Emails válidos**: Para comunicación efectiva
- 📞 **Teléfonos actualizados**: Para contacto directo
- 🏷️ **Usernames únicos**: Evita conflictos en el sistema

#### **Para Pedidos**
- 🔄 **Actualiza estados rápidamente**: Mantén a los clientes informados
- 📋 **Revisa detalles**: Verifica información antes de confirmar
- 🔔 **Atiende notificaciones**: Responde rápido a nuevos pedidos

### **🚨 Solución de Problemas**

#### **No Puedo Tomar Fotos**
1. Verifica permisos de cámara en Configuración
2. Reinicia la aplicación
3. Verifica que la cámara funcione en otras apps

#### **La Huella No Funciona**
1. Verifica que tengas huellas registradas
2. Limpia el sensor de huella dactilar
3. Reintenta la autenticación
4. Si falla, la app continuará sin biometría

#### **No Recibo Notificaciones**
1. Verifica permisos de notificación
2. Revisa configuración de "No molestar"
3. Verifica que las notificaciones de la app estén habilitadas

#### **Problemas de Conexión**
1. Verifica conexión a internet
2. Reinicia la aplicación
3. Verifica que otros apps funcionen correctamente

### **📞 Soporte**
Si tienes problemas técnicos o necesitas ayuda:
- Revisa esta guía primero
- Verifica los permisos de la aplicación
- Reinicia la app si es necesario
- Contacta al equipo de desarrollo si persisten los problemas

## 🎯 Funcionalidades Próximas

### **🔜 Mejoras Planificadas**
- 🗑️ **Confirmación de eliminación**: Diálogos de confirmación
- 📊 **Reportes y estadísticas**: Analytics de ventas
- 🔍 **Búsqueda avanzada**: Filtros y búsqueda de texto
- 📷 **Galería de imágenes**: Visualización de fotos de productos
- 🎨 **Temas personalizables**: Modo oscuro y colores
- 📱 **Sincronización offline**: Trabajo sin conexión

---

**🍔 BurgerMenu** - Gestión moderna para restaurantes modernos

¡Gracias por usar BurgerMenu! Esta guía te ayudará a aprovechar al máximo todas las funcionalidades de la aplicación.