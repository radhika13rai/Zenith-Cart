package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.CartItemEntity
import com.example.model.NotificationEntity
import com.example.model.OrderEntity
import com.example.model.ProductEntity
import com.example.model.SupportTicketEntity
import com.example.model.WishlistItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OmniCartDao {

    // --- Products ---
    @Query("SELECT * FROM products ORDER BY rating DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: String): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductByIdDirect(id: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Query("UPDATE products SET stock = :newStock WHERE id = :productId")
    suspend fun updateStock(productId: String, newStock: Int)

    @Query("UPDATE products SET priceUsd = :price, stock = :stock WHERE id = :productId")
    suspend fun updateProductPriceAndStock(productId: String, price: Double, stock: Int)

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProduct(productId: String)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    // --- Cart ---
    @Query("SELECT * FROM cart_items ORDER BY addedTimestamp DESC")
    fun getCartItems(): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE productId = :productId")
    suspend fun updateCartQuantity(productId: String, quantity: Int)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun deleteCartItem(productId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    // --- Wishlist ---
    @Query("SELECT * FROM wishlist_items ORDER BY addedTimestamp DESC")
    fun getWishlistItems(): Flow<List<WishlistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlist(item: WishlistItemEntity)

    @Query("DELETE FROM wishlist_items WHERE productId = :productId")
    suspend fun deleteWishlistItem(productId: String)

    @Query("DELETE FROM wishlist_items")
    suspend fun clearWishlist()

    // --- Orders & 3PL Logistics ---
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE orderId = :orderId")
    fun getOrderById(orderId: String): Flow<OrderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Query("UPDATE orders SET shippingStatus = :status, currentHub = :hub WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String, hub: String)

    // --- Support Tickets ---
    @Query("SELECT * FROM support_tickets ORDER BY createdAt DESC")
    fun getAllTickets(): Flow<List<SupportTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicketEntity)

    @Query("UPDATE support_tickets SET status = :status, lastUpdate = :time WHERE ticketId = :ticketId")
    suspend fun updateTicketStatus(ticketId: String, status: String, time: Long)

    // --- Notifications ---
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationRead(id: String)
}
