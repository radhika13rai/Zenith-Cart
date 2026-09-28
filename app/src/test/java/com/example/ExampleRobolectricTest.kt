package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.OmniCartDao
import com.example.data.local.OmniCartDatabase
import com.example.model.Currency
import com.example.model.WishlistItemEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: OmniCartDatabase
    private lateinit var dao: OmniCartDao

    @Before
    fun setupDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OmniCartDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.dao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("OmniCart", appName)
    }

    @Test
    fun `test multi-currency formatting`() {
        val amount = 100.0
        val usd = Currency.USD.format(amount)
        val inr = Currency.INR.format(amount)
        val eur = Currency.EUR.format(amount)
        val jpy = Currency.JPY.format(amount)

        assertEquals("$100.00", usd)
        assertEquals("₹8,350.00", inr)
        assertEquals("€92.00", eur)
        assertEquals("¥15,500", jpy)
    }

    @Test
    fun `test Room database Wishlist operations`() = runBlocking {
        // Initially empty
        var items = dao.getWishlistItems().first()
        assertTrue(items.isEmpty())

        // Save items to Room database
        dao.insertWishlist(WishlistItemEntity(productId = "prod_001"))
        dao.insertWishlist(WishlistItemEntity(productId = "prod_002"))

        items = dao.getWishlistItems().first()
        assertEquals(2, items.size)
        assertTrue(items.any { it.productId == "prod_001" })
        assertTrue(items.any { it.productId == "prod_002" })

        // Delete one item
        dao.deleteWishlistItem("prod_001")
        items = dao.getWishlistItems().first()
        assertEquals(1, items.size)
        assertEquals("prod_002", items.first().productId)

        // Clear all wishlist
        dao.clearWishlist()
        items = dao.getWishlistItems().first()
        assertTrue(items.isEmpty())
    }
}
