package com.pucetec.events.mappers

import com.pucetec.events.dto.EventRequest
import com.pucetec.events.dto.EventResponse
import com.pucetec.events.entities.Event

fun Event.toResponse() = EventResponse(
    id = this.id!!,
    name = this.name,
    venue = this.venue,
    totalTickets = this.totalTickets,
    availableTickets = this.availableTickets
)

fun EventRequest.toEntity() = Event(
    name = this.name,
    venue = this.venue,
    totalTickets = this.totalTickets,
    availableTickets = this.totalTickets
)
