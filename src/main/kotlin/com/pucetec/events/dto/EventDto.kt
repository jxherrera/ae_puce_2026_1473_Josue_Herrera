package com.pucetec.events.dto

data class EventRequest(
    val name: String,
    val venue: String,
    val totalTickets: Int
)

data class EventResponse(
    val id: Long,
    val name: String,
    val venue: String,
    val totalTickets: Int,
    val availableTickets: Int
)
