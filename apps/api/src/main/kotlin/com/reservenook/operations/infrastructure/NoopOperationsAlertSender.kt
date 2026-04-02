package com.reservenook.operations.infrastructure

import com.reservenook.operations.application.OperationsAlertMessage
import com.reservenook.operations.application.OperationsAlertSender
import org.slf4j.LoggerFactory

class NoopOperationsAlertSender : OperationsAlertSender {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun sendAlert(recipientEmail: String, message: OperationsAlertMessage) {
        logger.info("Operational alert skipped because no mail sender is configured. recipient={}, subject={}", recipientEmail, message.subject)
    }
}
