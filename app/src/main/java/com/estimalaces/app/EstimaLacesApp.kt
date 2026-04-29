package com.estimalaces.app

import android.app.Application
import androidx.room.Room
import com.estimalaces.app.data.database.EstimaLacesDatabase
import com.estimalaces.app.data.repository.EstimaLacesRepository

class EstimaLacesApp : Application() {
    lateinit var repository: EstimaLacesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = Room.databaseBuilder(
            applicationContext,
            EstimaLacesDatabase::class.java,
            "estimalaces.db"
        ).build()

        repository = EstimaLacesRepository(
            productDao = database.productDao(),
            clientDao = database.clientDao(),
            saleDao = database.saleDao(),
            goalDao = database.goalDao(),
            giftDao = database.giftDao()
        )
    }
}
