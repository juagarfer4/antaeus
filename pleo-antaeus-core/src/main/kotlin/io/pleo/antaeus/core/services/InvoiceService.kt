/*
    Implements endpoints related to invoices.
 */

package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Invoice

class InvoiceService(private val dal: AntaeusDal) {
    fun fetchAll(): List<Invoice> {
        return dal.fetchInvoices()
    }

    /**
     * Retrieves all invoices that have not been paid.
     *
     * @return  the pending invoices
     */
    fun fetchPendingInvoices(): List<Invoice> {
        return dal.fetchPendingInvoices()
    }

    fun fetch(id: Int): Invoice {
        return dal.fetchInvoice(id) ?: throw InvoiceNotFoundException(id)
    }

    /**
     * Sets an invoice as paid once it has been charged.
     *
     * @param  id   the ID of the invoice to be paid
     * @return      the paid invoice
     */
    fun setInvoiceAsPaid(id: Int): Invoice {
        return dal.setInvoiceAsPaid(id) ?: throw InvoiceNotFoundException(id)
    }
}
