package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import mu.KotlinLogging

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService,
    private val customerService: CustomerService
) {
// TODO - Add code e.g. here

    private val logger = KotlinLogging.logger {}
    private val maxNumberOfRetries = 3

    fun payInvoices() {
        // fetch only pending invoices so not to check each invoice's status before trying to charge
        invoiceService.fetchPendingInvoices().forEach { invoice -> payInvoice(invoice, invoice.customerId) }
        logger.info { "Fetching all pending invoices" }
    }

    fun payInvoice(invoice: Invoice, customerId: Int) {
        val customer = customerService.fetch(customerId)
        val invoiceId = invoice.id

        if (invoice.amount.currency != customer.currency) {
            logger.error { "Invoice '$invoiceId' currency is different from customer '$customerId' currency" }
            throw CurrencyMismatchException(invoiceId, customerId)
        }

        // retry mechanism in case PaymentProvider external service can't process charge initially
        var charged = false
        var retries = 0
        while (!charged && retries < maxNumberOfRetries) {
            retries++
            try {
                charged = paymentProvider.charge(invoice)
                logger.info { "Invoice '$invoiceId' has been paid" }
            } catch (e: NetworkException) {
                logger.error { "A network error happened; payment '$invoiceId' could not be completed" }
            }
        }
    }
}
