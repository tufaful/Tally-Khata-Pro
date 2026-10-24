package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.entities.CustomerEntity
import com.example.data.entities.KhataEntity
import com.example.data.repository.TallyRepository
import com.example.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EditTransactionTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: TallyRepository
    private var khataId: Long = 0L
    private var customerId: Long = 0L

    @Before
    fun setup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = TallyRepository(db)

        khataId = db.khataDao().insertKhata(
            KhataEntity(name = "মূল খাতা", businessName = "রহিম স্টোর", isDefault = true)
        )
        customerId = db.customerDao().insertCustomer(
            CustomerEntity(khataId = khataId, name = "মোঃ রহিম", phone = "01700000000")
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `test edit transaction amount updates customer balance correctly from 5000 to 7000`() = runBlocking {
        // 1. Initial transaction: রহিম -> পাওনা ৳5,000
        val txnId = repository.addTransaction(
            khataId = khataId,
            customerId = customerId,
            type = TransactionType.CREDIT,
            amount = 5000L,
            note = "প্রাথমিক বাকি বিক্রি"
        )

        // Verify initial customer balance is 5000
        var customer = repository.getCustomerById(customerId)
        assertNotNull(customer)
        assertEquals(5000L, customer?.currentBalance)
        assertEquals(5000L, customer?.totalCredit)
        assertEquals(0L, customer?.totalPayment)

        // 2. Edit transaction: রহিম -> পাওনা ৳7,000
        val updateSuccess = repository.updateTransaction(
            id = txnId,
            amount = 7000L,
            type = TransactionType.CREDIT,
            note = "সংশোধিত বাকি বিক্রি"
        )
        assertTrue(updateSuccess)

        // 3. Verify customer balance is recalculated to 7000
        customer = repository.getCustomerById(customerId)
        assertNotNull(customer)
        assertEquals(7000L, customer?.currentBalance)
        assertEquals(7000L, customer?.totalCredit)
        assertEquals(0L, customer?.totalPayment)

        // 4. Verify original transaction ID is preserved (no duplicate created)
        val allTxns = db.transactionDao().getAllActiveTransactionsSync()
        assertEquals(1, allTxns.size)
        val editedTxn = allTxns.first()
        assertEquals(txnId, editedTxn.id)
        assertEquals(7000L, editedTxn.amount)
        assertEquals("সংশোধিত বাকি বিক্রি", editedTxn.note)
    }

    @Test
    fun `test edit transaction type from credit to payment recalculates balance`() = runBlocking {
        // Initial transaction: পাওনা ৳5,000
        val txnId = repository.addTransaction(
            khataId = khataId,
            customerId = customerId,
            type = TransactionType.CREDIT,
            amount = 5000L,
            note = "ভুলবশত পাওনা হিসেবে এন্ট্রি"
        )

        var customer = repository.getCustomerById(customerId)
        assertEquals(5000L, customer?.currentBalance)

        // Edit transaction: change type to জমা (PAYMENT) ৳5,000
        val updateSuccess = repository.updateTransaction(
            id = txnId,
            amount = 5000L,
            type = TransactionType.PAYMENT,
            note = "সংশোধিত: নগদ জমা গ্রহণ"
        )
        assertTrue(updateSuccess)

        // Customer balance should now be -5000 (advance/paid)
        customer = repository.getCustomerById(customerId)
        assertNotNull(customer)
        assertEquals(-5000L, customer?.currentBalance)
        assertEquals(0L, customer?.totalCredit)
        assertEquals(5000L, customer?.totalPayment)
    }

    @Test
    fun `test editing earlier transaction updates running balances for subsequent transactions`() = runBlocking {
        val baseTime = System.currentTimeMillis()

        // Txn 1: 5000 Credit
        val txnId1 = repository.addTransaction(
            khataId = khataId,
            customerId = customerId,
            type = TransactionType.CREDIT,
            amount = 5000L,
            date = baseTime
        )

        // Txn 2: 2000 Payment
        val txnId2 = repository.addTransaction(
            khataId = khataId,
            customerId = customerId,
            type = TransactionType.PAYMENT,
            amount = 2000L,
            date = baseTime + 1000L
        )

        var customer = repository.getCustomerById(customerId)
        assertEquals(3000L, customer?.currentBalance)

        // Edit Txn 1 from 5000 to 8000 Credit
        repository.updateTransaction(
            id = txnId1,
            amount = 8000L,
            type = TransactionType.CREDIT,
            date = baseTime
        )

        // Balance should now be 8000 - 2000 = 6000
        customer = repository.getCustomerById(customerId)
        assertEquals(6000L, customer?.currentBalance)

        // Check running balances on Txn 2
        val updatedTxn2 = repository.getTransactionById(txnId2)
        assertNotNull(updatedTxn2)
        assertEquals(8000L, updatedTxn2?.previousBalance)
        assertEquals(6000L, updatedTxn2?.newBalance)
    }

    @Test
    fun `test transaction edit creates audit log with previous and new values`() = runBlocking {
        val txnId = repository.addTransaction(
            khataId = khataId,
            customerId = customerId,
            type = TransactionType.CREDIT,
            amount = 5000L,
            note = "পুরাতন নোট"
        )

        repository.updateTransaction(
            id = txnId,
            amount = 7000L,
            type = TransactionType.CREDIT,
            note = "নতুন নোট"
        )

        val logs = repository.getRecentAuditLogs().first()
        val editLog = logs.find { it.action == "TRANSACTION_EDITED" && it.entityId == txnId }
        assertNotNull("Audit log for transaction edit must exist", editLog)
        assertTrue(editLog!!.description.contains("Amount: ৳5000 → ৳7000"))
        assertTrue(editLog.description.contains("Modified:"))
    }
}
