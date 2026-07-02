package com.pucetec.events.controllers

import com.pucetec.events.dto.AttendeeRequest
import com.pucetec.events.dto.AttendeeResponse
import com.pucetec.events.services.AttendeeService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/attendees")
class AttendeeController(
    private val attendeeService: AttendeeService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createAttendee(@RequestBody request: AttendeeRequest): AttendeeResponse {
        return attendeeService.createAttendee(request)
    }
}
