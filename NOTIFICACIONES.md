# Sistema de Notificaciones - BurgerMenu

## Descripción

La aplicación ahora cuenta con un sistema de notificaciones que alerta cuando:
1. **Aparece un nuevo pedido** en la base de datos
2. **Cambia el estado de un pedido** existente

## Cómo Funciona

### Monitoreo en Tiempo Real
- Cuando abres la pantalla de **Pedidos**, la app inicia un monitoreo automático
- Revisa la base de datos **cada 5 segundos** buscando cambios
- Detecta nuevos pedidos comparando los IDs actuales con los anteriores
- Detecta cambios de estado comparando el estado actual con el anterior

### Tipos de Notificaciones

#### 1. Nuevo Pedido
- **Título**: 🍔 Nuevo Pedido!
- **Contenido**: Nombre del cliente y total del pedido
- **Sonido**: Notificación predeterminada del sistema
- **Vibración**: Patrón de 500ms-250ms-500ms

#### 2. Cambio de Estado
- **Título**: 📋 Estado del Pedido Actualizado
- **Contenido**: ID del pedido y nuevo estado
- **Estados posibles**:
  - Pendiente (pending)
  - Confirmado (confirmed)
  - Preparando (preparing)
  - Listo (ready)
  - Entregado (delivered)
  - Cancelado (cancelled)

### Monitoreo en Segundo Plano

Además del monitoreo en tiempo real, la app tiene un **WorkManager** que:
- Revisa pedidos cada **15 minutos** incluso cuando la app está cerrada
- Requiere conexión a Internet
- Se programa automáticamente al abrir la app

## Permisos Necesarios

### Android 13+ (API 33+)
La app solicita automáticamente el permiso `POST_NOTIFICATIONS` al iniciar.

### Permisos en AndroidManifest.xml
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.INTERNET" />
```

## Configuración del Usuario

Para recibir notificaciones, el usuario debe:
1. **Aceptar el permiso** cuando la app lo solicite (Android 13+)
2. **Mantener las notificaciones activadas** en la configuración del sistema
3. **Tener conexión a Internet** para que la app pueda consultar la base de datos

## Componentes Técnicos

### 1. NotificationService.kt
Servicio que crea y muestra las notificaciones:
- Crea el canal de notificaciones
- Configura sonido y vibración
- Maneja los PendingIntents para abrir la app

### 2. OrderViewModel.kt
ViewModel que gestiona el monitoreo:
- `startMonitoring()`: Inicia el monitoreo cada 5 segundos
- `stopMonitoring()`: Detiene el monitoreo al salir de la pantalla
- `checkForNewOrdersAndUpdates()`: Compara pedidos actuales con anteriores

### 3. OrderMonitorWorker.kt
Worker de WorkManager para monitoreo en segundo plano:
- Se ejecuta cada 15 minutos
- Guarda el estado en SharedPreferences
- Funciona incluso con la app cerrada

### 4. MainActivity.kt
- Solicita permisos de notificación al iniciar
- Programa el WorkManager
- Inicia/detiene el monitoreo según la pantalla activa

## Solución de Problemas

### No recibo notificaciones

1. **Verifica los permisos**:
   - Ve a Configuración → Apps → BurgerMenu → Notificaciones
   - Asegúrate de que estén activadas

2. **Revisa los logs**:
   ```
   adb logcat | grep -E "OrderViewModel|NotificationService|OrderMonitorWorker"
   ```

3. **Verifica la conexión**:
   - La app necesita Internet para consultar la base de datos Turso

4. **Prueba manualmente**:
   - Abre la pantalla de Pedidos
   - Crea un pedido nuevo desde otra fuente (API, base de datos directa)
   - Espera 5 segundos máximo

### Las notificaciones llegan tarde

- El monitoreo en tiempo real revisa cada **5 segundos**
- El WorkManager revisa cada **15 minutos**
- Si la pantalla de Pedidos no está abierta, solo funciona el WorkManager

### Notificaciones duplicadas

- Esto puede ocurrir si el WorkManager y el monitoreo en tiempo real detectan el mismo cambio
- Es normal y no afecta la funcionalidad

## Mejoras Futuras

1. **WebSockets**: Implementar conexión en tiempo real para notificaciones instantáneas
2. **Firebase Cloud Messaging**: Para notificaciones push más confiables
3. **Configuración personalizable**: Permitir al usuario ajustar la frecuencia de monitoreo
4. **Sonidos personalizados**: Diferentes sonidos para nuevos pedidos vs cambios de estado
5. **Notificaciones agrupadas**: Agrupar múltiples pedidos en una sola notificación

## Notas Técnicas

- El monitoreo usa **coroutines** de Kotlin para no bloquear el hilo principal
- Los estados anteriores se mantienen en memoria mientras el ViewModel está activo
- El WorkManager usa **SharedPreferences** para persistir el estado entre ejecuciones
- Las notificaciones usan **NotificationCompat** para compatibilidad con versiones antiguas de Android
