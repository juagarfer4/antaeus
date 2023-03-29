package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import mu.KotlinLogging

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService,
    private val customerService: CustomerService
) {

    private val logger = KotlinLogging.logger {}
    private val maxNumberOfRetries = 3

    /**
     * Retrieves pending invoices and sets the payment for each of them.
     *
     */
    fun payInvoices() {
        // fetch only pending invoices so not to check each invoice's status before trying to charge
        invoiceService.fetchPendingInvoices().forEach { invoice -> payInvoice(invoice, invoice.customerId) }
        logger.info { "Fetching all pending invoices" }
    }

    /**
     * Handles the payment of a single invoice.
     *
     * @param  invoice                      the invoice to be paid
     * @param  customerId                   the ID of the customer responsible for the invoice
     * @throws CurrencyMismatchException    if the currency of the invoice and its corresponding customer do not match
     * @throws NetworkException             when a network error happens
     * @return                              the charged invoice
     */
    fun payInvoice(invoice: Invoice, customerId: Int): Invoice {
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
                // invoice needs to be registered as paid
                invoiceService.setInvoiceAsPaid(invoiceId)
                logger.info { "Invoice '$invoiceId' has been paid" }
            } catch (e: NetworkException) {
                logger.error { "A network error happened; payment '$invoiceId' could not be completed" }
            }
        }

        return invoice
    }
}
