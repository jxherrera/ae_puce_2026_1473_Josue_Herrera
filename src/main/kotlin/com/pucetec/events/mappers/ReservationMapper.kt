package com.pucetec.events.mappers

import com.pucetec.events.dto.ReservationResponse
import com.pucetec.events.entities.Reservation

fun Reservation.toResponse() = ReservationResponse(
    id = this.id!!,
    attendeeId = this.attendee.id!!,
    eventId = this.event.id!!,
    status = this.status.name,
    createdAt = this.createdAt
)
