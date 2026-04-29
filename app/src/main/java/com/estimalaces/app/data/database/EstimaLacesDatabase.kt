package com.estimalaces.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.estimalaces.app.data.dao.ClientDao
import com.estimalaces.app.data.dao.GiftDao
import com.estimalaces.app.data.dao.GoalDao
import com.estimalaces.app.data.dao.PaymentDao
import com.estimalaces.app.data.dao.ProductDao
import com.estimalaces.app.data.dao.SaleDao
import com.estimalaces.app.data.entity.ClientEntity
import com.estimalaces.app.data.entity.GiftEntity
import com.estimalaces.app.data.entity.GoalEntity
import com.estimalaces.app.data.entity.PaymentEntity
import com.estimalaces.app.data.entity.ProductEntity
import com.estimalaces.app.data.entity.SaleEntity

@Database(
    entities = [
        ProductEntity::class,
        ClientEntity::class,
        SaleEntity::class,
        GoalEntity::class,
        GiftEntity::class,
        PaymentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class EstimaLacesDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun clientDao(): ClientDao
    abstract fun saleDao(): SaleDao
    abstract fun goalDao(): GoalDao
    abstract fun giftDao(): GiftDao
    abstract fun paymentDao(): PaymentDao
}
