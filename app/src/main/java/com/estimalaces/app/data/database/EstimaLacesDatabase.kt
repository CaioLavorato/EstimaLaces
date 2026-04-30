package com.estimalaces.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.estimalaces.app.data.dao.ClientDao
import com.estimalaces.app.data.dao.GiftDao
import com.estimalaces.app.data.dao.GoalDao
import com.estimalaces.app.data.dao.PaymentDao
import com.estimalaces.app.data.dao.ProductDao
import com.estimalaces.app.data.dao.SaleDao
import com.estimalaces.app.data.dao.StockMovementDao
import com.estimalaces.app.data.entity.ClientEntity
import com.estimalaces.app.data.entity.GiftEntity
import com.estimalaces.app.data.entity.GoalEntity
import com.estimalaces.app.data.entity.PaymentEntity
import com.estimalaces.app.data.entity.ProductEntity
import com.estimalaces.app.data.entity.SaleEntity
import com.estimalaces.app.data.entity.StockMovementEntity

@Database(
    entities = [
        ProductEntity::class,
        ClientEntity::class,
        SaleEntity::class,
        GoalEntity::class,
        GiftEntity::class,
        PaymentEntity::class,
        StockMovementEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class EstimaLacesDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun clientDao(): ClientDao
    abstract fun saleDao(): SaleDao
    abstract fun goalDao(): GoalDao
    abstract fun giftDao(): GiftDao
    abstract fun paymentDao(): PaymentDao
    abstract fun stockMovementDao(): StockMovementDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN currentQuantity INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE products ADD COLUMN minimumQuantity INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE sales ADD COLUMN giftType TEXT NOT NULL DEFAULT 'VALOR'")
                db.execSQL("ALTER TABLE sales ADD COLUMN giftProductId INTEGER")
                db.execSQL("ALTER TABLE sales ADD COLUMN giftProductName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE sales ADD COLUMN cardFeePercent REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sales ADD COLUMN cardFeeValue REAL NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sales ADD COLUMN externalOrderId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE sales ADD COLUMN source TEXT NOT NULL DEFAULT 'APP'")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS stock_movements (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        productId INTEGER NOT NULL,
                        productName TEXT NOT NULL,
                        type TEXT NOT NULL,
                        quantity INTEGER NOT NULL,
                        previousStock INTEGER NOT NULL,
                        currentStock INTEGER NOT NULL,
                        note TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
