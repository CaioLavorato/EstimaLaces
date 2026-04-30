package com.estimalaces.app.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.estimalaces.app.R
import com.estimalaces.app.data.entity.ProductEntity

class LowStockNotifier(private val context: Context) {
    private val channelId = "low_stock"
    private val notifiedProducts = mutableSetOf<Long>()

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Estoque baixo",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Avisos quando produtos precisam de reposição."
            }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    fun notifyLowStock(products: List<ProductEntity>) {
        products.forEach { product ->
            if (product.id in notifiedProducts) return@forEach
            if (!canNotify()) return
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Estoque baixo")
                .setContentText("${product.name} está acabando (${product.currentQuantity} un.)")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("${product.name} está abaixo do estoque mínimo. Faça uma reposição quando possível.")
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(context).notify(product.id.toInt(), notification)
            notifiedProducts.add(product.id)
        }
    }

    private fun canNotify(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }
}
