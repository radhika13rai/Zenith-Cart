package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.CartItemEntity
import com.example.model.NotificationEntity
import com.example.model.OrderEntity
import com.example.model.ProductEntity
import com.example.model.SupportTicketEntity
import com.example.model.WishlistItemEntity

@Database(
    entities = [
        ProductEntity::class,
        CartItemEntity::class,
        WishlistItemEntity::class,
        OrderEntity::class,
        SupportTicketEntity::class,
        NotificationEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class OmniCartDatabase : RoomDatabase() {
    abstract fun dao(): OmniCartDao

    companion object {
        @Volatile
        private var INSTANCE: OmniCartDatabase? = null

        fun getDatabase(context: Context): OmniCartDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OmniCartDatabase::class.java,
                    "omnicart_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
