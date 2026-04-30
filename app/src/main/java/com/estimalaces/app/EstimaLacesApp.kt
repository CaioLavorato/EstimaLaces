package com.estimalaces.app

import android.app.Application
import androidx.room.Room
import com.estimalaces.app.data.database.EstimaLacesDatabase
import com.estimalaces.app.data.repository.EstimaLacesRepository
import com.estimalaces.app.integration.OrdersSyncService
import com.estimalaces.app.notification.LowStockNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class EstimaLacesApp : Application() {
    lateinit var repository: EstimaLacesRepository
        private set
    private var ordersSyncService: OrdersSyncService? = null
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        val database = Room.databaseBuilder(
            applicationContext,
            EstimaLacesDatabase::class.java,
            "estimalaces.db"
        )
            .addMigrations(
                EstimaLacesDatabase.MIGRATION_1_2,
                EstimaLacesDatabase.MIGRATION_2_3,
                EstimaLacesDatabase.MIGRATION_3_4
            )
            .build()

        repository = EstimaLacesRepository(
            productDao = database.productDao(),
            clientDao = database.clientDao(),
            saleDao = database.saleDao(),
            goalDao = database.goalDao(),
            giftDao = database.giftDao(),
            stockMovementDao = database.stockMovementDao()
        )

        ordersSyncService = OrdersSyncService(repository).also { it.start() }
        val notifier = LowStockNotifier(this)
        appScope.launch {
            repository.observeLowStockProducts().collect { products ->
                notifier.notifyLowStock(products)
            }
        }
    }
}
