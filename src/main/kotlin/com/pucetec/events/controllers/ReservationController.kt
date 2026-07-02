package com.pucetec.events.controllers

import com.pucetec.events.dto.ReservationRequest
import com.pucetec.events.dto.ReservationResponse
import com.pucetec.events.services.ReservationService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/reservations")
class ReservationController(
    private val reservationService: ReservationService
) {
    @GetMapping
    fun getAllReservations(): List<ReservationResponse> {
        return reservationService.getAllReservations()
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createReservation(@RequestBody request: ReservationRequest): ReservationResponse {
        return reservationService.createReservation(request)
    }

    @PutMapping("/{id}/cancel")
    fun cancelReservation(@PathVariable id: Long) {
        reservationService.cancelReservation(id)
    }
}
