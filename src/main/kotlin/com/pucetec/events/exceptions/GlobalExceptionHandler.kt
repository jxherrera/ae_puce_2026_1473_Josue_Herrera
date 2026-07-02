package com.pucetec.events.exceptions

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BlankFieldException::class, InvalidCapacityException::class)
    fun handleBadRequest(ex: RuntimeException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ExceptionResponse(ex.message))
    }

    @ExceptionHandler(AttendeeNotFoundException::class, EventNotFoundException::class, ReservationNotFoundException::class)
    fun handleNotFound(ex: RuntimeException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ExceptionResponse(ex.message))
    }

    @ExceptionHandler(SoldOutException::class, ReservationLimitExceededException::class, ReservationAlreadyCancelledException::class)
    fun handleConflict(ex: RuntimeException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ExceptionResponse(ex.message))
    }
}
