package com.pucetec.events.controllers

import com.pucetec.events.dto.EventRequest
import com.pucetec.events.dto.EventResponse
import com.pucetec.events.services.EventService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/events")
class EventController(
    private val eventService: EventService
) {
    @GetMapping
    fun getAllEvents(): List<EventResponse> {
        return eventService.getAllEvents()
    }

    @GetMapping("/{id}")
    fun getEventById(@PathVariable id: Long): EventResponse {
        return eventService.getEventById(id)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createEvent(@RequestBody request: EventRequest): EventResponse {
        return eventService.createEvent(request)
    }
}
