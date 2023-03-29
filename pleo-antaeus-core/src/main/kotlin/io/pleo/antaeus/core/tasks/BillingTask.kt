package io.pleo.antaeus.core.tasks

import io.pleo.antaeus.core.services.BillingService
import mu.KotlinLogging
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class BillingTask(private val billingService: BillingService) {

    private val logger = KotlinLogging.logger {}
    private val chargingDay = 1

    /**
     * Executes the scheduled task for the monthly payment of the invoices.
     *
     */
    fun run() {
        val scheduler = Executors.newScheduledThreadPool(1)

        val task = Runnable {
            logger.info { "Starting task" }
            billingService.payInvoices()
        }

        var dateTime = ZonedDateTime.now()
        if (dateTime.dayOfMonth > chargingDay) {
            dateTime = dateTime.plusMonths(1)
        }
        dateTime = dateTime.withDayOfMonth(chargingDay)

        // set up the handler so that the task is executed on a monthly basis from the first of the month
        val delay = dateTime.until(dateTime, ChronoUnit.MILLIS)
        val taskHandle = scheduler.scheduleAtFixedRate(task, delay, 1, TimeUnit.SECONDS)
    }
}
