package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class InvoiceServiceTest {
    private val dal = mockk<AntaeusDal>(relaxed = true) {
        every { fetchInvoice(404) } returns null
    }

    private val invoiceService = InvoiceService(dal = dal)

    @Test
    fun `will throw if invoice is not found`() {
        assertThrows<InvoiceNotFoundException> {
            invoiceService.fetch(404)
        }
    }

    @Test
    fun fetchPendingInvoices_whenPendingInvoicesAreFetched_shouldCallDALMethod() {
        // when
        invoiceService.fetchPendingInvoices()

        // then
        verify { dal.fetchPendingInvoices() }
    }

    @Test
    fun setInvoiceAsPaid_whenInvoiceIsPaid_shouldCallDALMethod() {
        // given
        val invoiceId = 1
        val amount = Money(BigDecimal(10.02), Currency.EUR)
        val invoice = Invoice(invoiceId, 2, amount, InvoiceStatus.PENDING)
        every { dal.fetchInvoice(invoiceId) } returns invoice

        // when
        invoiceService.setInvoiceAsPaid(invoiceId)

        // then
        verify { dal.setInvoiceAsPaid(invoiceId) }
    }
}
