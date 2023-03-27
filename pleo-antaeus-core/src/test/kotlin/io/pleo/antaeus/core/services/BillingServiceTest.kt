package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class BillingServiceTest {

    private val paymentProvider = mockk<PaymentProvider>(relaxed = true)
    private val invoiceService = mockk<InvoiceService>(relaxed = true)
    private val customerService = mockk<CustomerService> {}
    private val sut = BillingService(paymentProvider, invoiceService, customerService)

    @Test
    fun payInvoices_whenRetrievingAllInvoices_shouldFetchAllInvoices() {
        // given
        every { invoiceService.fetchPendingInvoices() } returns listOf()

        // when
        sut.payInvoices()

        // then
        verify { invoiceService.fetchPendingInvoices() }
    }

    @Test
    fun payInvoice_whenRetrievingCustomer_shouldFetchCustomer() {
        // given
        val customerId = 1
        val currency = Currency.EUR
        val amount = Money(BigDecimal(10.02), currency)
        val customer = Customer(customerId, currency)
        val invoice = Invoice(2, customerId, amount, InvoiceStatus.PAID)
        every { customerService.fetch(customerId ) } returns customer

        // when
        sut.payInvoice(invoice, customerId)

        // then
        verify { customerService.fetch(customerId) }
    }

    @Test
    fun payInvoice_whenChargingInvoice_shouldSetInvoiceAsPaid() {
        // given
        val customerId = 1
        val invoiceId = 2
        val currency = Currency.EUR
        val amount = Money(BigDecimal(10.02), currency)
        val customer = Customer(customerId, currency)
        val invoice = Invoice(invoiceId, customerId, amount, InvoiceStatus.PAID)
        every { customerService.fetch(customerId ) } returns customer

        // when
        sut.payInvoice(invoice, customerId)

        // then
        verify { invoiceService.setInvoiceAsPaid(invoiceId) }
    }

    @Test
    fun payInvoice_whenInvoiceCurrencyIsNotTheSameAsCustomer_shouldThrowCurrencyMismatchException() {
        // given
        val customerId = 1
        val amount = Money(BigDecimal(10.02), Currency.EUR)
        val invoice = Invoice(2, customerId, amount, InvoiceStatus.PAID)
        val customer = Customer(customerId, Currency.DKK)
        every { customerService.fetch(customerId ) } returns customer

        // when/then
        assertThrows<CurrencyMismatchException> {
            sut.payInvoice(invoice, customerId)
        }
    }
}
