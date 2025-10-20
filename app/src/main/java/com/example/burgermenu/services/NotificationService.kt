package com.example.burgermenu.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.burgermenu.MainActivity
import com.example.burgermenu.R

class NotificationService(private val context: Context) {
    
    companion object {
        private const val CHANNEL_ID = "order_notifications"
        private const val CHANNEL_NAME = "Pedidos"
        private const val CHANNEL_DESCRIPTION = "Notificaciones de nuevos pedidos"
        private const val NOTIFICATION_ID = 1001
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
                
                // Sonido personalizado (usar sonido por defecto de notificación)
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                setSound(soundUri, AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build())
            }
            
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun showNewOrderNotification(orderId: String, customerName: String, total: Double) {
        // Intent para abrir la app cuando se toque la notificación
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_orders", true)
        }
        
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Construir la notificación
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Usar ícono del sistema por ahora
            .setContentTitle("🍔 Nuevo Pedido!")
            .setContentText("$customerName - $${String.format("%.2f", total)}")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Nuevo pedido de $customerName\nTotal: $${String.format("%.2f", total)}\nID: $orderId"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(longArrayOf(0, 500, 250, 500))
        
        // Mostrar la notificación
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, builder.build())
            }
            
            // Vibración adicional para asegurar que se sienta
            vibratePhone()
            
        } catch (e: SecurityException) {
            // Si no hay permisos de notificación, solo vibrar
            vibratePhone()
        }
    }
    
    private fun vibratePhone() {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Patrón de vibración: esperar 0ms, vibrar 500ms, pausa 250ms, vibrar 500ms
                val vibrationEffect = VibrationEffect.createWaveform(
                    longArrayOf(0, 500, 250, 500), 
                    -1 // No repetir
                )
                vibrator.vibrate(vibrationEffect)
            } else {
                // Para versiones anteriores
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 500, 250, 500), -1)
            }
        } catch (e: Exception) {
            // Si falla la vibración, continuar sin error
        }
    }
    
    fun showOrderStatusUpdateNotification(orderId: String, newStatus: String, customerName: String) {
        val statusText = when (newStatus) {
            "confirmed" -> "Confirmado"
            "preparing" -> "Preparando"
            "ready" -> "Listo para recoger"
            "delivered" -> "Entregado"
            "cancelled" -> "Cancelado"
            else -> newStatus
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_orders", true)
        }
        
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("📋 Estado del Pedido Actualizado")
            .setContentText("Pedido $orderId: $statusText")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("El pedido de $customerName ha cambiado a: $statusText\nID: $orderId"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID + 1, builder.build())
            }
        } catch (e: SecurityException) {
            // Continuar sin notificación si no hay permisos
        }
    }
}