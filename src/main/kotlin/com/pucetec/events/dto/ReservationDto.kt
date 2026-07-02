package com.pucetec.events.dto

import java.time.LocalDateTime

data class ReservationRequest(
    val attendeeId: Long,
    val eventId: Long
)

data class ReservationResponse(
    val id: Long,
    val attendeeId: Long,
    val eventId: Long,
    val status: String,
    val createdAt: LocalDateTime
)
